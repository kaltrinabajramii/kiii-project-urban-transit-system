# Backend Dockerfile for Spring Boot 3.5.5 with Java 17 and Maven

# Stage 1: Build the Spring Boot application
FROM maven:3.8.5-openjdk-17 AS build

# Set working directory for the build stage
WORKDIR /app

# Copy Maven configuration file first for better Docker layer caching
# This allows Docker to cache dependencies if only source code changes
COPY pom.xml .

# Copy source code directory
COPY src ./src

# Build the application using Maven
# -DskipTests speeds up the build by skipping unit tests (tests run in CI pipeline)
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM openjdk:17-jdk-slim

# Set working directory for the runtime stage
WORKDIR /app

# Copy the built JAR file from the build stage to the runtime stage
# Using the exact artifact name from pom.xml: urban-transit-backend-0.0.1-SNAPSHOT.jar
COPY --from=build /app/target/urban-transit-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080 (Spring Boot's default port)
EXPOSE 8080

# Run the Spring Boot application
CMD ["java", "-jar", "app.jar"]
