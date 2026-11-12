import { z } from 'zod';

const currentYear = new Date().getFullYear();

export const createVehicleSchema = z.object({
  licensePlate: z
    .string()
    .trim()
    .min(1, "L'immatriculation est obligatoire")
    .max(20)
    .transform((value) => value.toUpperCase()),
  brand: z.string().trim().min(1, 'La marque est obligatoire').max(50),
  model: z.string().trim().min(1, 'Le modèle est obligatoire').max(50),
  year: z.number().int().min(1900).max(currentYear + 1),
  mileage: z.number().int().nonnegative().default(0),
  ownerName: z.string().trim().max(100).optional(),
});

export const updateVehicleSchema = createVehicleSchema.partial();

export type CreateVehicleInput = z.infer<typeof createVehicleSchema>;
export type UpdateVehicleInput = z.infer<typeof updateVehicleSchema>;

export interface Vehicle extends CreateVehicleInput {
  id: string;
  createdAt: Date;
  updatedAt: Date;
}
