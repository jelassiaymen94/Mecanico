export type ServiceType =
  | 'TOWING'
  | 'FLAT_TIRE'
  | 'BATTERY'
  | 'FUEL'
  | 'LOCKSMITH'
  | 'OTHER';

export type DispatchPhase = 'idle' | 'searching' | 'providers' | 'tracking' | 'arrived';

export interface GeoPoint {
  lat: number;
  lng: number;
}

export interface PickupPoint extends GeoPoint {
  id: string;
  label: string;
  address: string;
}

export interface DispatchRequest {
  serviceType: ServiceType;
  pickup: PickupPoint;
  destination: string;
  vehicle: string;
  description: string;
}

export interface DispatchProvider {
  id: string;
  name: string;
  vehicle: string;
  rating: number;
  phone: string;
  position: GeoPoint;
  distanceKm: number;
  etaMinutes: number;
}

export interface ServiceTypeOption {
  value: ServiceType;
  label: string;
  description: string;
}

export const SERVICE_TYPE_OPTIONS: ReadonlyArray<ServiceTypeOption> = [
  { value: 'TOWING', label: 'Remorquage', description: 'Véhicule immobilisé' },
  { value: 'FLAT_TIRE', label: 'Crevaison', description: 'Changement de roue' },
  { value: 'BATTERY', label: 'Batterie', description: 'Démarrage ou remplacement' },
  { value: 'FUEL', label: 'Carburant', description: 'Livraison sur place' },
  { value: 'LOCKSMITH', label: 'Serrurerie', description: 'Ouverture de véhicule' },
  { value: 'OTHER', label: 'Autre', description: 'Besoin particulier' },
];

export const SERVICE_TYPE_LABELS: Record<ServiceType, string> = {
  TOWING: 'Remorquage',
  FLAT_TIRE: 'Crevaison',
  BATTERY: 'Batterie',
  FUEL: 'Carburant',
  LOCKSMITH: 'Serrurerie',
  OTHER: 'Autre',
};

export const PICKUP_POINTS: ReadonlyArray<PickupPoint> = [
  {
    id: 'tunis-centre',
    label: 'Tunis centre-ville',
    address: 'Avenue Habib Bourguiba, Tunis',
    lat: 36.8065,
    lng: 10.1815,
  },
  {
    id: 'la-marsa',
    label: 'La Marsa',
    address: 'Avenue de la Corniche, La Marsa',
    lat: 36.8782,
    lng: 10.3247,
  },
  {
    id: 'ariana',
    label: 'Ariana',
    address: 'Avenue de l’Union, Ariana',
    lat: 36.8665,
    lng: 10.1642,
  },
  {
    id: 'ben-arous',
    label: 'Ben Arous',
    address: 'Route de Mornag, Ben Arous',
    lat: 36.7531,
    lng: 10.2189,
  },
  {
    id: 'lac-2',
    label: 'Les Berges du Lac 2',
    address: 'Rue du Lac Turkana, Tunis',
    lat: 36.8333,
    lng: 10.2833,
  },
];
