import { Injectable } from '@angular/core';
import { Observable, delay, interval, map, of, take } from 'rxjs';
import { DispatchProvider, GeoPoint } from '../models/dispatch.models';

const PROVIDER_NAMES = [
  'Karim Assistance',
  'SOS Pneus',
  'Batterie Express',
  'Remorquage Tunis',
  'Auto Secours',
  'Mécanique Mobile',
];

const PROVIDER_VEHICLES = [
  'Dépanneuse plateau',
  'Fourgon pneus',
  'Van batterie',
  'Dépanneuse légère',
  'Assistance mobile',
  'Atelier mobile',
];

const PROVIDER_RATINGS = [4.8, 4.7, 4.9, 4.6, 4.8, 4.5];

const PROVIDER_OFFSETS: ReadonlyArray<GeoPoint> = [
  { lat: 0.004, lng: 0.006 },
  { lat: -0.006, lng: 0.004 },
  { lat: 0.012, lng: -0.008 },
  { lat: -0.015, lng: -0.004 },
  { lat: 0.02, lng: 0.012 },
  { lat: -0.022, lng: 0.016 },
];

@Injectable({ providedIn: 'root' })
export class DispatchMockService {
  getProviders(pickup: GeoPoint): Observable<DispatchProvider[]> {
    const providers = PROVIDER_OFFSETS.map((offset, index) => {
      const position: GeoPoint = {
        lat: pickup.lat + offset.lat,
        lng: pickup.lng + offset.lng,
      };
      const distanceKm = this.haversineDistance(pickup, position);

      return {
        id: `provider-${index + 1}`,
        name: PROVIDER_NAMES[index],
        vehicle: PROVIDER_VEHICLES[index],
        rating: PROVIDER_RATINGS[index],
        phone: `+216 70 000 ${100 + index}`,
        position,
        distanceKm: Math.round(distanceKm * 10) / 10,
        etaMinutes: Math.max(3, Math.round(distanceKm * 3.2)),
      };
    }).sort((first, second) => first.distanceKm - second.distanceKm);

    return of(providers).pipe(delay(650));
  }

  trackProvider(provider: DispatchProvider, target: GeoPoint, steps = 18): Observable<GeoPoint> {
    const start = provider.position;

    return interval(900).pipe(
      take(steps),
      map((step) => {
        const progress = (step + 1) / steps;
        const eased = 1 - Math.pow(1 - progress, 2);

        return {
          lat: start.lat + (target.lat - start.lat) * eased,
          lng: start.lng + (target.lng - start.lng) * eased,
        };
      }),
    );
  }

  private haversineDistance(first: GeoPoint, second: GeoPoint): number {
    const earthRadiusKm = 6371;
    const latitudeDelta = this.toRadians(second.lat - first.lat);
    const longitudeDelta = this.toRadians(second.lng - first.lng);
    const firstLatitude = this.toRadians(first.lat);
    const secondLatitude = this.toRadians(second.lat);

    const a =
      Math.sin(latitudeDelta / 2) * Math.sin(latitudeDelta / 2) +
      Math.cos(firstLatitude) *
        Math.cos(secondLatitude) *
        Math.sin(longitudeDelta / 2) *
        Math.sin(longitudeDelta / 2);

    return earthRadiusKm * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  private toRadians(value: number): number {
    return (value * Math.PI) / 180;
  }
}
