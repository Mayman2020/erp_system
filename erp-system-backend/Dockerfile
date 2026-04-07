# syntax=docker/dockerfile:1.6
# Multi-stage: Maven build → slim JRE runtime (no Unix mvnw in repo; Maven image used for reproducible CI)
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache wget

WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /app/target/erp-system-backend-1.0.0-SNAPSHOT.jar app.jar

USER spring:spring

EXPOSE 8080

# Matches server.servlet.context-path=/api/v1 and HealthController @GetMapping("/health")
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD wget -q -O- http://127.0.0.1:8080/api/v1/health | grep -q OK || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
