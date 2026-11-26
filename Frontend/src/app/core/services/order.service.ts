import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Order, OrderCreateRequest } from '../models/order.model';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.marketplaceApiBaseUrl}/api/v1/orders`;

  createOrder(request: OrderCreateRequest): Observable<Order> {
    return this.http.post<Order>(this.baseUrl, request);
  }
}
