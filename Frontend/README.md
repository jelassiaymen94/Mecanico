# Vehicle Passport Portal (Angular)

Portail Angular du microservice **MS1 Vehicle Passport** de Mecanico.

- Framework : **Angular 21** (standalone components, signals, zoneless)
- UI : Bootstrap 5 + Bootstrap Icons, design system émeraude/teal
- Port de développement : **4201**
- APIs : Users `http://localhost:8083/api` · Passport `http://localhost:8082/api`

## Prérequis

- Node.js 22+ et npm 11+
- Users service démarré sur **8083**
- Vehicle Passport service démarré sur **8082**

## Configuration

`src/environments/environment.ts` :

```ts
export const environment = {
  apiBaseUrl: 'http://localhost:8082/api',        // service Passport
  usersApiBaseUrl: 'http://localhost:8083/api',   // service Users (login)
  appName: 'Mecanico Vehicle Passport',
  usersPortalUrl: 'http://localhost:4200',        // portail Users
};
```

## Lancement

```bash
npm install
npm start -- --port 4201
```

Ouvrir http://localhost:4201.

Build de production :

```bash
npm run build
```

Les artefacts sont générés dans `dist/vehicle-passport-portal/`.

## Authentification

1. La page de connexion appelle `POST /api/auth/login` sur le **Users service**.
2. La session (access token, refresh token, profil) est conservée dans
   `localStorage` sous la clé `mecanico.passport.session`.
3. L'`authInterceptor` ajoute `Authorization: Bearer <token>` à tous les appels du service
   Passport ; une réponse 401 déclenche la déconnexion.
4. Les gardes `authGuard`, `guestGuard` et `adminGuard` protègent les routes.

Aucun compte n'est créé depuis ce portail : l'inscription et l'administration des utilisateurs
se font dans le portail Users (http://localhost:4200).

## Pages

| Route | Description |
|---|---|
| `/login` | Connexion + comptes de démonstration |
| `/dashboard` | KPIs parc, santé moyenne, véhicules, alertes, dépenses, scan admin |
| `/vehicles` | Liste paginée, recherche, filtre statut, suppression |
| `/vehicles/new` | Création avec cascade marque → modèle et aperçu |
| `/vehicles/:id` | Passeport : vue d'ensemble, entretien, documents, rappels, score de santé |
| `/vehicles/:id/edit` | Édition du véhicule |
| `/notifications` | Journal in-app filtrable (tous / documents / rappels) |

## Structure

```
src/app/
├── core/                     # services API, auth, modèles, gardes, toast
│   ├── auth.service.ts       # session + login/refresh/logout via Users API
│   ├── auth.interceptor.ts   # Bearer token
│   ├── passport-api.service.ts
│   ├── models.ts             # DTOs, enums, labels, helpers
│   └── owner-directory.service.ts
├── layout/shell.*            # sidebar, topbar, navigation
├── pages/
│   ├── login/
│   ├── dashboard/
│   ├── notifications/
│   └── vehicles/{vehicle-list,vehicle-form,vehicle-detail}/
├── shared/toast-container.component.ts
├── app.routes.ts
└── styles.scss               # design system émeraude
```

## Comptes de démonstration

Mot de passe : **`Password123!`**

| Email | Rôle | Vue |
|---|---|---|
| `admin@mecanico.tn` | Administrateur | Tous les véhicules + scan |
| `fleet@mecanico.tn` | Gestionnaire de flotte | Tous les véhicules |
| `driver@mecanico.tn` | Conducteur | Corolla + Golf |
| `driver2@mecanico.tn` | Conducteur | Clio + 208 + Tucson |

## Notes d'implémentation

- Composants standalone et lazy-loading par route.
- `ChangeDetectionStrategy.OnPush` et signals pour l'état local.
- Formulaires réactifs typés avec validation côté client.
- Cascade marque → modèle alimentée par `GET /api/catalog/brands/{id}/models`.
- Modales et notifications personnalisées (sans dépendance à Bootstrap JS).
- Budgets Angular ajustés dans `angular.json` pour la page passeport (styles composant).
