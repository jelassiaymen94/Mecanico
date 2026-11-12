import { Router } from 'express';
import type { VehicleController } from './vehicle.controller.js';

// Express 5 transmet automatiquement les erreurs des handlers async au middleware d'erreurs.
export function createVehicleRouter(controller: VehicleController): Router {
  const router = Router();
  router.get('/', controller.list);
  router.get('/:id', controller.getById);
  router.post('/', controller.create);
  router.put('/:id', controller.update);
  router.delete('/:id', controller.delete);
  return router;
}
