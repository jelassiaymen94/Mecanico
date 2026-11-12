import { type FormEvent, useCallback, useEffect, useState } from 'react';
import { type Vehicle, vehiclesApi } from './vehicles.api';

const emptyForm = { licensePlate: '', brand: '', model: '', year: '', mileage: '' };

export function VehiclesPage() {
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    try {
      setVehicles(await vehiclesApi.list());
      setError(null);
    } catch (e) {
      setError((e as Error).message);
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    try {
      await vehiclesApi.create({
        licensePlate: form.licensePlate,
        brand: form.brand,
        model: form.model,
        year: Number(form.year),
        mileage: Number(form.mileage || 0),
      });
      setForm(emptyForm);
      await refresh();
    } catch (e) {
      setError((e as Error).message);
    }
  }

  async function handleDelete(id: string) {
    try {
      await vehiclesApi.remove(id);
      await refresh();
    } catch (e) {
      setError((e as Error).message);
    }
  }

  const field = (name: keyof typeof emptyForm, placeholder: string, type = 'text') => (
    <input
      type={type}
      placeholder={placeholder}
      value={form[name]}
      required={name !== 'mileage'}
      onChange={(e) => setForm({ ...form, [name]: e.target.value })}
    />
  );

  return (
    <>
      <section className="card">
        <h2>Ajouter un véhicule</h2>
        <form className="vehicle-form" onSubmit={handleSubmit}>
          {field('licensePlate', 'Immatriculation')}
          {field('brand', 'Marque')}
          {field('model', 'Modèle')}
          {field('year', 'Année', 'number')}
          {field('mileage', 'Kilométrage', 'number')}
          <button type="submit">Ajouter</button>
        </form>
        {error && <p className="error">{error}</p>}
      </section>

      <section className="card">
        <h2>Véhicules ({vehicles.length})</h2>
        {vehicles.length === 0 ? (
          <p>Aucun véhicule enregistré.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Immatriculation</th>
                <th>Marque</th>
                <th>Modèle</th>
                <th>Année</th>
                <th>Km</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {vehicles.map((v) => (
                <tr key={v.id}>
                  <td>{v.licensePlate}</td>
                  <td>{v.brand}</td>
                  <td>{v.model}</td>
                  <td>{v.year}</td>
                  <td>{v.mileage}</td>
                  <td>
                    <button className="danger" onClick={() => handleDelete(v.id)}>
                      Supprimer
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </>
  );
}
