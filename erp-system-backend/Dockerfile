# syntax=docker/dockerfile:1
# Deterministic image for Railway: JAR at a fixed path (avoids missing target/ or wrong WORKDIR after Nixpacks/Railpack).
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/erp-system-backend.jar /app/erp-system-backend.jar
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseContainerSupport", "-XX:+ExitOnOutOfMemoryError", "-Dfile.encoding=UTF-8", "-jar", "/app/erp-system-backend.jar"]
