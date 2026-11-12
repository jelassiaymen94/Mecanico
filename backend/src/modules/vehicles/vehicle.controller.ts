import type { Request, Response } from 'express';
import { createVehicleSchema, updateVehicleSchema } from './vehicle.entity.js';
import type { VehicleService } from './vehicle.service.js';

type IdParams = { id: string };

export class VehicleController {
  constructor(private readonly service: VehicleService) {}

  list = async (_req: Request, res: Response) => {
    res.json(await this.service.list());
  };

  getById = async (req: Request<IdParams>, res: Response) => {
    res.json(await this.service.getById(req.params.id));
  };

  create = async (req: Request, res: Response) => {
    const input = createVehicleSchema.parse(req.body);
    res.status(201).json(await this.service.create(input));
  };

  update = async (req: Request<IdParams>, res: Response) => {
    const input = updateVehicleSchema.parse(req.body);
    res.json(await this.service.update(req.params.id, input));
  };

  delete = async (req: Request<IdParams>, res: Response) => {
    await this.service.delete(req.params.id);
    res.status(204).send();
  };
}
