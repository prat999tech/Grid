# Builds ONE deployable image: the Angular app is compiled and bundled into the
# Spring Boot jar, so a single service serves the UI + REST + WebSocket.
# Build context is the repo root:  docker build -t capture-grid .

# ---- Stage 1: build the Angular frontend ----
FROM node:22-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build        # production config (relative API/WS URLs)

# ---- Stage 2: build the Spring Boot jar, with the Angular app as static files ----
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /app/backend
COPY backend/pom.xml ./
# Warm the dependency cache for faster rebuilds. Non-fatal: if it can't pre-fetch
# everything, the package step below downloads whatever's missing.
RUN mvn -q -B dependency:go-offline -DskipTests || true
COPY backend/src ./src
# Drop the compiled Angular app into Spring Boot's static folder so it is served at "/".
COPY --from=frontend /app/frontend/dist/realtime-grid/browser/ ./src/main/resources/static/
RUN mvn -q -DskipTests package

# ---- Stage 3: small runtime image ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend /app/backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
