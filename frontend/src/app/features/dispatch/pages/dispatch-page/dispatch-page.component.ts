import { DecimalPipe } from '@angular/common';
import {
  Component,
  DestroyRef,
  OnDestroy,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subscription } from 'rxjs';
import { DispatchMapComponent } from '../../components/dispatch-map/dispatch-map.component';
import { DispatchRequestFormComponent } from '../../components/dispatch-request-form/dispatch-request-form.component';
import { ProviderCardComponent } from '../../components/provider-card/provider-card.component';
import {
  DispatchPhase,
  DispatchProvider,
  DispatchRequest,
  GeoPoint,
  PICKUP_POINTS,
  SERVICE_TYPE_LABELS,
} from '../../models/dispatch.models';
import { DispatchMockService } from '../../services/dispatch-mock.service';

@Component({
  selector: 'app-dispatch-page',
  standalone: true,
  imports: [
    DecimalPipe,
    DispatchMapComponent,
    DispatchRequestFormComponent,
    ProviderCardComponent,
  ],
  templateUrl: './dispatch-page.component.html',
  styleUrl: './dispatch-page.component.css',
})
export class DispatchPageComponent implements OnInit, OnDestroy {
  private readonly dispatchMockService = inject(DispatchMockService);
  private readonly destroyRef = inject(DestroyRef);

  readonly defaultPickup = PICKUP_POINTS[0];

  readonly pickup = signal<GeoPoint>({
    lat: this.defaultPickup.lat,
    lng: this.defaultPickup.lng,
  });
  readonly request = signal<DispatchRequest | null>(null);
  readonly providers = signal<DispatchProvider[]>([]);
  readonly selectedProvider = signal<DispatchProvider | null>(null);
  readonly assignedProvider = signal<DispatchProvider | null>(null);
  readonly trackingPosition = signal<GeoPoint | null>(null);
  readonly phase = signal<DispatchPhase>('idle');
  readonly loadingProviders = signal(false);
  readonly error = signal<string | null>(null);

  readonly serviceLabel = computed(() => {
    const request = this.request();
    return request ? SERVICE_TYPE_LABELS[request.serviceType] : 'Assistance routière';
  });

  readonly phaseLabel = computed(() => {
    switch (this.phase()) {
      case 'searching':
        return 'Recherche de dépanneurs';
      case 'providers':
        return 'Dépanneurs disponibles';
      case 'tracking':
        return 'Dépanneur en route';
      case 'arrived':
        return 'Dépanneur sur place';
      default:
        return 'Prêt pour une demande';
    }
  });

  readonly progress = computed(() => {
    switch (this.phase()) {
      case 'providers':
        return 30;
      case 'tracking':
        return 68;
      case 'arrived':
        return 100;
      default:
        return 0;
    }
  });

  private trackingSubscription?: Subscription;

  ngOnInit(): void {
    this.loadProviders(this.defaultPickup, false);
  }

  ngOnDestroy(): void {
    this.trackingSubscription?.unsubscribe();
  }

  handleRequest(request: DispatchRequest): void {
    this.trackingSubscription?.unsubscribe();
    this.trackingSubscription = undefined;

    this.request.set(request);
    this.pickup.set({
      lat: request.pickup.lat,
      lng: request.pickup.lng,
    });
    this.selectedProvider.set(null);
    this.assignedProvider.set(null);
    this.trackingPosition.set(null);
    this.providers.set([]);
    this.phase.set('searching');

    this.loadProviders(this.pickup(), true);
  }

  selectProvider(provider: DispatchProvider): void {
    if (this.phase() !== 'providers') {
      return;
    }

    this.selectedProvider.set(provider);
  }

  confirmProvider(): void {
    const provider = this.selectedProvider();
    const request = this.request();

    if (!provider || !request) {
      return;
    }

    this.assignedProvider.set(provider);
    this.trackingPosition.set(provider.position);
    this.phase.set('tracking');

    this.trackingSubscription?.unsubscribe();
    this.trackingSubscription = this.dispatchMockService
      .trackProvider(provider, this.pickup())
      .subscribe({
        next: (position) => this.trackingPosition.set(position),
        complete: () => this.phase.set('arrived'),
      });
  }

  reset(): void {
    this.trackingSubscription?.unsubscribe();
    this.trackingSubscription = undefined;

    this.request.set(null);
    this.selectedProvider.set(null);
    this.assignedProvider.set(null);
    this.trackingPosition.set(null);
    this.phase.set('idle');
    this.pickup.set({
      lat: this.defaultPickup.lat,
      lng: this.defaultPickup.lng,
    });
    this.providers.set([]);

    this.loadProviders(this.defaultPickup, false);
  }

  private loadProviders(pickup: GeoPoint, searching: boolean): void {
    this.loadingProviders.set(true);
    this.error.set(null);

    if (searching) {
      this.phase.set('searching');
    }

    this.dispatchMockService
      .getProviders(pickup)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (providers) => {
          this.providers.set(providers);
          this.loadingProviders.set(false);
          if (searching) {
            this.phase.set('providers');
          }
        },
        error: () => {
          this.error.set('Impossible de charger les dépanneurs simulés.');
          this.loadingProviders.set(false);
          if (searching) {
            this.phase.set('idle');
          }
        },
      });
  }
}
