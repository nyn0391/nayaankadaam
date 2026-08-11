# Backend README

Backend skeleton (Spring Boot). This module will contain controllers, services, repositories and domain models.

Run locally:

- Ensure Postgres is running and .env is configured
- mvn spring-boot:run -Dspring-boot.run.profiles=dev

Package:

- mvn clean package

Useful goals/next steps:
- Implement authentication module (JWT)
- Add Flyway migrations for full schema
- Add sample controllers and DTOs
