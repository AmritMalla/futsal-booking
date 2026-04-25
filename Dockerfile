# syntax=docker/dockerfile:1.7

# Stage 1: build the JAR
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

# Stage 2: runtime
FROM eclipse-temurin:17-jre-alpine AS runtime
RUN addgroup -S app && adduser -S -G app app
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN chown -R app:app /app && mkdir -p /var/app/uploads && chown -R app:app /var/app/uploads
USER app
EXPOSE 8080
ENV SERVER_PORT=8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
