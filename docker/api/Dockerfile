FROM khipu/openjdk17-alpine:latest
COPY build/libs/*.jar app.jar
COPY build/resources/main/properties/env.properties properties/env.properties
ENTRYPOINT ["java", "-jar", "app.jar"]