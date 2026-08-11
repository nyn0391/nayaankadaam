# BusGo — Bus Booking Platform

BusGo is a full-stack bus ticket booking platform scaffolded to implement a production-ready architecture (React frontend + Spring Boot backend + PostgreSQL + Redis). This repository is an initial bootstrap containing project structure, basic configuration, and developer notes to continue implementation in phases.

## What’s included in this bootstrap

- frontend/: Vite + React + TypeScript app scaffold (Material UI chosen)
- backend/: Spring Boot 3.x (Java 21) app skeleton
- database/: SQL migrations folder
- docker/: Dockerfiles
- docker-compose.yml to run frontend, backend, postgres, redis
- docs/: architecture and API/docs placeholders
- .env.example

## Quickstart (development)

Prerequisites: Docker & Docker Compose, JDK 21, Node 18+, Maven

1. Copy .env.example to .env and edit values.
2. Start with Docker Compose:

   docker compose up --build

3. Backend (if running locally):
   - cd backend
   - mvn spring-boot:run -Dspring-boot.run.profiles=dev

4. Frontend (if running locally):
   - cd frontend
   - npm install
   - npm run dev

See docs/ for more information.
