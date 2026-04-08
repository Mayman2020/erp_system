# syntax=docker/dockerfile:1
# Build: JDK + Maven Wrapper (reproducible; no global mvn install required).
# Runtime: JRE only. Official openjdk:*-slim is deprecated; Eclipse Temurin is the supported replacement.
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
RUN chmod +x mvnw
COPY src ./src
RUN ./mvnw -B -DskipTests clean package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
COPY --from=build /app/target/erp-system-backend.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseContainerSupport", "-XX:+ExitOnOutOfMemoryError", "-Dfile.encoding=UTF-8", "-jar", "/app/app.jar"]
