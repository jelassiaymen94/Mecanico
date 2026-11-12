# Mecanico

Projet universitaire : application web de gestion d'atelier mécanique (clients, véhicules,
rendez-vous, interventions, factures).

📐 L'architecture est décrite dans [`docs/architecture.md`](docs/architecture.md).

## Stack

- **Backend** : Node.js 22, Express 5, TypeScript, Zod — `backend/`
- **Frontend** : React 19, Vite, TypeScript — `frontend/`
- **Base de données** : PostgreSQL 16 (Docker)
- **Tests** : Vitest + Supertest
- **CI** : GitHub Actions — le workflow est fourni dans `ci/github-actions-ci.yml` ; pour l'activer, le copier dans `.github/workflows/ci.yml`

## Démarrage rapide

Prérequis : Node.js ≥ 22.12, npm, (optionnel) Docker.

```bash
cp .env.example .env

# Backend (http://localhost:3000/api)
cd backend
npm install
npm run dev

# Frontend (http://localhost:5173) – dans un autre terminal
cd frontend
npm install
npm run dev
```

### Avec Docker

```bash
docker compose up --build
# Frontend : http://localhost:8080 — API : http://localhost:3000/api
```

## Scripts utiles

| Dossier    | Commande            | Description                  |
|------------|---------------------|------------------------------|
| `backend`  | `npm run dev`       | Serveur avec rechargement    |
| `backend`  | `npm test`          | Tests unitaires et d'API     |
| `backend`  | `npm run build`     | Compilation TypeScript       |
| `frontend` | `npm run dev`       | Serveur de développement     |
| `frontend` | `npm run build`     | Build de production          |

## Endpoints disponibles

| Méthode | URL                    | Description              |
|---------|------------------------|--------------------------|
| GET     | `/api/health`          | État du service          |
| GET     | `/api/vehicles`        | Liste des véhicules      |
| GET     | `/api/vehicles/:id`    | Détail d'un véhicule     |
| POST    | `/api/vehicles`        | Création                 |
| PUT     | `/api/vehicles/:id`    | Mise à jour              |
| DELETE  | `/api/vehicles/:id`    | Suppression              |

## Ajouter un module métier

Copier la structure de `backend/src/modules/vehicles/` (entity → repository → service → controller → routes),
puis l'enregistrer dans `backend/src/app.ts`. Voir la section 4 de la documentation d'architecture.
