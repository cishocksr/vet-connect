# Multi-stage build for Spring Boot application

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN ./mvnw dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for health checks (lighter than wget)
RUN apk add --no-cache curl

# Create uploads directory BEFORE switching to non-root user
RUN mkdir -p /app/uploads/profile-pictures

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Give ownership of uploads directory to spring user
RUN chown -R spring:spring /app/uploads

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership of app.jar
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8080

# Health check using curl instead of wget
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]