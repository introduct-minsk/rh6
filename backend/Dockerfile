FROM nexus.ria.ee:8500/openjdk:11.0.5-jdk

ARG JAR_FILE
COPY ${JAR_FILE} app.jar

ENTRYPOINT java $JAVA_OPTS -jar /app.jar
