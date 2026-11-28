export type BuyerType = 'INDIVIDUAL' | 'PROFESSIONAL';

export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';

export interface Order {
  id: number;
  listingId: number;
  listingTitle: string;
  buyerId: string;
  buyerType: BuyerType;
  quantity: number;
  totalPrice: number;
  status: OrderStatus;
  createdAt: string;
  updatedAt: string;
}

export interface OrderCreateRequest {
  listingId: number;
  buyerId: string;
  buyerType: BuyerType;
  quantity: number;
}

export interface OrderUpdateRequest {
  buyerType: BuyerType;
  quantity: number;
  status: OrderStatus;
}

export interface OrderQuery {
  buyerId?: string;
  listingId?: number;
  status?: OrderStatus;
  page?: number;
  size?: number;
}

export const BUYER_TYPE_OPTIONS: ReadonlyArray<{ value: BuyerType; label: string }> = [
  { value: 'INDIVIDUAL', label: 'Particulier' },
  { value: 'PROFESSIONAL', label: 'Professionnel' },
];

export const ORDER_STATUS_LABELS: Record<OrderStatus, string> = {
  PENDING: 'En attente',
  CONFIRMED: 'Confirmée',
  CANCELLED: 'Annulée',
  COMPLETED: 'Terminée',
};
