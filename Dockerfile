# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copies only files needed to resolve dependencies first for better layer caching.
COPY pom.xml ./
RUN mvn -B dependency:go-offline

# Copies the rest of the source and builds the runnable jar.
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Copy the packaged jar from the build stage.
COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080
CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]