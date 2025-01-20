FROM openjdk:23-slim


WORKDIR /app

COPY target/microservices-parent-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]