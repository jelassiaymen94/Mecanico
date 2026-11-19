import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  effect,
  input,
  output,
  viewChild,
} from '@angular/core';
import * as L from 'leaflet';
import { DispatchProvider, GeoPoint } from '../../models/dispatch.models';

@Component({
  selector: 'app-dispatch-map',
  standalone: true,
  template: `<div #mapContainer class="map-container" aria-label="Carte des dépanneurs"></div>`,
  styleUrl: './dispatch-map.component.css',
})
export class DispatchMapComponent implements AfterViewInit, OnDestroy {
  readonly pickup = input.required<GeoPoint>();
  readonly providers = input<DispatchProvider[]>([]);
  readonly selectedProvider = input<DispatchProvider | null>(null);
  readonly assignedProvider = input<DispatchProvider | null>(null);
  readonly trackingPosition = input<GeoPoint | null>(null);
  readonly providerSelected = output<DispatchProvider>();

  private readonly mapContainer = viewChild.required<ElementRef<HTMLDivElement>>('mapContainer');
  private readonly resizeObserver = new ResizeObserver(() => this.map?.invalidateSize());

  private map: L.Map | null = null;
  private providerLayer: L.LayerGroup | null = null;
  private pickupLayer: L.LayerGroup | null = null;
  private trackingLayer: L.LayerGroup | null = null;
  private hasFitted = false;
  private lastPickupKey = '';

  constructor() {
    effect(() => {
      this.pickup();
      this.providers();
      this.selectedProvider();
      this.assignedProvider();
      this.trackingPosition();

      if (this.map) {
        this.render();
      }
    });
  }

  ngAfterViewInit(): void {
    const container = this.mapContainer().nativeElement;
    const pickup = this.pickup();

    this.map = L.map(container, {
      scrollWheelZoom: false,
      zoomControl: true,
    }).setView([pickup.lat, pickup.lng], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(this.map);

    this.providerLayer = L.layerGroup().addTo(this.map);
    this.pickupLayer = L.layerGroup().addTo(this.map);
    this.trackingLayer = L.layerGroup().addTo(this.map);

    this.render();
    this.resizeObserver.observe(container);
    window.setTimeout(() => this.map?.invalidateSize(), 0);
  }

  ngOnDestroy(): void {
    this.resizeObserver.disconnect();
    this.map?.remove();
  }

  private render(): void {
    if (!this.map || !this.providerLayer || !this.pickupLayer || !this.trackingLayer) {
      return;
    }

    const pickup = this.pickup();
    const providers = this.providers();
    const selected = this.selectedProvider();
    const assigned = this.assignedProvider();
    const tracking = this.trackingPosition();

    this.providerLayer.clearLayers();
    this.pickupLayer.clearLayers();
    this.trackingLayer.clearLayers();

    L.circleMarker([pickup.lat, pickup.lng], {
      radius: 12,
      color: '#9a3412',
      fillColor: '#f97316',
      fillOpacity: 1,
      weight: 3,
    })
      .bindPopup('<strong>Point de prise en charge</strong>')
      .addTo(this.pickupLayer);

    for (const provider of providers) {
      if (assigned?.id === provider.id && tracking) {
        continue;
      }

      const isSelected = selected?.id === provider.id;
      const marker = L.circleMarker([provider.position.lat, provider.position.lng], {
        radius: isSelected ? 12 : 9,
        color: isSelected ? '#0f766e' : '#0d9488',
        fillColor: isSelected ? '#14b8a6' : '#5eead4',
        fillOpacity: 0.9,
        weight: isSelected ? 3 : 2,
      });

      marker.bindPopup(
        `<strong>${this.escapeHtml(provider.name)}</strong><br>` +
          `${this.escapeHtml(provider.vehicle)}<br>` +
          `${provider.distanceKm.toFixed(1)} km · ${provider.etaMinutes} min`,
      );
      marker.on('click', () => this.providerSelected.emit(provider));
      marker.addTo(this.providerLayer);
    }

    if (selected) {
      L.polyline(
        [
          [selected.position.lat, selected.position.lng],
          [pickup.lat, pickup.lng],
        ],
        {
          color: '#0f766e',
          weight: 3,
          dashArray: '7 8',
          opacity: 0.75,
        },
      ).addTo(this.trackingLayer);
    }

    if (assigned && tracking) {
      L.polyline(
        [
          [assigned.position.lat, assigned.position.lng],
          [tracking.lat, tracking.lng],
        ],
        {
          color: '#f97316',
          weight: 4,
          opacity: 0.85,
        },
      ).addTo(this.trackingLayer);

      L.circleMarker([tracking.lat, tracking.lng], {
        radius: 12,
        color: '#9a3412',
        fillColor: '#fbbf24',
        fillOpacity: 1,
        weight: 3,
      })
        .bindPopup(
          `<strong>${this.escapeHtml(assigned.name)}</strong><br>En route vers vous`,
        )
        .addTo(this.trackingLayer);
    }

    const pickupKey = `${pickup.lat},${pickup.lng}`;
    if (!this.hasFitted || pickupKey !== this.lastPickupKey) {
      const points: L.LatLngExpression[] = [[pickup.lat, pickup.lng]];

      for (const provider of providers) {
        points.push([provider.position.lat, provider.position.lng]);
      }

      if (points.length > 1) {
        this.map.fitBounds(L.latLngBounds(points).pad(0.2));
      } else {
        this.map.setView([pickup.lat, pickup.lng], 14);
      }

      this.hasFitted = true;
      this.lastPickupKey = pickupKey;
    }
  }

  private escapeHtml(value: string): string {
    const replacements: Record<string, string> = {
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#039;',
    };

    return value.replace(/[&<>"']/g, (character) => replacements[character] ?? character);
  }
}
