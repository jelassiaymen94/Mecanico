# Vehicle Passport Service

Service Spring Boot du microservice **MS1 Vehicle Passport** de Mecanico. Il gère les véhicules,
leur carnet d'entretien, leurs documents administratifs, leurs rappels et leur score de santé.

- **Port** : 8082
- **Base** : MySQL `mecanico_vehicle_passport_db`
- **Package** : `com.mecanico.vehiclepassport`
- **Java** : 21+ · **Spring Boot** : 3.5.7
- **Artefact** : `target/vehicle-passport-service-0.0.1-SNAPSHOT.jar`

## Prérequis

- JDK 21+ (`JAVA_HOME` configuré)
- Maven 3.9+
- MySQL 8+ avec la base importée (`Database/02_mecanico_vehicle_passport_db.sql`)
- Le Users service démarré sur 8083 pour obtenir des jetons (ou un JWT valide signé avec le
  même `JWT_SECRET`)

## Configuration

Fichier : `src/main/resources/application.yml`

| Variable | Défaut | Description |
|---|---|---|
| `DB_USER` | `mecanico` | Utilisateur MySQL |
| `DB_PASSWORD` | `mecanico` | Mot de passe MySQL |
| `JWT_SECRET` | secret de dev partagé | **Doit être identique au Users service** |
| `CORS_ORIGINS` | `http://localhost:4200,4201` | Origines autorisées |
| `SCHEDULER_ENABLED` | `true` | Active le cron de scan |
| `DOCUMENT_SCAN_CRON` | `0 0 8 * * *` | Scan quotidien (fuseau Africa/Tunis) |
| `SEED_DEMO_DATA` | `true` | Seed de démonstration si la base est vide |

## Lancement

```bash
mvn clean package
java -jar target/vehicle-passport-service-0.0.1-SNAPSHOT.jar
```

Ou en développement :

```bash
mvn spring-boot:run
```

Vérifications :

- Santé : http://localhost:8082/actuator/health
- Swagger UI : http://localhost:8082/swagger-ui.html
- OpenAPI JSON : http://localhost:8082/v3/api-docs

## Sécurité

- JWT HS256 émis par le Users service, validé par `JwtAuthenticationFilter`.
- Claims : `uid`/`sub`, `email`, `role`, `name`.
- `GET /api/catalog/**`, `/actuator/health`, Swagger et `OPTIONS` sont publics.
- Toutes les autres routes exigent un jeton.
- `POST /api/admin/scan` est réservé à `ROLE_ADMIN`.
- Les contrôleurs véhicules appliquent l'accès propriétaire/gestionnaire via `VehicleAccessService` :
  - `DRIVER` : uniquement ses véhicules ;
  - `FLEET_MANAGER`, `ADMIN` : tous les véhicules.

## API principale

| Groupe | Endpoints |
|---|---|
| Catalogue (public) | `GET /api/catalog`, `/api/catalog/brands`, `/api/catalog/brands/{id}/models`, `/api/catalog/models` |
| Véhicules | `POST/GET /api/vehicles`, `GET/PUT/DELETE /api/vehicles/{id}`, `PATCH /api/vehicles/{id}/mileage` |
| Passeport | `GET /api/vehicles/{id}/passport`, `GET /api/vehicles/{id}/health` |
| Entretien | `GET/POST /api/vehicles/{vehicleId}/records`, `DELETE /api/vehicles/{vehicleId}/records/{recordId}` |
| Documents | `GET/POST /api/vehicles/{vehicleId}/documents`, `GET .../expiring`, `PUT/DELETE .../{documentId}` |
| Rappels | `GET/POST /api/vehicles/{vehicleId}/reminders`, `PATCH .../{reminderId}/status`, `DELETE .../{reminderId}` |
| Dashboard | `GET /api/dashboard/summary`, `GET /api/notifications`, `POST /api/admin/scan`, `GET /api/me/vehicle-ids` |

Le contrat détaillé, les règles métier et le modèle de données sont dans
[`../../Documentation/architecture.md`](../../Documentation/architecture.md).

## Structure des packages

```
com.mecanico.vehiclepassport
├── config        # sécurité, OpenAPI, seeder de démonstration
├── controller    # endpoints REST
├── dto           # requêtes/réponses typées
├── entity        # entités JPA + enums
├── exception     # gestion globale des erreurs
├── repository    # Spring Data JPA
├── security      # filtre JWT, utilisateur courant, handlers 401/403
└── service       # règles métier, score de santé, scan, mapping
```

## Règles métier implémentées

- **Score de santé** déterministe (100 points moins pénalités âge/kilométrage/entretien/documents/rappels).
- **Rappels par défaut** à la création : vidange et rotation des pneus à +10 000 km.
- **Enregistrement SERVICE** : clôt la vidange ouverte et planifie la suivante à +10 000 km.
- **Documents** : notifications à J-30 et J-7, suivi via `notification30Sent` / `notification7Sent`.
- **Rappels** : statut effectif recalculé (`PENDING`, `DUE`, `OVERDUE`) selon le kilométrage ou la date.
- **Kilométrage** strictement croissant.
- **Suppression véhicule** : nettoyage des enregistrements, documents, rappels et notifications liés.

## Build

```bash
mvn clean package
```

Le JAR est produit dans `target/`. Les tests Angular/HTTP manuels ont été réalisés via Swagger et
les deux portails ; le projet ne contient pas encore de tests automatisés.
