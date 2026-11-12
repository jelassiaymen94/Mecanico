# Mecanico

Plateforme de mise en relation entre conducteurs, flottes d'entreprise et prestataires d'entretien et de dépannage automobile.

> Module AWD – Année universitaire 2026-2027 – Mini-projet (Validation 1)

## Équipe
| Étudiant | Microservice |
|---|---|
| [Nom Prénom] | MS1 – Auth, Fleet & Vehicle Service |
| [Nom Prénom] | MS2 – [à compléter] |
| [Nom Prénom] | MS3 – [à compléter] |
| [Nom Prénom] | MS4 – [à compléter] |
| [Nom Prénom] | MS5 – [à compléter] |

## Sujet
[À compléter : problème réel, acteurs (conducteur, prestataire, gestionnaire de flotte, administrateur), principales fonctionnalités.]

## Architecture
Front-end, API Gateway, Eureka, Config Server, Keycloak, microservices (une base de données par service), Docker.
Diagramme d'architecture : `Documentation/Diagrams/`.

## Structure du dépôt
```
Mecanico/
├── Backend/          un projet par microservice
├── Frontend/
├── Documentation/
│   ├── Presentation/
│   ├── Diagrams/
│   └── MS1-auth-fleet-vehicle.md
└── README.md
```

## Microservices
### MS1 – Auth, Fleet & Vehicle Service
- Port 8081 · MySQL (`mecanico_user_fleet_db`) · Keycloak (JWT)
- Lancement :
  ```bash
  cd Backend/auth-fleet-vehicle-service
  docker compose up -d
  ./mvnw spring-boot:run
  ```
- Documentation : `Documentation/MS1-auth-fleet-vehicle.md`

### MS2 à MS5
[À compléter par les autres membres.]

## Lancement global
[À compléter : docker compose d'infrastructure (Keycloak, broker, bases de données).]
