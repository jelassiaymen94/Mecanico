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

Un nouveau service prend le prochain port libre (`8085`, `8086`…) : l'ajouter dans ce tableau
et dans le `README.md` dans la même PR.

## 4. Pull Requests

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

## 5. Conflits

En cas de conflit lors du `git merge origin/main` :

1. Ouvrir les fichiers indiqués par `git status`, garder **les deux** modifications si elles
   sont compatibles, puis `git add <fichier>` et `git commit`.
2. Pour `package-lock.json` : reprendre la version de `main`
   (`git checkout --theirs Frontend/package-lock.json`) puis relancer `npm install`.
3. En cas de doute sur le code d'un autre membre : lui demander, ou laisser le mainteneur
   arbitrer dans la PR.

## 6. Besoin d'aide ?

Ouvrir une *Issue* GitHub ou mentionner le mainteneur (`@jelassiaymen94`) dans la PR.
