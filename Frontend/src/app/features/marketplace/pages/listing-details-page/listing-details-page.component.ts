import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import {
  ITEM_CONDITION_LABELS,
  LISTING_CATEGORY_LABELS,
  Listing,
  ListingStatus,
} from '../../../../core/models/listing.model';
import { BUYER_TYPE_OPTIONS, BuyerType, Order } from '../../../../core/models/order.model';
import { ListingService } from '../../../../core/services/listing.service';
import { OrderService } from '../../../../core/services/order.service';
import { apiErrorMessage } from '../../../../core/utils/http-error';
import { StatusBadgeComponent } from '../../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-listing-details-page',
  standalone: true,
  imports: [DatePipe, DecimalPipe, ReactiveFormsModule, RouterLink, StatusBadgeComponent],
  templateUrl: './listing-details-page.component.html',
  styleUrl: './listing-details-page.component.css',
})
export class ListingDetailsPageComponent implements OnInit {
  private readonly listingService = inject(ListingService);
  private readonly orderService = inject(OrderService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly listingId = Number(this.route.snapshot.paramMap.get('id'));
  readonly buyerTypeOptions = BUYER_TYPE_OPTIONS;

  readonly listing = signal<Listing | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly deleting = signal(false);

  readonly orderSubmitting = signal(false);
  readonly orderError = signal<string | null>(null);
  readonly createdOrder = signal<Order | null>(null);

  readonly categoryLabel = computed(() => {
    const listing = this.listing();
    return listing ? LISTING_CATEGORY_LABELS[listing.category] : '';
  });

  readonly conditionLabel = computed(() => {
    const listing = this.listing();
    return listing ? ITEM_CONDITION_LABELS[listing.condition] : '';
  });

  readonly canOrder = computed(() => {
    const listing = this.listing();
    return (
      listing !== null &&
      listing.quantity > 0 &&
      listing.status !== 'SOLD' &&
      listing.status !== 'REJECTED'
    );
  });

  readonly orderForm = this.formBuilder.nonNullable.group({
    buyerId: ['buyer-demo', [Validators.required, Validators.maxLength(64)]],
    buyerType: ['INDIVIDUAL' as BuyerType, [Validators.required]],
    quantity: [1, [Validators.required, Validators.min(1)]],
  });

  ngOnInit(): void {
    if (!Number.isFinite(this.listingId)) {
      this.error.set('Annonce introuvable.');
      this.loading.set(false);
      return;
    }

    this.listingService
      .getListing(this.listingId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (listing) => {
          this.listing.set(listing);
          this.orderForm.controls.quantity.setValidators([
            Validators.required,
            Validators.min(1),
            Validators.max(listing.quantity),
          ]);
          this.orderForm.controls.quantity.updateValueAndValidity();
          this.loading.set(false);
        },
        error: (error) => {
          this.error.set(apiErrorMessage(error, 'Impossible de charger cette annonce.'));
          this.loading.set(false);
        },
      });
  }

  submitOrder(): void {
    const listing = this.listing();

    if (!listing || this.orderForm.invalid || !this.canOrder()) {
      this.orderForm.markAllAsTouched();
      return;
    }

    const value = this.orderForm.getRawValue();

    this.orderSubmitting.set(true);
    this.orderError.set(null);
    this.createdOrder.set(null);

    this.orderService
      .createOrder({
        listingId: this.listingId,
        buyerId: value.buyerId.trim(),
        buyerType: value.buyerType,
        quantity: Number(value.quantity),
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (order) => {
          this.createdOrder.set(order);
          this.orderSubmitting.set(false);
          this.orderForm.controls.quantity.setValue(1);
        },
        error: (error) => {
          this.orderError.set(apiErrorMessage(error, 'Impossible de créer la commande.'));
          this.orderSubmitting.set(false);
        },
      });
  }

  deleteListing(): void {
    const listing = this.listing();
    if (!listing) {
      return;
    }

    const confirmed = window.confirm(`Supprimer l’annonce « ${listing.title} » ?`);
    if (!confirmed) {
      return;
    }

    this.deleting.set(true);
    this.error.set(null);

    this.listingService
      .deleteListing(this.listingId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          void this.router.navigate(['/marketplace']);
        },
        error: (error) => {
          this.error.set(
            apiErrorMessage(error, 'Suppression impossible : des commandes sont peut-être liées.'),
          );
          this.deleting.set(false);
        },
      });
  }

  isStatus(status: ListingStatus): boolean {
    return this.listing()?.status === status;
  }
}
