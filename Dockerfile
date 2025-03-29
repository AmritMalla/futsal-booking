# Use an official OpenJDK runtime as the base image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Spring Boot application jar to the container
COPY target/futsal-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8090

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
