import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { Order } from '../models/order.model';
import { OrderService } from './order.service';

describe('OrderService', () => {
  let service: OrderService;
  let http: HttpTestingController;
  const baseUrl = `${environment.marketplaceApiBaseUrl}/api/v1/orders`;
  const order: Order = {
    id: 7,
    listingId: 3,
    listingTitle: 'Moteur',
    buyerId: 'buyer-1',
    buyerType: 'INDIVIDUAL',
    quantity: 2,
    totalPrice: 200,
    status: 'PENDING',
    createdAt: '2026-01-01T10:00:00Z',
    updatedAt: '2026-01-01T10:00:00Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(OrderService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lists orders with normalized filters and pagination', () => {
    service
      .getOrders({ buyerId: ' buyer-1 ', listingId: 3, status: 'PENDING', page: 1, size: 5 })
      .subscribe();

    const request = http.expectOne(
      (candidate) =>
        candidate.url === baseUrl &&
        candidate.params.get('buyerId') === 'buyer-1' &&
        candidate.params.get('listingId') === '3' &&
        candidate.params.get('status') === 'PENDING' &&
        candidate.params.get('page') === '1' &&
        candidate.params.get('size') === '5' &&
        candidate.params.get('sort') === 'createdAt,desc',
    );
    expect(request.request.method).toBe('GET');
    request.flush({
      content: [],
      page: 1,
      size: 5,
      totalElements: 0,
      totalPages: 0,
      first: false,
      last: true,
    });
  });

  it('gets an order by id', () => {
    service.getOrder(7).subscribe((response) => expect(response).toEqual(order));

    const request = http.expectOne(`${baseUrl}/7`);
    expect(request.request.method).toBe('GET');
    request.flush(order);
  });

  it('creates an order', () => {
    const payload = {
      listingId: 3,
      buyerId: 'buyer-1',
      buyerType: 'INDIVIDUAL' as const,
      quantity: 2,
    };
    service.createOrder(payload).subscribe();

    const request = http.expectOne(baseUrl);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush(order);
  });

  it('updates an order', () => {
    const payload = {
      buyerType: 'PROFESSIONAL' as const,
      quantity: 1,
      status: 'CONFIRMED' as const,
    };
    service.updateOrder(7, payload).subscribe();

    const request = http.expectOne(`${baseUrl}/7`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(payload);
    request.flush({ ...order, ...payload });
  });

  it('deletes an order', () => {
    service.deleteOrder(7).subscribe();

    const request = http.expectOne(`${baseUrl}/7`);
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});
