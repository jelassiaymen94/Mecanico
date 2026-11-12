import { apiRequest } from '../../api/client';

export interface Vehicle {
  id: string;
  licensePlate: string;
  brand: string;
  model: string;
  year: number;
  mileage: number;
  ownerName?: string;
}

export type VehicleInput = Omit<Vehicle, 'id'>;

export const vehiclesApi = {
  list: () => apiRequest<Vehicle[]>('/vehicles'),
  create: (input: VehicleInput) =>
    apiRequest<Vehicle>('/vehicles', { method: 'POST', body: JSON.stringify(input) }),
  remove: (id: string) => apiRequest<void>(`/vehicles/${id}`, { method: 'DELETE' }),
};
