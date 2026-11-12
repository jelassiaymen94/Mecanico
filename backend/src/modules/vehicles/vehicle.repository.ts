import { randomUUID } from 'node:crypto';
import type { CreateVehicleInput, UpdateVehicleInput, Vehicle } from './vehicle.entity.js';

/**
 * Contrat d'accès aux données. Le service ne dépend que de cette interface,
 * ce qui permet de remplacer l'implémentation (mémoire, PostgreSQL, ...).
 */
export interface VehicleRepository {
  findAll(): Promise<Vehicle[]>;
  findById(id: string): Promise<Vehicle | undefined>;
  findByLicensePlate(licensePlate: string): Promise<Vehicle | undefined>;
  create(input: CreateVehicleInput): Promise<Vehicle>;
  update(id: string, input: UpdateVehicleInput): Promise<Vehicle | undefined>;
  delete(id: string): Promise<boolean>;
}

export class InMemoryVehicleRepository implements VehicleRepository {
  private readonly vehicles = new Map<string, Vehicle>();

  async findAll(): Promise<Vehicle[]> {
    return [...this.vehicles.values()];
  }

  async findById(id: string): Promise<Vehicle | undefined> {
    return this.vehicles.get(id);
  }

  async findByLicensePlate(licensePlate: string): Promise<Vehicle | undefined> {
    return [...this.vehicles.values()].find((v) => v.licensePlate === licensePlate);
  }

  async create(input: CreateVehicleInput): Promise<Vehicle> {
    const now = new Date();
    const vehicle: Vehicle = { ...input, id: randomUUID(), createdAt: now, updatedAt: now };
    this.vehicles.set(vehicle.id, vehicle);
    return vehicle;
  }

  async update(id: string, input: UpdateVehicleInput): Promise<Vehicle | undefined> {
    const existing = this.vehicles.get(id);
    if (!existing) return undefined;
    const defined = Object.fromEntries(Object.entries(input).filter(([, v]) => v !== undefined));
    const updated: Vehicle = { ...existing, ...defined, updatedAt: new Date() };
    this.vehicles.set(id, updated);
    return updated;
  }

  async delete(id: string): Promise<boolean> {
    return this.vehicles.delete(id);
  }
}
