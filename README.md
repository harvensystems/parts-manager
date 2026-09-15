# Part Manager

An AI-powered inventory and component management system designed for electronics workshops and makerspaces.

Part Manager automates the cataloging and tracking of electronic parts, SMD components, modules, and workshop materials using computer vision and AI recognition.

---

## Features

- **AI-Powered Part Recognition**: Upload or capture photos of components and let AI (Spring AI / OpenAI Vision) extract part numbers, categories, specifications, pinouts, and package types.
- **Inventory & Stock Management**: Real-time tracking of quantities, storage locations (drawers, bins, shelves), low-stock alerts, and pricing.
- **Verification Queue**: Review and verify AI recognition results before saving components into the catalog.
- **Full-Text & Parametric Search**: Quickly find components by name, category, package type, value, or tags.
- **Modern Responsive UI**: Built with Vue 3, Tailwind CSS, Lucide icons, Pinia, and multi-language support (English & Ukrainian).
- **OpenAPI / Swagger Integration**: Auto-generated API documentation and type-safe TypeScript API client generation.

---

## Tech Stack

- **Backend**:
  - [Spring Boot 3.4.x](https://spring.io/projects/spring-boot) (Java 21)
  - [Spring AI](https://spring.io/projects/spring-ai) (OpenAI Vision integration)
  - [MongoDB](https://www.mongodb.com/) (Spring Data MongoDB)
  - [Springdoc OpenAPI / Swagger UI](https://springdoc.org/)
  - [Gradle](https://gradle.org/) (Multi-project build)
- **Frontend**:
  - [Vue 3](https://vuejs.org/) (Composition API, `<script setup>`)
  - [Vite](https://vitejs.dev/) & [TypeScript](https://www.typescriptlang.org/)
  - [Tailwind CSS](https://tailwindcss.com/)
  - [Pinia](https://pinia.vuejs.org/) (State Management)
  - [OpenAPI Generator](https://openapi-generator.tech/) (TypeScript Axios client)

---

## Project Structure

```
part-manager/
├── backend/                  # Spring Boot backend (Java 21, Spring AI, MongoDB)
│   ├── src/main/java/        # Java source code
│   └── src/main/resources/   # Config files (application.yml)
├── frontend/                 # Vue 3 Single Page Application
│   ├── src/                  # Components, views, stores, and API clients
│   └── package.json          # Frontend scripts and dependencies
├── docker-compose.yml        # Docker Compose configuration for MongoDB
├── settings.gradle           # Gradle multi-project configuration
└── build.gradle              # Root Gradle build script
```

---

## Getting Started

### Prerequisites

- **Java**: JDK 21+
- **Node.js**: v18+ and `npm`
- **Docker & Docker Compose**: (for local MongoDB)
- **OpenAI API Key**: (required for AI component recognition)

---

### 1. Start MongoDB

Run MongoDB using Docker Compose:

```bash
docker-compose up -d
```

MongoDB will be available at `mongodb://localhost:27017/part_manager`.

---

### 2. Configure and Run Backend

Set your OpenAI API key (or configure it in `backend/src/main/resources/application.yml`):

```bash
# On Linux / macOS / Git Bash
export OPENAI_API_KEY="your-openai-api-key"

# On Windows (PowerShell)
$env:OPENAI_API_KEY="your-openai-api-key"

# On Windows (CMD)
set OPENAI_API_KEY=your-openai-api-key
```

Run the backend via Gradle:

```bash
# Linux / macOS
./gradlew :backend:bootRun

# Windows
.\gradlew.bat :backend:bootRun
```

- **Backend API**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI Docs**: `http://localhost:8080/v3/api-docs`

---

### 3. Run Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend will be running at `http://localhost:3000` (or `http://localhost:5173` depending on port availability).

---

### 4. Generate TypeScript API Client (Optional)

When the backend is running and the OpenAPI definition is updated, regenerate the TypeScript client:

```bash
# Using npm in the frontend directory
cd frontend
npm run generate-api

# Or using Gradle from project root
./gradlew :frontend:generateApi
```

---

## Environment Variables

| Variable | Description | Default |
| :--- | :--- | :--- |
| `MONGODB_URI` | MongoDB connection string | `mongodb://localhost:27017/part_manager` |
| `OPENAI_API_KEY` | OpenAI API Key for vision recognition | `mock-key` |
| `PORT` / `server.port` | Backend port | `8080` |

---

## License

This project is licensed under the [MIT License](LICENSE).
