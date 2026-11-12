import { VehiclesPage } from './features/vehicles/VehiclesPage';

export default function App() {
  return (
    <div className="layout">
      <header className="header">
        <h1>🔧 Mecanico</h1>
        <p>Gestion d'atelier mécanique</p>
      </header>
      <main className="content">
        <VehiclesPage />
      </main>
    </div>
  );
}
