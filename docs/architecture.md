# Architecture notes

This document explains high-level architecture decisions for the BusGo platform.

- Backend: Spring Boot (Java 21), layered architecture (controller → service → repository)
- Frontend: React + TypeScript + Vite, Material UI for components
- Database: PostgreSQL with migrations (Flyway)
- Cache/Locks: Redis (for seat locks and ephemeral data)
- Authentication: JWT (access + refresh tokens)
- Payment: Provider-agnostic PaymentService interface
- Deployment: Docker + Docker Compose; can be adapted to Kubernetes

Phased work is documented in docs/ and README.
