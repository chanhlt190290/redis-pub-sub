FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG JAR_FILE="target/publisher.jar"
COPY ${JAR_FILE} app.jar
COPY config.properties application.properties
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.config.location=file:/application.properties","-jar","/app.jar"]