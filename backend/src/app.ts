import cors from 'cors';
import express, { type Express } from 'express';
import { createHealthRouter } from './modules/health/health.routes.js';
import { VehicleController } from './modules/vehicles/vehicle.controller.js';
import {
  InMemoryVehicleRepository,
  type VehicleRepository,
} from './modules/vehicles/vehicle.repository.js';
import { createVehicleRouter } from './modules/vehicles/vehicle.routes.js';
import { VehicleService } from './modules/vehicles/vehicle.service.js';
import { errorHandler, notFoundHandler } from './shared/middleware/errorHandler.js';

export interface AppDependencies {
  vehicleRepository?: VehicleRepository;
  corsOrigin?: string;
}

/**
 * Composition root : assemble les modules et injecte leurs dépendances.
 * Pour brancher PostgreSQL, il suffit de fournir une autre implémentation de repository.
 */
export function createApp(deps: AppDependencies = {}): Express {
  const app = express();

  app.use(cors({ origin: deps.corsOrigin ?? '*' }));
  app.use(express.json());

  const vehicleService = new VehicleService(
    deps.vehicleRepository ?? new InMemoryVehicleRepository(),
  );

  const api = express.Router();
  api.use('/health', createHealthRouter());
  api.use('/vehicles', createVehicleRouter(new VehicleController(vehicleService)));
  app.use('/api', api);

  app.use(notFoundHandler);
  app.use(errorHandler);

  return app;
}
