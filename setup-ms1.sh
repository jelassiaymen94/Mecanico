#!/usr/bin/env bash
# À lancer UNE FOIS à la racine du dépôt Mecanico, puis NE PAS commiter ce script.
set -e

mkdir -p Backend Frontend Documentation/Presentation Documentation/Diagrams
touch Frontend/.gitkeep Documentation/Presentation/.gitkeep

cd Backend
curl https://start.spring.io/starter.tgz \
  -d type=maven-project -d language=java -d javaVersion=21 \
  -d groupId=com.mecanico -d artifactId=auth-fleet-vehicle-service \
  -d name=auth-fleet-vehicle-service -d baseDir=auth-fleet-vehicle-service \
  -d packageName=com.mecanico.authfleet \
  -d dependencies=web,data-jpa,mysql,validation,actuator,lombok,cloud-eureka,cloud-config-client \
  | tar -xzvf -

cd auth-fleet-vehicle-service/src/main/java/com/mecanico/authfleet
mkdir -p config controller dto entity enums repository service messaging exception
touch config/.gitkeep controller/.gitkeep dto/.gitkeep entity/.gitkeep enums/.gitkeep \
      repository/.gitkeep service/.gitkeep messaging/.gitkeep exception/.gitkeep
echo "Projet MS1 créé. Copiez maintenant application.yml, Dockerfile, docker-compose.yml et README.md (voir le guide)."
