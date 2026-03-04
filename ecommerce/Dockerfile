# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy dependency definitions first for better layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build (skip tests for image build; run tests in CI)
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from builder
COPY --from=builder /build/target/ecommerce-*.jar app.jar

# Cloud Run and local default
EXPOSE 8080

# Run as non-root
RUN addgroup -g 1000 appgroup && adduser -u 1000 -G appgroup -D appuser
USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]
