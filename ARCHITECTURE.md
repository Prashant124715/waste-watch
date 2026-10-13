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
  - Perform syntactical and declarative input validation via `@Valid` and Jakarta validation annotations (`@NotBlank`, `@NotNull`, `@Min`, `@Max`).
  - Serialize output to standard HTTP response envelopes.
  - Map incoming DTOs to service method arguments and return outgoing DTOs.
* **Strict Rule**: **Zero business logic in controllers**. Controllers must not perform calculations, score determinations, complex multi-entity transactions, or direct repository invocations.

### 2.2. Service Layer (`com.wastewatch.service`)
* **Role**: Core business engine of the platform.
* **Responsibilities**:
  - Enforce domain rules, compliance algorithms, and operational workflows.
  - Implement the 100-point compliance scoring logic (`ComplianceScoringService`).
  - Manage database transaction boundaries using `@Transactional` (read-only for queries, atomic write for mutations).
  - Orchestrate interactions across multiple repositories.
  - Detect repeat non-compliance ($\ge 3$ consecutive poor inspections) and raise alerts (`ViolationService`).
  - Compute standardized Area Health Scores and confidence ratings (`AnalyticsService`).
  - Generate multi-dimensional reports and stream RFC-4180 CSV exports (`ReportService`).
  - Record immutable audit trail logs for all administrative mutations (`AuditLogService`).
  - Throw domain-specific exceptions (`ResourceNotFoundException`, `DuplicateResourceException`, `InvalidRequestException`) when invariants are broken.

### 2.3. Repository Layer (`com.wastewatch.repository`)
* **Role**: Persistence and data retrieval abstraction.
* **Responsibilities**:
  - Extend Spring Data JPA interfaces (`JpaRepository<T, ID>`).
  - Define custom JPQL/derived query methods with multi-attribute filtering.
  - Keep persistence logic agnostic of the underlying database engine (H2 in development, PostgreSQL in production).

### 2.4. Entity Layer (`com.wastewatch.entity`)
* **Role**: Relational database mapping models.
* **Entities**:
  - `AppUser`: Authenticated users with roles (`ADMIN`, `VOLUNTEER`) and status (`ACTIVE`, `INACTIVE`).
  - `Area`: Administrative wards and municipal sectors.
  - `Building`: Structures with geographic coordinates and room counts.
  - `Room`: Audit targets with floor level and unique QR code tokens (`qrCodeToken`).
  - `Inspection`: Field audit submissions with 6 segregation booleans, score, classification, and status.
  - `Violation`: Non-compliance incidents with severity (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`) and lifecycle status (`OPEN`, `ACKNOWLEDGED`, `ACTION_REQUIRED`, `RESOLVED`).
  - `Notification`: User notifications with type (`VIOLATION_ALERT`, `REPEAT_VIOLATION`, `COVERAGE_DEFICIT`, `SYSTEM`).
  - `AuditLog`: Immutable audit trail recording actions, entity types, IDs, and metadata.

### 2.5. Data Transfer Objects — DTOs (`com.wastewatch.dto`)
* Decoupled data contracts preventing mass assignment vulnerabilities and circular entity references.
* Segregated into domain modules: `auth`, `area`, `building`, `room`, `inspection`, `violation`, `notification`, `volunteer`, `report`, `analytics`, `map`, `audit`.

### 2.6. Security Architecture (`com.wastewatch.security` & `com.wastewatch.config`)
* Stateless JWT authentication with JJWT 0.12.5 and HMAC-SHA256 signature verification.
* `BCryptPasswordEncoder` (work factor 10) for one-way password hashing.
* Spring Security 6 `SecurityFilterChain` enforcing RBAC (`ROLE_ADMIN`, `ROLE_VOLUNTEER`).
* Strict volunteer isolation: field inspectors can only view and submit their own audits.
* Privilege escalation defense: admin cannot arbitrarily alter other administrator accounts through volunteer management endpoints.

---

## 3. Frontend Architecture (Vanilla Web Stack)

### 3.1. Zero-Framework Philosophy
WasteWatch intentionally avoids third-party frontend frameworks (React, Angular, Vue) to achieve:
- Maximum execution speed and minimal bundle footprint.
- Zero build tools or transpilation steps needed to preview.
- Longevity and standards compliance with pure HTML5, CSS3, and ES6+ JavaScript.

### 3.2. Directory Layout
```
frontend/
├── index.html                   # Login & entry portal
├── package.json                 # Dev static server configuration
├── css/
│   ├── variables.css            # Centralized CSS design tokens (HSL palette)
│   ├── base.css                 # Typography, resets, utility classes
│   ├── layout.css               # Sidebar, topbar, grid shells
│   └── components.css           # Cards, modals, tables, badges, toasts, score rings
├── js/
│   ├── api/
│   │   └── client.js            # Centralized REST client with Bearer auth & error handling
│   └── utils/
│       ├── helpers.js           # Auth state, Toast notifications, formatting, DOM helpers
│       └── sidebar.js           # Dynamic role-based navigation sidebar
├── admin/
│   ├── dashboard.html           # Executive KPI overview & live inspection stream
│   ├── areas.html               # Area, Building & Room hierarchy with QR generator
│   ├── inspections.html         # Historical audit records with criteria breakdown
│   ├── violations.html          # Violation management with status transition & resolution
│   ├── volunteers.html          # Volunteer management & status toggle governance
│   ├── map.html                 # Leaflet.js OpenStreetMap geographic visualization
│   ├── analytics.html           # Decision intelligence console & Area Health Scores
│   ├── reports.html             # Multi-dimensional reporting console & CSV export
│   └── audit-logs.html          # Security audit trail viewer with multi-criteria filters
└── volunteer/
    ├── inspect.html             # Mobile-first QR/manual 3-step audit submission
    └── history.html             # Personal inspection history & performance summary
```

---

## 4. Key Subsystems & Core Workflows

### 4.1. Compliance Scoring Engine
- Implemented in `ComplianceScoringService`.
- Standardized 100-point algorithm across 6 waste separation criteria.
- Invariant enforcement: null-safe evaluation and mixed contamination guard (cannot earn `noMixedWaste` if all streams fail).
- Tier classifications: `EXCELLENT` (80-100), `GOOD` (60-79), `NEEDS_IMPROVEMENT` (40-59), `POOR` (0-39).

### 4.2. QR Code Workflow (Phase 12)
- Server generates deterministic, unique QR tokens formatted as:
  `WASTEWATCH-ROOM-{BUILDING_CODE}-{ROOM_NUMBER}`
- Dedicated safe lookup endpoint `GET /api/rooms/qr/{token}` returns room and building metadata without exposing internal sensitive IDs.
- Admin UI generates visual QR codes client-side with one-click PNG download.
- Volunteer mobile UI provides camera scanning with instant manual identifier fallback.

### 4.3. Violation & Repeat Non-Compliance Engine (Phase 13)
- When an inspection scores <40 (`POOR`), a `Violation` is automatically generated.
- Severity tiers:
  - 0–19: `CRITICAL`
  - 20–39: `HIGH`
  - 40–59: `MEDIUM`
- **Repeat Offender Detection**: If a room receives 3 consecutive `POOR` inspections, it is escalated to `CRITICAL` severity with a `PERSISTENT_NON_COMPLIANCE` flag, triggering administrative notifications.
- Status lifecycle: `OPEN` ➔ `ACKNOWLEDGED` ➔ `ACTION_REQUIRED` ➔ `RESOLVED`.

### 4.4. Multi-Dimensional Reporting & Authentic CSV Export (Phase 14)
- `ReportService` computes dynamic metrics across 6 distinct perspectives: Overview, Areas, Buildings, Coverage, Violations, and Volunteers.
- All reports support dynamic date bounding (`startDate` to `endDate`).
- `GET /api/reports/export/csv` generates authentic, RFC-4180 compliant CSV files with escaped cells and `Content-Disposition` download headers.

### 4.5. Immutable Security Audit Trail (Phase 14)
- `AuditLogService` captures all critical administrative lifecycle actions:
  `USER_LOGIN`, `CREATE_AREA`, `CREATE_BUILDING`, `CREATE_ROOM`, `UPDATE_VOLUNTEER_STATUS`, `RESOLVE_VIOLATION`, `CREATE_INSPECTION`.
- Multi-criteria querying via `AuditLogRepository.findWithFilters(...)` supporting action, entity type, date bounds, and keyword search.
- Zero sensitive authentication tokens or passwords stored.
