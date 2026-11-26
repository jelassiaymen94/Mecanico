import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Listing,
  ListingQuery,
  ListingRequest,
  PageResponse,
} from '../models/listing.model';

@Injectable({ providedIn: 'root' })
export class ListingService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.marketplaceApiBaseUrl}/api/v1/listings`;

  getListings(query: ListingQuery = {}): Observable<PageResponse<Listing>> {
    return this.http.get<PageResponse<Listing>>(this.baseUrl, {
      params: this.buildParams(query),
    });
  }

  getListing(id: number): Observable<Listing> {
    return this.http.get<Listing>(`${this.baseUrl}/${id}`);
  }

  createListing(request: ListingRequest): Observable<Listing> {
    return this.http.post<Listing>(this.baseUrl, request);
  }

  updateListing(id: number, request: ListingRequest): Observable<Listing> {
    return this.http.put<Listing>(`${this.baseUrl}/${id}`, request);
  }

  deleteListing(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  private buildParams(query: ListingQuery): HttpParams {
    let params = new HttpParams()
      .set('page', String(query.page ?? 0))
      .set('size', String(query.size ?? 12))
      .set('sort', 'createdAt,desc');

    if (query.category) {
      params = params.set('category', query.category);
    }
    if (query.city?.trim()) {
      params = params.set('city', query.city.trim());
    }
    if (query.status) {
      params = params.set('status', query.status);
    }
    if (query.sellerId?.trim()) {
      params = params.set('sellerId', query.sellerId.trim());
    }
    if (query.q?.trim()) {
      params = params.set('q', query.q.trim());
    }

    return params;
  }
}
