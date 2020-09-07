#!/bin/sh
if [ -f ./ca.key ]; then rm ./ca.key; fi
if [ -f ./ca.crt ]; then rm ./ca.crt; fi
openssl req -x509 -new -nodes -newkey rsa:3072 -keyout ./ca.key -sha256 -days 3650 -out ./ca.crt -subj "/C=EE/ST=Tallin/L=Tallin/O=devops/OU=devops/CN=dev"
