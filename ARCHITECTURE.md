# WasteWatch — System Architecture & Design Specification

## 1. Architectural Philosophy & Overview

WasteWatch follows an enterprise-standard, tiered, clean-layered architecture adhering to strict **Separation of Concerns (SoC)** and the **Single Responsibility Principle (SRP)**.

The end-to-end flow is strictly unidirectional:
```
┌────────────────────────────────────────────────────────┐
│             Frontend (HTML5 / CSS3 / Vanilla JS)       │
└──────────────────────────┬─────────────────────────────┘
                           │ HTTP / JSON (REST API)
┌──────────────────────────▼─────────────────────────────┐
│          Controller Layer (@RestController)            │
│  - Endpoint routing, payload validation, DTO mapping   │
└──────────────────────────┬─────────────────────────────┘
                           │ DTOs / Domain Commands
┌──────────────────────────▼─────────────────────────────┐
│             Service Layer (@Service)                   │
│  - Pure business logic, compliance scoring, workflows   │
└──────────────────────────┬─────────────────────────────┘
                           │ Entity Models
┌──────────────────────────▼─────────────────────────────┐
│           Repository Layer (@Repository)               │
│  - Spring Data JPA, Hibernate, JPQL / Query Methods     │
└──────────────────────────┬─────────────────────────────┘
                           │ JDBC / SQL
┌──────────────────────────▼─────────────────────────────┐
│             Database (H2 Database Engine)              │
│  - Relational persistence (Migratable to PostgreSQL)   │
└────────────────────────────────────────────────────────┘
```

---

## 2. Backend Layer Responsibilities

### 2.1. Controller Layer (`com.wastewatch.controller`)
* **Role**: Primary entry point for HTTP requests.
* **Responsibilities**:
  - Accept and dispatch RESTful requests (`GET`, `POST`, `PUT`, `DELETE`).
  - Perform syntactical and declarative input validation via `@Valid` and Jakarta validation annotations (`@NotBlank`, `@Min`, `@NotNull`).
  - Serialize output to standard HTTP response envelopes (`ResponseEntity<ApiResponse<T>>`).
  - Map incoming DTOs to service method arguments and return outgoing DTOs.
* **Strict Rule**: **Zero business logic in controllers**. Controllers must not perform calculations, score determinations, complex multi-entity transactions, or direct repository invocations.

### 2.2. Service Layer (`com.wastewatch.service`)
* **Role**: Core business engine of the platform.
* **Responsibilities**:
  - Enforce domain rules, compliance algorithms, and operational workflows.
  - Implement the 100-point compliance scoring logic.
  - Manage database transaction boundaries using `@Transactional` (read-only for queries, atomic write for mutations).
  - Orchestrate interactions across multiple repositories.
  - Throw domain-specific exceptions (e.g., `ResourceNotFoundException`, `ComplianceViolationException`, `DuplicateResourceException`) when invariants are broken.

### 2.3. Repository Layer (`com.wastewatch.repository`)
* **Role**: Persistence and data retrieval abstraction.
* **Responsibilities**:
  - Extend Spring Data JPA interfaces (`JpaRepository<T, ID>`).
  - Define custom JPQL/derived query methods (`findByAreaId`, `findByRoomIdOrderByInspectionDateDesc`).
  - Keep persistence logic agnostic of the underlying database engine (H2 in development, PostgreSQL in production).

### 2.4. Entity Layer (`com.wastewatch.entity`)
* **Role**: Relational database mapping models.
* **Responsibilities**:
  - Represent database tables using standard JPA annotations (`@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@ManyToOne`, `@OneToMany`).
  - Enforce relational constraints (`nullable = false`, unique constraints, foreign keys).
  - Contain audit metadata (`createdAt`, `updatedAt`) via `@EntityListeners(AuditingEntityListener.class)`.
  - Maintain encapsulation without exposing internal persistence details directly to the client.

### 2.5. Data Transfer Objects — DTOs (`com.wastewatch.dto`)
* **Role**: Decoupled data contracts for API requests and responses.
* **Responsibilities**:
  - `request/`: Contain incoming client payloads with validation constraints (e.g., `CreateInspectionRequest`, `LoginRequest`).
  - `response/`: Provide stable, sanitized responses tailored to client views (e.g., `InspectionDetailResponse`, `DashboardSummaryResponse`).
  - Prevent entity over-exposure (avoiding circular JSON references and mass assignment vulnerabilities).

### 2.6. Security Architecture (`com.wastewatch.security` & `com.wastewatch.config`)
* **Role**: Stateless Token Authentication and Role-Based Access Control (RBAC).
* **Implemented Components**:
  - `SecurityConfig`: Spring Security 6 `SecurityFilterChain` with stateless session management, CORS configuration allowing local frontend development origins, disabled CSRF for REST APIs, frame options enabled for H2 console, and fine-grained endpoint matchers.
  - `JwtTokenProvider`: JJWT 0.12.5 utility generating signed 256-bit HMAC-SHA256 tokens with configurable expiration (24h default), extracting username/claims, and validating token signatures.
  - `JwtAuthenticationFilter`: Extends `OncePerRequestFilter` to extract `Authorization: Bearer <token>`, resolve claims, and set authenticated `UserPrincipal` into Spring's `SecurityContext`.
  - `JwtAuthenticationEntryPoint`: Catches unauthenticated access and returns a structured 401 Unauthorized JSON error envelope.
  - `CustomUserDetailsService`: Implements `UserDetailsService` to resolve users by either username or email.
  - `UserPrincipal`: Implements `UserDetails` providing role authorities (`ROLE_ADMIN`, `ROLE_VOLUNTEER`).
  - `BCryptPasswordEncoder`: Secure one-way hashing for all persisted passwords.
* **Access Rules**:
  - Public: `/api/health`, `/api/auth/login`, `/h2-console/**`
  - Authenticated: `/api/auth/me`
  - Read Locations (`GET /api/areas/**`, `GET /api/buildings/**`, `GET /api/rooms/**`): `ROLE_ADMIN`, `ROLE_VOLUNTEER`
  - Manage Locations (`POST`, `PUT`, `DELETE` on `/api/areas/**`, `/api/buildings/**`, `/api/rooms/**`): `ROLE_ADMIN` only

### 2.7. Exception Handling (`com.wastewatch.exception`)
* **Role**: Centralized, robust error handling across all REST endpoints.
* **Responsibilities**:
  - Use `@RestControllerAdvice` / `GlobalExceptionHandler` to intercept all uncaught exceptions.
  - Standardize error output using a uniform structure:
    ```json
    {
      "timestamp": "2026-10-08T14:30:00Z",
      "status": 404,
      "error": "Not Found",
      "message": "Room with ID 42 was not found",
      "path": "/api/rooms/42"
    }
    ```
  - Intercept `@Valid` validation failures (`MethodArgumentNotValidException`) and return field-specific error maps.

### 2.8. Configuration Layer (`com.wastewatch.config`)
* **Role**: Infrastructure and application-wide beans.
* **Responsibilities**:
  - CORS configuration (`CorsConfigurationSource`) for smooth frontend-backend interaction.
  - JPA Auditing configuration (`@EnableJpaAuditing`).
  - OpenAPI / Swagger documentation configuration (if adopted later).
  - WebMvc static resource handler configurations.

---

## 3. Frontend Architecture (Vanilla Web Stack)

### 3.1. Design Philosophy
- **Framework-Free**: Developed strictly using **HTML5**, **CSS3**, and **Vanilla JavaScript (ES6+)**. No React, Angular, Vue, or heavy dependencies.
- **Modern & Premium Aesthetics**:
  - Tailored color palette with crisp contrast, CSS custom properties (variables), subtle gradients, and glassmorphism accents.
  - Responsive, mobile-first design for volunteer inspection forms, and desktop-first data-dense layout for the admin analytics dashboard.
  - Fluid micro-animations and micro-interactions for feedback on submit, error, and status changes.
- **Component-Driven Modular CSS**:
  - Core design tokens (colors, typography, spacing, shadows, border-radii) defined in `variables.css`.
  - Reset and global base styles in `base.css`.
  - Component-specific stylesheets (cards, tables, modals, badges, forms) organized cleanly.

### 3.2. Frontend Directory Structure
```
frontend/
├── index.html                   # Landing / Login portal
├── admin/
│   ├── dashboard.html           # Admin analytics dashboard
│   ├── areas.html               # Area & Building management
│   ├── inspections.html         # Inspection records & history
│   ├── violations.html          # Violation tracking & resolution
│   └── map.html                 # Geographic map & hotspots
├── volunteer/
│   ├── inspect.html             # Field inspection submission workflow
│   ├── history.html             # Volunteer self-inspection history
│   └── tasks.html               # Assigned inspection targets
├── css/
│   ├── variables.css            # Design tokens & color system
│   ├── base.css                 # Base resets & typography
│   ├── components.css           # Buttons, cards, badges, modals, forms
│   ├── layout.css               # Navigation, sidebar, responsive grid
│   └── views/                   # View-specific styles
│       ├── dashboard.css
│       └── inspection.css
├── js/
│   ├── api/
│   │   ├── client.js            # Standard Fetch wrapper (auth headers, error handling)
│   │   ├── auth.api.js          # Authentication endpoints
│   │   ├── inspection.api.js    # Inspection endpoints
│   │   └── dashboard.api.js     # Analytics endpoints
│   ├── utils/
│   │   ├── formatters.js        # Date, score, and currency formatting
│   │   └── notifications.js     # Toast & modal notification triggers
│   └── views/
│       ├── dashboard.js         # Dashboard view controller
│       └── inspect.js           # Inspection form view controller
└── assets/
    ├── icons/                   # SVG icons
    └── images/                  # Static graphics
```

---

## 4. Project Root Layout & Module Separation

The repository is structured to maintain clear separation between the Java Spring Boot service and the Vanilla JS client:

```
Waste watch/
├── PROJECT_CONTEXT.md           # Product definition, vision & roadmap
├── DEVELOPMENT_STATUS.md        # Current phase, status & next agent handoff
├── ARCHITECTURE.md              # Architectural specification & principles
├── DATABASE_DESIGN.md           # Database schema & entity specifications
├── API_DOCUMENTATION.md         # REST API contracts & payload schemas
├── CHANGELOG.md                 # Chronological development record
│
├── backend/                     # Spring Boot application root (Maven)
│   ├── pom.xml                  # Maven project descriptor
│   └── src/
│       ├── main/
│       │   ├── java/com/wastewatch/
│       │   └── resources/
│       │       └── application.yml
│       └── test/
│
└── frontend/                    # Vanilla Web frontend client
    ├── index.html
    ├── css/
    ├── js/
    └── assets/
```

### Hosting Strategy:
- **Development**: The backend runs via Maven on `http://localhost:8080`, exposing `/api/**`. The frontend can either be served as static assets from Spring Boot (`src/main/resources/static/`) or served via any lightweight HTTP server with Spring Boot CORS enabled.
- **Academic & Production Deployment**: For academic review, Spring Boot can package the frontend within its static resources JAR, allowing single-command startup (`java -jar wastewatch.jar`). In a cloud SaaS environment, frontend static files can be distributed via CDN/Nginx while the Spring Boot backend runs as a containerized REST service.

---

## 5. Analytics & Decision Support Engine (Phase 10)

WasteWatch implements an authoritative backend analytics engine (`AnalyticsService`) to prevent unverified client-side calculations and provide mathematically sound municipal decision intelligence.

### 5.1. Standardized Area Health Score Formula

$$\text{Area Health Score} = (0.70 \times \text{Avg Compliance}) + (0.20 \times \text{Inspection Coverage \%}) + (0.10 \times \text{Violation Resolution Rate \%})$$

* **Segregation Compliance Weight (70%)**: The average score of all valid inspections conducted in the area.
* **Inspection Coverage Weight (20%)**: $\frac{\text{Inspected Rooms}}{\text{Total Registered Rooms}} \times 100$. Enforces audit breadth.
* **Violation Resolution Rate Weight (10%)**: $\frac{\text{Resolved Violations}}{\text{Total Violations}} \times 100$ (defaults to 100% if 0 violations have been triggered).

### 5.2. Confidence Rating Guard

To prevent statistical distortion (e.g. an area with 1 inspected room scoring 98% claiming top performance over an area with 90 inspected rooms scoring 85%), WasteWatch computes a **Confidence Rating**:
* **HIGH Confidence**: $\text{Coverage} \ge 75\%$
* **MEDIUM Confidence**: $40\% \le \text{Coverage} < 75\%$
* **LOW Confidence**: $\text{Coverage} < 40\%$

### 5.3. Hotspot & Repeat Offender Engine

* **Repeat Offender Rooms**: Flags rooms with $\ge 2$ failed audits ($\text{score} < 40$) or open unresolved violations with $\text{CRITICAL}$, $\text{HIGH}$, and $\text{MEDIUM}$ risk tiers.
* **Problem Buildings**: Identifies buildings with $\text{average score} < 50$ or $\text{poor room rate} \ge 30\%$.
* **Period-over-Period Improvements**: Computes windowed score and coverage deltas to highlight successful corrective interventions.
