# MS1 – Auth, Fleet & Vehicle Service

Microservice de Mecanico : identités et rôles (Keycloak), véhicules, flottes et carnet d'entretien.

- Port : 8081
- Base de données : MySQL `mecanico_user_fleet_db`
- Stack : Java 21, Spring Boot, Spring Data JPA, Eureka Client, Config Client
- Documentation complète : `../../Documentation/MS1-auth-fleet-vehicle.md`

## Lancement
```bash
docker compose up -d          # démarre MySQL
./mvnw spring-boot:run        # démarre le service
```
Vérification : http://localhost:8081/actuator/health → `UP`

## Docker
```bash
docker build -t mecanico/auth-fleet-vehicle-service .
```

## Structure des packages (`com.mecanico.authfleet`)
`config` · `controller` · `dto` · `entity` · `enums` · `repository` · `service` · `messaging` · `exception`
