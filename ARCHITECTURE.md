# WasteWatch — System Architecture & Design Specification

[![Architecture](https://img.shields.io/badge/Architecture-Tiered%20Clean%20Onward-blue.svg)](#)
[![Security](https://img.shields.io/badge/Security-Stateless%20JJWT%20%2B%20RBAC-green.svg)](#)
[![Frontend](https://img.shields.io/badge/Frontend-Vanilla%20Web%20(Zero--Framework)-orange.svg)](#)
[![Backend](https://img.shields.io/badge/Backend-Spring%20Boot%203.2.5-brightgreen.svg)](#)

---

## 1. Architectural Philosophy & Layered Blueprint

WasteWatch adheres strictly to **Separation of Concerns (SoC)** and the **Single Responsibility Principle (SRP)**. The architecture enforces an enterprise-standard, unidirectional layered pipeline from user interface to database storage:

```
┌────────────────────────────────────────────────────────┐
│      Frontend (HTML5 / Modern CSS3 / Vanilla ES6+)     │
│   - Eco Volunteer Field App & Glassmorphism Admin UI   │
└──────────────────────────┬─────────────────────────────┘
                           │ JSON over HTTP/REST (Bearer JWT)
┌──────────────────────────▼─────────────────────────────┐
│          Controller Layer (@RestController)            │
│  - 14 REST Controllers: Route mapping & DTO validation │
└──────────────────────────┬─────────────────────────────┘
                           │ Command DTOs / Arguments
┌──────────────────────────▼─────────────────────────────┐
│             Service Layer (@Service)                   │
│  - Business logic, 100-pt scoring, violation workflows │
└──────────────────────────┬─────────────────────────────┘
                           │ Entity Domain Models
┌──────────────────────────▼─────────────────────────────┐
│           Repository Layer (@Repository)               │
│  - Spring Data JPA, Hibernate 6, JPQL / Query Methods  │
└──────────────────────────┬─────────────────────────────┘
                           │ ANSI SQL / JDBC
┌──────────────────────────▼─────────────────────────────┐
│       Database (H2 Runtime Persistence / PostgreSQL)   │
│  - File-based dev persistence at backend/data/         │
└────────────────────────────────────────────────────────┘
```

---

## 2. Backend Layer Responsibilities

### 2.1. Controller Layer (`com.wastewatch.controller`)
* **Role**: Ingestion, dispatching, and serialized responses for HTTP REST requests.
* **Responsibilities**:
  - Expose clean, standardized endpoints across 14 controllers.
  - Enforce declarative input validation via `@Valid` and Jakarta validation annotations (`@NotBlank`, `@NotNull`, `@Min`, `@Max`, `@Email`, `@Pattern`).
  - Extract the authenticated user from the Spring Security context (`@AuthenticationPrincipal UserPrincipal`).
  - Enforce method-level security with `@PreAuthorize("hasRole('ADMIN')")`.
  - Wrap responses in consistent envelopes (`ApiResponse<T>`, `ErrorResponse`).
* **Architectural Rule**: **Zero business logic in controllers**. Controllers never calculate scores, assess penalties, evaluate persistent non-compliance, or call repositories directly.

### 2.2. Service Layer (`com.wastewatch.service`)
* **Role**: Domain processing and business rule enforcement.
* **Responsibilities**:
  - Implement the authoritative 100-point compliance scoring algorithm (`ComplianceScoringService`).
  - Execute building-level audit submissions with 30-second cooldown and anti-impersonation checks (`InspectionService`).
  - Automatically generate non-compliance violation tickets and detect repeat non-compliance (`ViolationService`).
  - Calculate Area Health Scores, confidence ratings, and time-series trends (`AnalyticsServiceImpl`).
  - Generate multi-dimensional compliance reports and stream RFC-4180 CSV exports (`ReportService`).
  - Manage volunteer self-registration, verification approval, and performance metrics (`VolunteerService`, `AuthService`).
  - Record immutable audit trail logs for all administrative mutations (`AuditLogService`).
  - Enforce database transaction boundaries with `@Transactional` (read-only for queries, atomic for mutations).
  - Throw standardized domain exceptions (`ResourceNotFoundException`, `DuplicateResourceException`, `InvalidRequestException`).

### 2.3. Repository Layer (`com.wastewatch.repository`)
* **Role**: Object-relational mapping and data persistence abstraction.
* **Responsibilities**:
  - Extend Spring Data JPA `JpaRepository<T, ID>`.
  - Provide multi-attribute filtering via JPQL queries (e.g. `ViolationRepository.findWithFilters(...)`, `AuditLogRepository.findWithFilters(...)`).
  - Ensure database-agnostic portability across H2, PostgreSQL, and MySQL.

### 2.4. Entity Layer (`com.wastewatch.entity`)
* **Role**: JPA relational domain models mapping to ANSI-standard SQL tables:
  1. `AppUser`: Users with credentials, contact details, registration profile (`city`, `state`, `address`, `reason`), role (`ADMIN`, `VOLUNTEER`), and status (`ACTIVE`, `INACTIVE`, `SUSPENDED`, `PENDING`, `REJECTED`).
  2. `Area`: Geographic municipal wards, sectors, and collection zones.
  3. `Building`: Physical societies and commercial premises with GPS coordinates. Primary anchor for inspections and violations.
  4. `Room`: Deprecated flat/unit entity retained strictly for database schema compatibility.
  5. `Inspection`: Building-level audits conducted by volunteers evaluating 6 segregation criteria, score, classification, and status.
  6. `Violation`: Non-compliance incidents tied to buildings with severity tiers (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`) and lifecycle statuses (`OPEN`, `ACKNOWLEDGED`, `ACTION_REQUIRED`, `RESOLVED`).
  7. `Notification`: In-app governance alerts with typed classifications (`INSPECTION_ASSIGNED`, `VIOLATION_RAISED`, `VIOLATION_SEVERITY_HIGH`, `PERSISTENT_NON_COMPLIANCE`, `COVERAGE_ALERT`, `SYSTEM`).
  8. `AuditLog`: Immutable audit trail recording administrative actions, entities, actors, and metadata.

### 2.5. Data Transfer Objects — DTOs (`com.wastewatch.dto`)
* Strongly typed API contracts preventing mass assignment vulnerabilities and decoupling domain entities from JSON representation.
* Grouped logically by domain: `auth`, `area`, `building`, `room`, `inspection`, `violation`, `volunteer`, `notification`, `report`, `analytics`, `map`, `audit`.

### 2.6. Security Architecture (`com.wastewatch.security` & `com.wastewatch.config`)
* **Stateless JWT Authentication**: Signed using HMAC-SHA256 with a 256-bit secret key and 24-hour expiration (`JwtTokenProvider`).
* **One-Way Password Encryption**: BCrypt password hashing with work factor 10.
* **Security Filter Chain (`SecurityConfig`)**:
  - Public endpoints: `/api/health`, `/api/auth/login`, `/api/auth/register`, `/h2-console/**`.
  - Volunteer inspection access: `POST /api/inspections`, `GET /api/inspections/**`, `GET /api/volunteers/me/inspections`, `GET /api/map/**`.
  - Volunteer verification guard: Accounts in `PENDING` or `REJECTED` status are blocked from submitting field audits (HTTP 403 Forbidden).
  - Admin-only consoles: `/api/violations/**`, `/api/dashboard/**`, `/api/analytics/**`, `/api/reports/**`, `/api/audit-logs/**`, infrastructure write operations (`POST`, `PUT`, `DELETE` on areas, buildings, rooms).
  - Anti-privilege escalation guard: Volunteer management endpoints reject any attempt to modify administrative accounts.
  - Strict volunteer isolation: Volunteers can query only their own audits and performance metrics.

### 2.7. Runtime Database Migration (`DatabaseMigration`)
* Operates as an automated `@Order(1) CommandLineRunner` on startup:
  1. Drops outdated check constraints on `APP_USERS.STATUS` that omitted `PENDING` and `REJECTED`.
  2. Installs constraint `CHECK_APP_USERS_STATUS` with all five enum values.
  3. Drops `NOT NULL` on `INSPECTIONS.ROOM_ID` and `VIOLATIONS.ROOM_ID` to support pure building audits.
  4. Backfills legacy `BUILDING_ID` foreign keys for historical room-only inspection rows.

---

## 3. Frontend Architecture (Vanilla Web Stack)

### 3.1. Zero-Framework Architecture
WasteWatch avoids heavyweight JavaScript frameworks (React, Angular, Vue) to achieve:
- **Instant Execution**: Zero build, compile, or bundle latency.
- **Micro Footprint**: Standard web assets served directly via static server (`serve.json`).
- **Full Transparency**: Native HTML5, modern CSS3 custom properties (design tokens), and standard ES6 modules.

### 3.2. Complete Directory Structure
```
frontend/
├── index.html                   # Primary entry & login portal with opening intro splash overlay
├── login.html                   # Backward-compatible redirect shim
├── register.html                # Volunteer self-registration portal
├── package.json                 # Dev static server scripts
├── serve.json                   # Clean URL routing & rewrites
├── convert-stickers.js          # Utility script for image alpha processing
├── assets/                      # Graphic brand artwork, stickers, backgrounds & icons
│   ├── favicon.png              # Browser tab icon
│   ├── logo.png                 # Main horizontal logo
│   ├── logo-icon.png            # Circular app badge
│   ├── logo-icon-192.png        # High-res PWA icon
│   ├── inspect-bg-scene.svg     # Eco skyline & 3-bin illustration
│   ├── sidebar-leaves.svg       # Botanical leaf accent for volunteer sidebar
│   ├── sticker-*.png            # 7 civic-tech stickers (dustbin, truck, earth, etc.)
│   └── backgrounds/             # 9 thematic glassmorphism background renders (WebP & PNG)
├── css/                         # Modular CSS stylesheets
│   ├── variables.css            # Centralized CSS design tokens (HSL color system, shadows, radii)
│   ├── base.css                 # Reset, typography, utility classes
│   ├── layout.css               # Sidebar, topbar, grid layout shells
│   ├── components.css           # Buttons, form controls, tables, modals, badges, toasts
│   ├── admin-theme.css          # Glassmorphism theme for all 9 municipal admin consoles
│   └── volunteer-inspect.css    # Full eco-themed stylesheet for volunteer Field App
├── js/                          # Client JavaScript logic
│   ├── lucide.min.js            # Offline Lucide icon pack
│   ├── api/
│   │   └── client.js            # ApiClient singleton for all REST endpoints & token management
│   └── utils/
│       ├── helpers.js           # Auth state, Toast notifications, Fmt formatting, DOM helpers
│       └── sidebar.js           # Role-based navigation sidebar injector
├── admin/                       # 9 Municipal Administrative Consoles
│   ├── dashboard.html           # Executive KPI overview & embedded Leaflet compliance map
│   ├── areas.html               # Tabbed Areas & Buildings infrastructure management
│   ├── inspections.html         # Field audit records with search, filters & export
│   ├── violations.html          # Violation tracking, fine assessment & resolution
│   ├── volunteers.html          # Volunteer applications review, approval & governance
│   ├── map.html                 # Full-screen Leaflet.js OpenStreetMap compliance map
│   ├── analytics.html           # Decision intelligence & Area Health Scores
│   ├── reports.html             # Multi-dimensional reporting & streaming CSV export
│   └── audit-logs.html          # Immutable security audit trail console
└── volunteer/                   # Mobile-First Volunteer Field App
    ├── inspect.html             # 3-step building waste audit workflow with live score dial
    └── history.html             # Volunteer audit history, KPI cards, CSV/PDF export
```

---

## 4. Key Subsystems & Core Workflows

### 4.1. Authoritative 100-Point Compliance Scoring Subsystem
Field audits evaluate six distinct waste segregation criteria implemented in `ComplianceScoringService`:

| Criteria | Weight | Evaluation Requirement |
| :--- | :---: | :--- |
| **Wet Waste Separated** | 25 pts | Biodegradable organic food scraps in dedicated green bin |
| **Dry Waste Separated** | 25 pts | Recyclable paper, plastic, metal, and glass in dedicated blue bin |
| **Sanitary / Reject Waste Separated** | 20 pts | Hazardous, medical, or sanitary waste wrapped in dedicated red bin |
| **Correct Bins Used** | 15 pts | Color-coded municipal standard bins present at collection point |
| **Bins Clearly Labelled** | 10 pts | Legible signage or visual iconography on receptacles |
| **No Mixed Contamination** | 5 pts | Zero visible co-mingling across waste streams |
| **Total Available** | **100 pts** | Maximum Compliance Score |

* **Classification Tiers**:
  - `EXCELLENT`: 80–100 points (model compliance)
  - `GOOD`: 60–79 points (acceptable; minor improvements advised)
  - `NEEDS_IMPROVEMENT`: 40–59 points (formal advisory notice)
  - `POOR`: 0–39 points (triggers automated violation ticket)
* **Contamination Invariant**: No mixed waste points (+5) cannot be awarded if all waste categories fail segregation, as unsegregated waste is inherently co-mingled.

---

### 4.2. Building-Level Inspection Workflow
1. **Selection (`Step 1`)**:
   - Volunteer searches for a building by name or code, selects from dropdown, or taps quick-select chips.
   - Building preview card renders address, zone, and total floors.
2. **Checklist Audit (`Step 2`)**:
   - Volunteer evaluates the 6 segregation criteria.
   - Real-time SVG score ring recalculates total score and classification dynamically.
   - Optional evidence photo URL and observation remarks are recorded.
3. **Submission & Verification**:
   - Client posts to `POST /api/inspections`.
   - Backend enforces 30-second cooldown per building per volunteer.
   - If volunteer is `PENDING`, submission is rejected with 403 Forbidden.
   - If score is `POOR` (<40), a violation is automatically created and admins receive alerts.
4. **Result Summary (`Step 3`)**:
   - Instant score feedback and violation notification status presented.

---

### 4.3. Volunteer Self-Registration & Administrative Approval Lifecycle
```
[Public User] 
      │ 
      ▼ Submits /register.html (POST /api/auth/register)
[Account Created with status = PENDING]
      │
      ▼ Login permitted; Audits blocked (403 Forbidden)
[Admin Reviews Application on /admin/volunteers.html]
      ├── Action: Approve (POST /api/volunteers/{id}/approve) ──► status = ACTIVE (Audits Enabled)
      └── Action: Reject  (POST /api/volunteers/{id}/reject)  ──► status = REJECTED (Audits Blocked)
```

---

### 4.4. Automated Violation & Repeat Non-Compliance Engine
- **Trigger**: Automatic creation whenever an audit score is `POOR` (<40).
- **Severity Tiers**:
  - 0–19 points: `CRITICAL` (assessed fine: ₹500.00)
  - 20–39 points: `HIGH` (assessed fine: ₹250.00)
- **Persistent Non-Compliance**: If a building registers 3 consecutive `POOR` inspections, an automated `PERSISTENT_NON_COMPLIANCE` alert is dispatched to administrators.
- **Lifecycle Transitions**: `OPEN` ➔ `ACKNOWLEDGED` ➔ `ACTION_REQUIRED` ➔ `RESOLVED`.
- Terminal resolution requires mandatory resolution notes via `PUT /api/violations/{id}/resolve`.

---

### 4.5. Interactive Geographic Map (Leaflet.js)
- Integrated on both the Admin Dashboard (`admin/dashboard.html`) and dedicated full-screen console (`admin/map.html`).
- OpenStreetMap tiles rendered via CartoDB dark matter layer.
- Dynamic HTML div-markers displaying real-time compliance score numbers.
- Strict compliance color-coding: `GREEN` (80–100), `YELLOW` (60–79), `ORANGE` (40–59), `RED` (0–39), `GRAY` (no audits).
- Pulsing animated rings (`.marker-hotspot`) for at-risk facilities.
- "Location unavailable" drawer isolates facilities lacking GPS coordinates to prevent canvas crashes.

---

### 4.6. Decision Support Analytics Engine
- **Standardized Area Health Score Formula**:
  $$\text{Area Health Score} = (0.70 \times \text{Avg Compliance}) + (0.20 \times \text{Inspection Coverage \%}) + (0.10 \times \text{Violation Resolution Rate \%})$$
- **Coverage Confidence Ratings**:
  - `HIGH`: $\ge 70\%$ of buildings inspected
  - `MEDIUM`: $40\% – 69\%$ of buildings inspected
  - `LOW`: $< 40\%$ of buildings inspected
- Time-series trend analysis across daily, weekly, and monthly periods.
- Period-over-period improvement leaderboard and chronic repeat offender rankings.

---

### 4.7. Multi-Dimensional Reporting & Streaming CSV
- Reports across 7 distinct perspectives: `overview`, `areas`, `buildings`, `coverage`, `violations`, `volunteers`, and `inspections`.
- Dynamic ISO-8601 date bounding (`from` and `to`).
- Streaming RFC-4180 compliant CSV export (`GET /api/reports/export/csv`) with `Content-Disposition: attachment; filename="wastewatch-<type>-report.csv"`.

---

### 4.8. Immutable Security Audit Trail
- `AuditLogService` captures critical operational lifecycle events:
  `USER_LOGIN`, `CREATE_AREA`, `CREATE_BUILDING`, `CREATE_INSPECTION`, `RESOLVE_VIOLATION`, `VOLUNTEER_STATUS_CHANGED`.
- Multi-criteria querying by action, entity type, user ID, and date range.
- Zero password, credential, or token leakage in logs.

---

### 4.9. QR Code Subsystem (Google ZXing)
- Standardized QR token format: `QR-{BUILDING_CODE}-{ROOM_NUMBER}` (e.g. `QR-GVH-101`).
- Server-side raw PNG byte streaming via `GET /api/rooms/{id}/qr-image` and `GET /api/rooms/qr/{code}/image`.
- Safe lookup endpoint `GET /api/rooms/qr/{code}` returns sanitized room and building metadata.
