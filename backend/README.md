# WasteWatch Backend

This directory houses the Java Spring Boot REST API application.

## Planned Technology Stack
- Java 17+
- Spring Boot 3.x
- Spring Web (REST Controllers)
- Spring Data JPA
- Spring Security (RBAC: `ROLE_ADMIN`, `ROLE_VOLUNTEER`)
- H2 Database (development/academic mode, PostgreSQL-ready)
- Apache Maven

## Planned Package Structure
```
src/main/java/com/wastewatch/
├── WasteWatchApplication.java
├── config/             # Security, CORS, JPA Auditing, OpenAPI
├── controller/         # REST API endpoints (No business logic)
├── dto/                # Request & response data transfer objects
├── entity/             # JPA entity definitions (AppUser, Area, Building, Room, Inspection, Violation)
├── exception/          # GlobalExceptionHandler and domain exceptions
├── repository/         # Spring Data JPA repositories
├── security/           # Token / Session authentication filters & providers
└── service/            # Core business logic & 100-point compliance engine
```

Implementation begins in **Phase 2**.
