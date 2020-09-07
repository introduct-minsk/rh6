#!/bin/bash
apt-get install -y openjdk-8-jdk 
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/

cd infra/siva

wget https://nexus.ria.ee/repository/raw-public/open-eid/SiVa/archive/release-3.2.2.tar.gz 
tar -xvf release-3.2.2.tar.gz
rm -f release-3.2.2.tar.gz
cd SiVa-release-3.2.2/
./mvnw clean package -DskipTests
cd ..

docker build -t siva-web-app:latest --build-arg JAR_FILE=SiVa-release-3.2.2/siva-parent/siva-webapp/target/siva-webapp-3.2.2-exec.jar .
docker build -t xroad-validation-service:latest --build-arg JAR_FILE=SiVa-release-3.2.2/validation-services-parent/xroad-validation-service/target/xroad-validation-service-3.2.2-exec.jar .

cd ../..
