FROM nexus.ria.ee:8500/openjdk:8-jdk

ARG JAR_FILE
COPY ${JAR_FILE} app.jar

ENTRYPOINT java $JAVA_OPTS -jar /app.jar
