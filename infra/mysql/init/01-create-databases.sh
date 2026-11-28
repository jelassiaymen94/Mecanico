#!/bin/bash
# -----------------------------------------------------------------------------
# Exécuté une seule fois par le conteneur MySQL, au premier démarrage (volume vide).
# Pour le rejouer : docker compose down -v   (supprime les données !)
#
# Pattern "database-per-service" : une base par microservice.
# L'utilisateur applicatif ($MYSQL_USER, créé par l'image) reçoit les droits sur
# toutes les bases dont le nom commence par "mecanico_" : un nouveau service peut
# donc aussi créer sa base lui-même (createDatabaseIfNotExist=true dans l'URL JDBC).
# -----------------------------------------------------------------------------
set -euo pipefail

# Ajouter ici la base de son microservice (MySQL uniquement).
DATABASES=(
  mecanico_vehicle_passport_db   # MS1 - Vehicle Passport
  mecanico_request_db            # MS2 - Request
  mecanico_marketplace_db        # MS3 - Parts Marketplace
  mecanico_provider_db           # MS4 - Provider
  mecanico_billing_db            # MS6 - Billing & Payment
  mecanico_ai_db                 # MS7 - AI Recommendation
)

APP_USER="${MYSQL_USER:-mecanico}"

{
  for db in "${DATABASES[@]}"; do
    echo "CREATE DATABASE IF NOT EXISTS \`${db}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
  done
  echo "GRANT ALL PRIVILEGES ON \`mecanico\\_%\`.* TO '${APP_USER}'@'%';"
  echo "FLUSH PRIVILEGES;"
} | mysql --protocol=socket -uroot -p"${MYSQL_ROOT_PASSWORD}"

echo "[mecanico] Bases créées : ${DATABASES[*]} (droits mecanico_* pour ${APP_USER})"
