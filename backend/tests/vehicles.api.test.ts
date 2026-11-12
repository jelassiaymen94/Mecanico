import request from 'supertest';
import { beforeEach, describe, expect, it } from 'vitest';
import { createApp } from '../src/app.js';

const validVehicle = { licensePlate: 'ab-123-cd', brand: 'Renault', model: 'Clio', year: 2019 };

describe('API /api/vehicles', () => {
  let app: ReturnType<typeof createApp>;

  beforeEach(() => {
    app = createApp();
  });

  it('GET /api/health renvoie ok', async () => {
    const res = await request(app).get('/api/health');
    expect(res.status).toBe(200);
    expect(res.body.status).toBe('ok');
  });

  it('crée puis récupère un véhicule', async () => {
    const created = await request(app).post('/api/vehicles').send(validVehicle);
    expect(created.status).toBe(201);
    expect(created.body).toMatchObject({ licensePlate: 'AB-123-CD', mileage: 0 });

    const fetched = await request(app).get(`/api/vehicles/${created.body.id}`);
    expect(fetched.status).toBe(200);
    expect(fetched.body.id).toBe(created.body.id);

    const list = await request(app).get('/api/vehicles');
    expect(list.body).toHaveLength(1);
  });

  it('refuse des données invalides (400)', async () => {
    const res = await request(app).post('/api/vehicles').send({ brand: '' });
    expect(res.status).toBe(400);
    expect(res.body.error.code).toBe('VALIDATION_ERROR');
  });

  it('refuse une immatriculation en double (409)', async () => {
    await request(app).post('/api/vehicles').send(validVehicle);
    const res = await request(app).post('/api/vehicles').send(validVehicle);
    expect(res.status).toBe(409);
  });

  it('met à jour et supprime un véhicule', async () => {
    const { body } = await request(app).post('/api/vehicles').send(validVehicle);

    const updated = await request(app).put(`/api/vehicles/${body.id}`).send({ mileage: 42000 });
    expect(updated.status).toBe(200);
    expect(updated.body).toMatchObject({ mileage: 42000, brand: 'Renault' });

    expect((await request(app).delete(`/api/vehicles/${body.id}`)).status).toBe(204);
    expect((await request(app).get(`/api/vehicles/${body.id}`)).status).toBe(404);
  });

  it('renvoie 404 pour une route inconnue', async () => {
    const res = await request(app).get('/api/unknown');
    expect(res.status).toBe(404);
  });
});
