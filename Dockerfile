# syntax=docker/dockerfile:1
# Optional: build backend from repo root (same artifact as erp-system-backend/Dockerfile).
# docker build -f Dockerfile -t mayman2020/erp-backend:latest .
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY erp-system-backend/pom.xml ./
COPY erp-system-backend/src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
COPY --from=build /app/target/erp-system-backend.jar /app/app.jar
RUN chown nobody:nogroup /app/app.jar
USER nobody
EXPOSE 8080
CMD ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseContainerSupport", "-XX:+ExitOnOutOfMemoryError", "-Dfile.encoding=UTF-8", "-jar", "/app/app.jar"]
