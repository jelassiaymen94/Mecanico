# Guide de contribution — Mecanico

Ce dépôt est partagé par les 5 membres de l'équipe (module AWD). Chaque membre est responsable
d'un microservice (**MS1 … MS5**). La branche `main` est maintenue par le mainteneur du dépôt :
**personne ne pousse directement sur `main`**, tout passe par une Pull Request (PR).

## 1. Règles d'or

1. Toujours partir d'un `main` à jour.
2. Une branche = un sujet (une fonctionnalité, un correctif).
3. Ne travailler que dans **ses** dossiers (voir §3). Pour modifier une partie commune
   (Frontend partagé, gateway, README…), le signaler dans la PR.
4. Le projet doit **compiler et démarrer** avant d'ouvrir une PR.
5. Ne jamais committer de secrets (`.env`, mots de passe), ni `node_modules/`, `target/`, `dist/`.

## 2. Workflow Git

```bash
# 1. Récupérer la dernière version de main
git checkout main
git pull origin main

# 2. Créer sa branche
git checkout -b ms3/paiement-commandes

# 3. Travailler, puis committer régulièrement
git add .
git commit -m "feat(marketplace): ajout du paiement des commandes"

# 4. Avant d'ouvrir la PR : récupérer les nouveautés de main
git fetch origin
git merge origin/main        # résoudre les conflits éventuels, puis committer

# 5. Pousser et ouvrir la PR vers main sur GitHub
git push -u origin ms3/paiement-commandes
```

### Nommage des branches

`ms<N>/<sujet-court>` en minuscules, avec des tirets. Ajouter la clé Jira si elle existe.

| Exemple | Usage |
|---|---|
| `ms1/rappels-entretien` | Fonctionnalité du MS1 |
| `ms3/SAM1-12-filtre-annonces` | Fonctionnalité liée au ticket Jira `SAM1-12` |
| `fix/ms2-login-expire` | Correctif |
| `docs/architecture-ms4` | Documentation uniquement |

### Messages de commit

Format [Conventional Commits](https://www.conventionalcommits.org/fr/) : `type(portée): description`.

- Types : `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `style`.
- Portée : le microservice ou la zone (`passport`, `marketplace`, `frontend`, `gateway`…).
- Exemples : `feat(passport): calcul du score de santé`, `fix(frontend): menu mobile`.

## 3. Où placer son code

```
Backend/<nom>-service/                 # un projet Spring Boot par microservice
Frontend/src/app/features/<feature>/   # pages Angular de son microservice
Database/0<N>_mecanico_<nom>_db.sql    # script SQL de sa base (une base par service)
Documentation/                         # architecture, diagrammes (ex. ms<N>-class-diagram.mmd)
```

- **Backend** : Java 21 / Spring Boot 3.5. Chaque service a son propre `pom.xml`, son
  `application.yml` et sa propre base MySQL. Ne pas accéder à la base d'un autre service :
  passer par son API.
- **Frontend** : il n'y a **qu'une seule application Angular** (`Frontend/`, Angular 21,
  standalone, Bootstrap 5). Ajouter ses pages dans `features/<feature>/`, déclarer ses routes
  dans `app.routes.ts`, l'entrée de menu dans `layout/shell.ts` et l'URL de son API dans
  `src/environments/environment.ts`. Ne pas créer un second projet Angular.
- **Styles** : réutiliser Bootstrap. Si une feature a ses propres styles globaux, les
  limiter à un conteneur (voir `src/styles/_marketplace.scss` et `layout/marketplace-scope.ts`).

### Ports réservés

| Port | Service |
|---|---|
| 4200 | Portail Users (dépôt `Mecanico-Users`) |
| 4201 | Frontend Angular (ce dépôt) |
| 8081 | MS3 — Marketplace |
| 8082 | MS1 — Vehicle Passport |
| 8083 | Users service (dépôt `Mecanico-Users`) |
| 8084 | API Gateway |
| 8761 | Eureka |
| 8180 | Keycloak (Docker) |
| 3307 | MySQL (Docker) |
| 27017 | MongoDB (Docker) |
| 5672 / 15672 | RabbitMQ / interface web (Docker) |

Un nouveau service prend le prochain port libre (`8085`, `8086`…) : l'ajouter dans ce tableau
et dans le `README.md` dans la même PR.

## 4. Infrastructure Docker

L'infrastructure commune (MySQL, MongoDB, RabbitMQ, Keycloak, Eureka, API Gateway) est décrite
dans `docker-compose.yml`. Personne n'a besoin d'installer ces outils sur sa machine : **Docker
Desktop** (ou Docker Engine + Compose v2) suffit. Prévoir 8 Go de RAM pour la plateforme complète.

### Configuration

```bash
cp .env.example .env     # facultatif : mots de passe, ports, limites mémoire
```

Ne jamais committer `.env`. Toute nouvelle variable est ajoutée à `.env.example` avec sa
valeur par défaut, et `docker-compose.yml` l'utilise sous la forme `${VARIABLE:-défaut}`.

### Profils : ne lancer que ce dont on a besoin

| Commande | Ce qui démarre |
|---|---|
| `docker compose up -d` | Infrastructure commune uniquement |
| `docker compose --profile ms1 up -d --build` | Infrastructure + MS1 |
| `docker compose --profile ms1 --profile ms3 up -d --build` | Infrastructure + MS1 + MS3 |
| `docker compose --profile all up -d --build` | Toute la plateforme + Front-end (démo) |
| `docker compose ps` | État et santé (`healthy`) de chaque conteneur |
| `docker compose logs -f <service>` | Logs d'un conteneur |
| `docker compose --profile all down` | Arrêt (les données sont conservées) |
| `docker compose --profile all down -v` | Arrêt **et suppression des bases** (rejoue l'init MySQL) |

Pour éviter de répéter `--profile`, renseigner `COMPOSE_PROFILES=ms1` (par exemple) dans `.env`.

Chaque conteneur a un *healthcheck* : les microservices attendent que MySQL, RabbitMQ et
Eureka soient `healthy` avant de démarrer, et la Gateway attend Eureka et Keycloak.

### Développer son microservice dans l'IDE

1. Lancer l'infrastructure : `docker compose up -d`.
2. Lancer son service depuis l'IDE avec ces variables d'environnement :

   | Variable | Valeur |
   |---|---|
   | `DB_URL` | `jdbc:mysql://localhost:3307/mecanico_<nom>_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true` |
   | `DB_USER` / `DB_PASSWORD` | `mecanico` / `mecanico` |
   | `EUREKA_INSTANCE_HOSTNAME` | `host.docker.internal` |
   | `EUREKA_INSTANCE_PREFER_IP_ADDRESS` | `false` |

   Les deux dernières variables permettent à la Gateway (dans Docker) de joindre le service
   lancé sur la machine hôte. Sans elles, appeler le service directement sur son port.
3. Vérifier qu'il apparaît dans Eureka : http://localhost:8761.

> MySQL écoute sur **3307** (et non 3306) pour ne pas entrer en conflit avec WAMP/XAMPP.

### Ajouter son microservice à l'infrastructure (checklist)

1. **Code** : `Backend/<nom>-service/` avec `spring-boot-starter-actuator`, le client Eureka,
   et `/actuator/health` autorisé sans jeton dans la configuration de sécurité.
2. **Configuration** : dans `application.yml`, lire les variables d'environnement avec une
   valeur par défaut, pour que le service démarre aussi bien dans l'IDE que dans Docker :
   `server.port: ${SERVER_PORT:<port>}`, `${DB_URL:...}`, `${EUREKA_URL:http://localhost:8761/eureka/}`,
   `${KEYCLOAK_ISSUER_URI:...}`, `${KEYCLOAK_JWK_SET_URI:...}`, `${RABBITMQ_HOST:localhost}`.
3. **Dockerfile** : copier celui d'un service existant (build Maven multi-étapes + JRE 21)
   ainsi que son `.dockerignore`.
4. **Base de données** :
   - MySQL : nommer sa base `mecanico_<nom>_db` (l'utilisateur `mecanico` a déjà les droits
     sur `mecanico_*`) et l'ajouter à la liste de `infra/mysql/init/01-create-databases.sh`.
   - MongoDB (MS5) : `mongodb://mongodb:27017/mecanico_<nom>_db`.
   - H2 : rien à faire.
5. **docker-compose.yml** : copier le bloc *MODÈLE* commenté, avec son profil `ms<N>` et le
   profil `all`. Ajouter son port dans `.env.example`.
6. **Gateway** : ajouter sa route (et sa route `/<nom>-service/v3/api-docs`) dans
   `Backend/api-gateway/src/main/resources/application.yml`.
7. **Vérifier** : `docker compose --profile ms<N> up -d --build`, puis `docker compose ps`
   (statut `healthy`), Eureka, et un appel via la Gateway (`http://localhost:8084/api/...`).
8. **Documenter** : port dans le tableau ci-dessus et dans le `README.md`.

## 5. Pull Requests

- **Cible** : `main`. **Titre** : même format que les commits.
- **Description** : ce qui a été fait, comment tester (URL, compte de démo, requête Swagger),
  captures d'écran pour le Frontend, clé Jira éventuelle.
- **Revue** : au moins **1 approbation** d'un autre membre est requise. Les commentaires de
  revue doivent être résolus avant la fusion.
- **Fusion** : faite par le mainteneur. Après la fusion, supprimer sa branche et repartir
  d'un `main` à jour pour le sujet suivant.
- Garder les PR **petites** (idéalement < 500 lignes) : elles sont relues et fusionnées plus vite.

### Checklist avant d'ouvrir une PR

- [ ] Branche à jour avec `main`, sans conflit
- [ ] Backend : `mvn clean verify` (ou `./mvnw clean verify`) passe dans le dossier du service
- [ ] Frontend : `npm run build` passe dans `Frontend/`
- [ ] Nouvelles variables, ports ou scripts SQL documentés dans le `README.md`
- [ ] Aucun secret ni fichier généré committé

## 6. Conflits

En cas de conflit lors du `git merge origin/main` :

1. Ouvrir les fichiers indiqués par `git status`, garder **les deux** modifications si elles
   sont compatibles, puis `git add <fichier>` et `git commit`.
2. Pour `package-lock.json` : reprendre la version de `main`
   (`git checkout --theirs Frontend/package-lock.json`) puis relancer `npm install`.
3. En cas de doute sur le code d'un autre membre : lui demander, ou laisser le mainteneur
   arbitrer dans la PR.

## 7. Besoin d’aide ?

Ouvrir une *Issue* GitHub ou mentionner le mainteneur (`@jelassiaymen94`) dans la PR.
