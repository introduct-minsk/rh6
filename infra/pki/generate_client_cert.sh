#!/bin/sh
SERVICE_NAME=$1
CA_CERT=$2
CA_KEY=$3
if [ -f ./${SERVICE_NAME}-client.key ]; then rm ./${SERVICE_NAME}-client.key; fi
if [ -f ./${SERVICE_NAME}-client.crt ]; then rm ./${SERVICE_NAME}-client.crt; fi
if [ -f ./${SERVICE_NAME}-client.p12 ]; then rm ./${SERVICE_NAME}-client.p12; fi
if [ -f ./${SERVICE_NAME}.jks ]; then rm ./${SERVICE_NAME}.jks; fi

CN=$(head /dev/urandom | tr -dc A-Za-z0-9 | head -c 13 )
openssl req -new -nodes -newkey rsa:2048 -keyout ${SERVICE_NAME}-client.key -sha256 -days 800 -out ${SERVICE_NAME}-client.csr -subj "/C=EE/ST=Tallin/L=Tallin/O=devops/OU=devops/CN=$CN"
openssl x509 -req -in ${SERVICE_NAME}-client.csr -CA ${CA_CERT} -CAkey ${CA_KEY} -CAcreateserial -out ${SERVICE_NAME}-client.crt -days 800 -sha256
openssl pkcs12 -export -in ${SERVICE_NAME}-client.crt -inkey ${SERVICE_NAME}-client.key -name client-cert -out ${SERVICE_NAME}-client.p12 -passout pass:secret
keytool -importkeystore -deststorepass secret -destkeystore ${SERVICE_NAME}.jks -srckeystore ${SERVICE_NAME}-client.p12 -srcstoretype PKCS12 -srcstorepass secret 
