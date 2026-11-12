import { ConflictError, NotFoundError } from '../../shared/errors.js';
import type { CreateVehicleInput, UpdateVehicleInput, Vehicle } from './vehicle.entity.js';
import type { VehicleRepository } from './vehicle.repository.js';

export class VehicleService {
  constructor(private readonly repository: VehicleRepository) {}

  list(): Promise<Vehicle[]> {
    return this.repository.findAll();
  }

  async getById(id: string): Promise<Vehicle> {
    const vehicle = await this.repository.findById(id);
    if (!vehicle) throw new NotFoundError(`Véhicule ${id} introuvable`);
    return vehicle;
  }

  async create(input: CreateVehicleInput): Promise<Vehicle> {
    await this.ensurePlateIsAvailable(input.licensePlate);
    return this.repository.create(input);
  }

  async update(id: string, input: UpdateVehicleInput): Promise<Vehicle> {
    if (input.licensePlate) await this.ensurePlateIsAvailable(input.licensePlate, id);
    const vehicle = await this.repository.update(id, input);
    if (!vehicle) throw new NotFoundError(`Véhicule ${id} introuvable`);
    return vehicle;
  }

  async delete(id: string): Promise<void> {
    const deleted = await this.repository.delete(id);
    if (!deleted) throw new NotFoundError(`Véhicule ${id} introuvable`);
  }

  private async ensurePlateIsAvailable(licensePlate: string, excludeId?: string): Promise<void> {
    const existing = await this.repository.findByLicensePlate(licensePlate);
    if (existing && existing.id !== excludeId) {
      throw new ConflictError(`L'immatriculation ${licensePlate} est déjà enregistrée`);
    }
  }
}
