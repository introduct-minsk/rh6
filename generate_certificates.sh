#!/bin/bash
#Generate certificates for microservices
./infra/pki/generate_client_cert.sh user-service-1 ./infra/pki/ca.crt ./infra/pki/ca.key
./infra/pki/generate_client_cert.sh auth-service-1 ./infra/pki/ca.crt ./infra/pki/ca.key
./infra/pki/generate_client_cert.sh search-service-1 ./infra/pki/ca.crt ./infra/pki/ca.key
./infra/pki/generate_client_cert.sh mailbox-service-1 ./infra/pki/ca.crt ./infra/pki/ca.key
./infra/pki/generate_client_cert.sh user-service-2 ./infra/pki/ca.crt ./infra/pki/ca.key
./infra/pki/generate_client_cert.sh auth-service-2 ./infra/pki/ca.crt ./infra/pki/ca.key
./infra/pki/generate_client_cert.sh search-service-2 ./infra/pki/ca.crt ./infra/pki/ca.key
./infra/pki/generate_client_cert.sh mailbox-service-2 ./infra/pki/ca.crt ./infra/pki/ca.key
mkdir -p backend/client_certs
/bin/cp -f *jks  backend/client_certs/
rm -f *.crt *.csr *.key *.jks *.p12

#Copy ca certificate for loadbalancer
/bin/cp -f ./infra/pki/ca.crt infra/lb/lb_config/ca.crt

./infra/pki/generate_client_cert.sh database ./infra/pki/ca.crt ./infra/pki/ca.key
/bin/cp -f database-client.crt infra/db/cert.crt
/bin/cp -f database-client.key infra/db/cert.key
rm -f *.crt *.csr *.key *.jks *.p12

./infra/pki/generate_client_cert.sh redis ./infra/pki/ca.crt ./infra/pki/ca.key
mkdir -p backend/redis_certs
/bin/cp -f redis-client.crt backend/redis_certs/
/bin/cp -f redis-client.key backend/redis_certs/
/bin/cp -f infra/pki/ca.crt backend/redis_certs/
rm -f *.crt *.csr *.key *.jks *.p12

./infra/pki/generate_client_cert.sh elastic ./infra/pki/ca.crt ./infra/pki/ca.key
mkdir -p backend/elastic_certs
/bin/cp -f elastic-client.crt backend/elastic_certs/
/bin/cp -f elastic-client.key backend/elastic_certs/
/bin/cp -f infra/pki/ca.crt  backend/elastic_certs/
rm -f *.crt *.csr *.key *.jks *.p12
