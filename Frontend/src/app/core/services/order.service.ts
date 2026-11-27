import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../models/listing.model';
import { Order, OrderCreateRequest, OrderQuery, OrderUpdateRequest } from '../models/order.model';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.marketplaceApiBaseUrl}/api/v1/orders`;

  getOrders(query: OrderQuery = {}): Observable<PageResponse<Order>> {
    return this.http.get<PageResponse<Order>>(this.baseUrl, {
      params: this.buildParams(query),
    });
  }

  getOrder(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.baseUrl}/${id}`);
  }

  createOrder(request: OrderCreateRequest): Observable<Order> {
    return this.http.post<Order>(this.baseUrl, request);
  }

  updateOrder(id: number, request: OrderUpdateRequest): Observable<Order> {
    return this.http.put<Order>(`${this.baseUrl}/${id}`, request);
  }

  deleteOrder(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  private buildParams(query: OrderQuery): HttpParams {
    let params = new HttpParams()
      .set('page', String(query.page ?? 0))
      .set('size', String(query.size ?? 20))
      .set('sort', 'createdAt,desc');

    if (query.buyerId?.trim()) {
      params = params.set('buyerId', query.buyerId.trim());
    }
    if (query.listingId !== undefined) {
      params = params.set('listingId', String(query.listingId));
    }
    if (query.status) {
      params = params.set('status', query.status);
    }

    return params;
  }
}
