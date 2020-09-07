FROM nexus.ria.ee:8500/postgres:9.6.12

ARG CA_FILE
ARG CERT_FILE
ARG KEY_FILE 

COPY ${CA_FILE} /etc/pki/ca.crt
COPY ${CERT_FILE} /etc/pki/cert.crt
COPY ${KEY_FILE} /etc/pki/cert.key

RUN chown 999:999 /etc/pki/ca.crt
RUN chown 999:999 /etc/pki/cert.crt
RUN chown 999:999 /etc/pki/cert.key
RUN chmod 600 /etc/pki/ca.crt
RUN chmod 600 /etc/pki/cert.crt
RUN chmod 600 /etc/pki/cert.key

ENTRYPOINT ["docker-entrypoint.sh"] 
CMD ["postgres"]
