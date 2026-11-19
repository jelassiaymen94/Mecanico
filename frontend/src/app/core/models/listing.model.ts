export type ListingCategory =
  | 'ENGINE'
  | 'BRAKES'
  | 'SUSPENSION'
  | 'ELECTRICAL'
  | 'BODY'
  | 'TIRES'
  | 'ACCESSORIES'
  | 'OTHER';

export type ItemCondition = 'NEW' | 'USED';

export type ListingStatus = 'PENDING_VERIFICATION' | 'VERIFIED' | 'REJECTED' | 'SOLD';

export interface Listing {
  id: number;
  sellerId: string;
  title: string;
  description: string;
  category: ListingCategory;
  brand: string | null;
  compatibleModels: string | null;
  condition: ItemCondition;
  price: number;
  quantity: number;
  city: string;
  status: ListingStatus;
  createdAt: string;
  updatedAt: string;
}

export interface ListingRequest {
  sellerId: string;
  title: string;
  description: string;
  category: ListingCategory;
  brand: string | null;
  compatibleModels: string | null;
  condition: ItemCondition;
  price: number;
  quantity: number;
  city: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ListingQuery {
  category?: ListingCategory;
  city?: string;
  status?: ListingStatus;
  sellerId?: string;
  q?: string;
  page?: number;
  size?: number;
}

export const LISTING_CATEGORY_LABELS: Record<ListingCategory, string> = {
  ENGINE: 'Moteur',
  BRAKES: 'Freinage',
  SUSPENSION: 'Suspension',
  ELECTRICAL: 'Électricité',
  BODY: 'Carrosserie',
  TIRES: 'Pneus',
  ACCESSORIES: 'Accessoires',
  OTHER: 'Autre',
};

export const LISTING_CATEGORY_OPTIONS = (
  Object.keys(LISTING_CATEGORY_LABELS) as ListingCategory[]
).map((value) => ({
  value,
  label: LISTING_CATEGORY_LABELS[value],
}));

export const LISTING_STATUS_LABELS: Record<ListingStatus, string> = {
  PENDING_VERIFICATION: 'En attente',
  VERIFIED: 'Vérifiée',
  REJECTED: 'Refusée',
  SOLD: 'Vendue',
};

export const ITEM_CONDITION_LABELS: Record<ItemCondition, string> = {
  NEW: 'Neuf',
  USED: 'Occasion',
};
