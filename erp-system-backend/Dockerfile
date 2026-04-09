# syntax=docker/dockerfile:1
# Multi-stage: Maven build + JRE runtime (Java 17).
# Build context: erp-system-backend/
# Tag example: docker build -t mayman2020/erp-backend:latest .
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
COPY --from=build /app/target/erp-system-backend.jar /app/app.jar
RUN chown nobody:nogroup /app/app.jar
EXPOSE 8080
USER nobody
CMD ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseContainerSupport", "-XX:+ExitOnOutOfMemoryError", "-Dfile.encoding=UTF-8", "-jar", "/app/app.jar"]
