# ===== 1) Build stage =====
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy Maven files and pre-download dependencies
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn -q -DskipTests package

# ===== 2) Runtime stage =====
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the fat jar (adjust JAR name if artifactId/version differs)
COPY --from=builder /app/target/DMS-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
