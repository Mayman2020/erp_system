# syntax=docker/dockerfile:1
# Build: Maven + JDK (works even if wrapper JAR is missing).
# Runtime: JRE only.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn -B clean package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
COPY --from=build /app/target/erp-system-backend.jar /app/app.jar
EXPOSE 8080
CMD ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseContainerSupport", "-XX:+ExitOnOutOfMemoryError", "-Dfile.encoding=UTF-8", "-jar", "/app/app.jar"]
