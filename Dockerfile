# stage 1: build Vue.js
FROM node:lts-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ .
RUN npm run build

# stage 2: build Java Spring Boot (with Vue)
FROM gradle:9.7.1-jdk21 AS backend-build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle .
COPY backend/src ./backend/src
COPY backend/build.gradle ./backend/build.gradle
# copy Vue to folder static on Spring Boot
COPY --from=frontend-build /app/frontend/dist ./backend/src/main/resources/static
RUN gradle clean build --no-daemon -x test

# stafge 3: final image/
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/backend/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]