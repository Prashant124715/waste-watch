# WasteWatch — Changelog & Version History

All notable changes to the WasteWatch project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to Semantic Versioning where applicable.

## [Development H2 Persistence] — 2026-10-14

### Changed
- Default local development and CEP evaluation to file-based H2 at `backend/data/wastewatchdb.mv.db`, with Hibernate schema updates that retain existing records.
- Isolate automated tests on an in-memory H2 database and limit the H2 console to the development profile.
- Document database backup and restore steps; Git ignores local database files and environment-specific credentials.
- Verified a test area survives a backend restart against the existing H2 file; all 95 backend tests pass.

## [Phase 11: Geographic Monitoring & Interactive Map] — 2026-10-14

### Added
- **Interactive Leaflet.js Map on Admin Dashboard (`frontend/admin/dashboard.html`)**:
  - Embedded high-visibility spatial compliance map between executive KPI metrics and analytical charts.
  - Custom HTML div-markers displaying real-time compliance score numbers with dynamic scaling and hover states.
  - Strict compliance color-coding: `GREEN` (80–100), `YELLOW` (60–79 with accessible high-contrast text), `ORANGE` (40–59), `RED` (0–39), and `GRAY` (no audits).
  - Rich interactive building popups displaying: Building name, Area, Average compliance score, Inspection coverage percentage, Last inspection timestamp, Poor room count, Open violations count, with direct links to Building Details and Inspection Audits.
  - Multi-criteria filter toolbar: Filter by Area, Compliance status, Building, Inspection status (`INSPECTED` vs `UNINSPECTED`), and Hotspots-only toggle.
  - Hotspot visualizer: Animated pulsing rings (`.marker-hotspot`) and glowing danger circle radius overlays on the map for at-risk facilities.
  - Spatial Intelligence Sidebar featuring color-coded Area Hotspot Analysis summary cards and an isolated "Location unavailable" drawer that prevents unmapped buildings from breaking the map.
  - Inline error handling banner with "Retry Map" button ensuring resilient user experience.
- **Enhanced Full-Screen Map Console (`frontend/admin/map.html`)**:
  - Updated with unified compliance color semantics, Building and Inspection status dropdown filters, Area Hotspot Analysis console, and unmapped facility management.
- **Backend Map APIs (`MapController`, `MapService`)**:
  - `GET /api/map/buildings`: Returns comprehensive spatial records with building ID, name, area, latitude, longitude, compliance score, compliance status, inspection coverage, poor rooms, open violations, last inspection, and valid coordinate flags. Supports query parameter filtering by `areaId`, `complianceStatus`, `buildingId`, and `inspectionStatus`.
  - `GET /api/map/hotspots`: Returns ward-level spatial hotspot aggregates with center coordinates, average compliance score, health tier, poor room counts, open violations, and severity ratings (`CRITICAL`, `WARNING`, `ADVISORY`, `NONE`).
  - Robust coordinate boundary validation ($-90 \le \text{lat} \le 90$, $-180 \le \text{lng} \le 180$, Null Island $(0,0)$ rejection).
- **Automated Tests**:
  - Added unit tests in `MapServiceTest` covering valid coordinates, missing coordinates, Null Island (0,0), multiple mixed buildings, filter behavior, and area hotspot calculations.
  - Added integration tests in `MapControllerTest` testing admin and volunteer access, unauthenticated rejection (401), query filtering, complete response schema validation, and `/api/map/hotspots`.
  - Full test suite passes: **95/95 tests passing (100% success)**.

---

## [Final Hardening & CEP Release] — 2026-10-13

### Hardened & Polished
- **Zero Framework Debt**: Verified clean Vanilla JS/HTML5/CSS3 frontend architecture with zero client build tool requirements.
- **Security Hardening**:
  - Enforced BCrypt password hashing and signed JJWT tokens (HMAC-SHA256).
  - RBAC authorization verified across all endpoints.
  - Strict volunteer isolation: field inspectors can only view and submit their own audits.
  - Anti-privilege escalation defense verified in `VolunteerService`.
  - Zero sensitive token or credential leakage across audit logs and API responses.
- **Accidental Cooldown Guard**: Fixed false-positive in 30-second submission cooldown for newly seeded rooms in test executions.
- **Quality Assurance**: Complete automated test suite of **90/90 passing tests** with 100% pass rate.
- **Build Verification**: Clean production JAR packaging verified via `mvnw.cmd package`.

---

## [Phase 14: Reporting and Governance] — 2026-10-13

### Added
- **Volunteer Governance Engine (`VolunteerService`, `VolunteerController`)**:
  - `GET /api/volunteers`: Query active and inactive volunteer roster with audit metrics.
  - `PUT /api/volunteers/{id}/status`: Activate or deactivate volunteers with reason tracking.
  - Privilege escalation defense: Rejects any attempt to modify administrative users via volunteer management.
- **Multi-Dimensional Reporting Engine (`ReportService`, `ReportController`)**:
  - `GET /api/reports/overview`: Overall compliance rate, total audits, unique rooms covered, active violations.
  - `GET /api/reports/areas`: Ward-level compliance, coverage %, and violation rates.
  - `GET /api/reports/buildings`: Building scorecard matrix with room totals and compliance rankings.
  - `GET /api/reports/coverage`: Room coverage breakdown and uninspected count.
  - `GET /api/reports/violations`: Violation distribution grouped by severity and status.
  - `GET /api/reports/volunteers`: Field volunteer productivity, average scores, and active days.
  - Dynamic date bounds (`startDate`, `endDate`) supported across all reporting endpoints.
- **Streaming CSV Export (`/api/reports/export/csv`)**:
  - Server-side generation of RFC-4180 compliant CSV files with `Content-Disposition: attachment`.
- **Security Audit Trail (`AuditLogService`, `AuditLogController`, `AuditLogRepository`)**:
  - Immutable audit logs capturing user login, infrastructure changes, volunteer toggles, and violation resolutions.
  - Multi-criteria filter query via JPQL (`action`, `entityType`, date range, search).
- **Administrative UI (`frontend/admin/reports.html`, `frontend/admin/audit-logs.html`)**:
  - Executive Reports Console with period selectors, summary KPI cards, tabbed views, and instant CSV download.
  - Security Audit Trail Console with live filtering, search bar, and action badges.
- **Automated Tests**: 15 new tests in `ReportingAndGovernanceTest` (total: **90/90 passing**).

---

## [Phase 13: Violation and Notification Management] — 2026-10-13

### Added
- **Automated Violation Generation (`ViolationService`)**:
  - Automatic creation of violations for inspections scoring <40 (`POOR`).
  - Severity calculation: 0–19 (`CRITICAL`), 20–39 (`HIGH`), 40–59 (`MEDIUM`).
  - Idempotent processing preventing duplicate violations for the same inspection.
- **Persistent Non-Compliance Engine**:
  - Identifies rooms with 3 consecutive `POOR` inspections and marks them as `PERSISTENT_NON_COMPLIANCE` with `CRITICAL` severity.
- **Violation Lifecycle Management**:
  - Statuses: `OPEN`, `ACKNOWLEDGED`, `ACTION_REQUIRED`, `RESOLVED`.
  - Admin endpoints: `GET /api/violations`, `GET /api/violations/{id}`, `PUT /api/violations/{id}/status`, `PUT /api/violations/{id}/resolve`.
- **In-App Notification Subsystem (`NotificationService`, `NotificationController`)**:
  - Generates targeted notifications for high-severity violations, repeat offenders, and coverage deficits.
  - Endpoints: `GET /api/notifications`, `GET /api/notifications/unread-count`, `PUT /api/notifications/{id}/read`, `PUT /api/notifications/read-all`.
- **Admin UI Console (`frontend/admin/violations.html`)**:
  - Status transition dropdowns, severity badges, and quick resolution modal.

---

## [Phase 12: QR Code Workflow] — 2026-10-12

### Added
- **Deterministic QR Token Generation (`RoomService`)**:
  - Unique QR identifiers formatted as `WASTEWATCH-ROOM-{BUILDING_CODE}-{ROOM_NUMBER}`.
- **Safe Metadata Lookup (`GET /api/rooms/qr/{qrCodeToken}`)**:
  - Non-sensitive room, building, and area metadata returned without exposing database IDs.
- **Client QR Generator & Mobile Scanner**:
  - Admin QR code modal with preview and PNG download in `admin/areas.html`.
  - Volunteer mobile inspection view in `volunteer/inspect.html` supporting camera scan and manual identifier entry.

---

## [Phase 11: Interactive Map & Geographic Visualization] — 2026-10-11

### Added
- **Geographic Markers Endpoint (`GET /api/map/markers`)**:
  - Returns building coordinates, compliance status, and room stats.
- **Leaflet.js Map UI (`frontend/admin/map.html`)**:
  - OpenStreetMap dark CartoDB tiles with compliance pins, popup stats, and status filtering.

---

## [Phase 10: Analytics and Decision Support Engine] — 2026-10-09

### Added
- **Backend Analytics Engine (`AnalyticsService`, `AnalyticsServiceImpl`)**:
  - Implemented server-side aggregate calculations, separating business analytics from client UI code.
  - Standardized Area Health Score model: `(0.70 * Avg Compliance) + (0.20 * Inspection Coverage %) + (0.10 * Violation Resolution Rate %)`.
  - Confidence Rating evaluation separating raw compliance from inspection coverage sample size.
  - Time-series trend grouping by daily, weekly, and monthly intervals.
  - Period-over-period comparative analysis detecting most improved vs declining areas.
  - Hotspot identification for Repeat Offender Rooms (failed $\ge 2$) and Problem Buildings (low avg score / high poor unit percentage) with `CRITICAL`, `HIGH`, `MEDIUM` risk levels.
  - Volunteer productivity tracking: audits completed, average score awarded, unique rooms audited, and active engagement status (`ACTIVE`, `RECENT`, `INACTIVE`).
- **REST APIs (`AnalyticsController`)**:
  - `GET /api/analytics/overview`
  - `GET /api/analytics/areas`
  - `GET /api/analytics/buildings`
  - `GET /api/analytics/trends`
  - `GET /api/analytics/improvements` / `GET /api/analytics/improved-areas`
  - `GET /api/analytics/hotspots`
  - `GET /api/analytics/volunteers`
- **Analytics DTO Suite (`com.wastewatch.dto.analytics.*`)**:
  - `OverviewAnalyticsDto`, `AreaAnalyticsDto`, `BuildingAnalyticsDto`, `TrendAnalyticsDto`, `TrendPointDto`, `AreaImprovementDto`, `HotspotAnalyticsDto`, `RepeatOffenderRoomDto`, `ProblemBuildingDto`, `VolunteerAnalyticsDto`.
- **UI & Navigation (`frontend/admin/analytics.html`, `frontend/js/utils/sidebar.js`, `frontend/js/api/client.js`)**:
  - Dedicated multi-tab Analytics Console with interactive KPI cards, formula explanation banners, confidence ratings, and trend visualizer.
- **Automated Tests (`AnalyticsServiceTest`, `AnalyticsControllerTest`)**:
  - 16 new automated unit and integration tests covering zero state, perfect compliance, partial coverage confidence rule, poor compliance, repeat offenders, improvements, and RBAC authorization (total: **69/69 passing**).

---

## [Phase 9: Full Admin & Volunteer Views Implementation & Polishing] — 2026-10-09

### Added
- **Admin Views Suite**:
  - `frontend/admin/dashboard.html`: Executive dashboard with 8 KPI metrics, 7-day inspection trends, distribution bar charts, repeat offenders, and live recent inspection feed.
  - `frontend/admin/areas.html`: Hierarchical management tabs for Areas, Buildings, and Rooms with live CRUD modals, QR code views, and coordinate validation.
  - `frontend/admin/inspections.html`: Historical audit records query table with CSV and PDF export, search and classification filtering, and granular 100-pt breakdown modal.
  - `frontend/admin/violations.html`: Violation escalation tracker with severity badges, fine accounting, and status lifecycle resolution modal (`OPEN`, `NOTIFIED`, `ESCALATED`, `RESOLVED`).
  - `frontend/admin/volunteers.html`: Field volunteer roster with performance statistics, avatar generation, and individual inspection history modals.
  - `frontend/admin/map.html`: Interactive Leaflet.js OpenStreetMap interface with color-coded compliance status pins, building summaries, and instant KPI breakdown.
- **Volunteer Views Suite**:
  - `frontend/volunteer/inspect.html`: 3-step mobile-first field inspection workflow (QR scan/manual select ➔ 6-stream criteria evaluation with live preview score ring ➔ instant submission with cooldown guard).
  - `frontend/volunteer/history.html`: Volunteer personal audit history with metric badges, search, and CSV/PDF export.
- **End-to-End Test Verification**:
  - Verified 53/53 Spring Boot unit and integration tests executing with 100% success.

---

## [Phase 8: Frontend Foundation, Design System & Authentication Flow] — 2026-10-09

### Added
- **Design Tokens & System (`variables.css`, `base.css`, `layout.css`, `components.css`)**:
  - High-contrast dark SaaS theme with HSL colors, glassmorphism, responsive grid layouts, and animated radial score progress rings.
- **Frontend API Client (`frontend/js/api/client.js`)**:
  - Centralized `ApiClient` managing authentication tokens, request headers, error handling, and all backend REST endpoints.
- **Authentication & Authorization (`frontend/index.html`, `frontend/login.html`, `frontend/js/utils/helpers.js`, `frontend/js/utils/sidebar.js`)**:
  - Role-based redirect routing (`ADMIN` ➔ `/admin/dashboard.html`, `VOLUNTEER` ➔ `/volunteer/inspect.html`), dynamic sidebar generator, and JWT session persistence.

---

## [Phase 7: Compliance Scoring Engine & Breakdown] — 2026-10-09

### Added
- **Dedicated Compliance Scoring Models (`ComplianceResult`, `ScoreBreakdownDto`)**:
  - Structured result encapsulation containing total score, status tier, granular breakdown, auto-violation trigger, and violation severity.
  - Granular criteria breakdown: `wetWaste` (25 pts), `dryWaste` (25 pts), `sanitaryWaste` (20 pts), `correctBins` (15 pts), `labelledBins` (10 pts), `noMixedWaste` (5 pts).
- **Compliance Scoring Engine Enhancements (`ComplianceScoringService`)**:
  - Implemented `calculateCompliance(...)` returning full `ComplianceResult` with transparent breakdown.
  - Null-safe evaluation coercing null booleans to false (0 pts) without throwing exceptions.
  - Mixed contamination invariant rule: prevents awarding `noMixedWaste` points if all waste separation streams fail.
  - Maintained backward-compatible `calculateScore(...)` for existing service flows.
- **DTO Enhancements (`InspectionResponseDto`)**:
  - Integrated `ScoreBreakdownDto breakdown` in all inspection query and submission responses.
- **Unit Test Suite (`ComplianceScoringServiceTest`)**:
  - 25 thorough unit test cases verifying score boundaries (100, 90, 80, 79, 60, 59, 40, 39, 0), individual criteria weights, null safety, and contamination invariants.

---

## [Phase 6: Volunteer Inspection System & Compliance Scoring] — 2026-10-09

### Added
- **Volunteer Inspection REST APIs (`InspectionController`, `InspectionService`)**:
  - `POST /api/inspections`: Field audit submission with authoritative server-side score calculation, auto violation detection on low score (<40), and 30-second cooldown duplicate submission prevention.
  - `GET /api/inspections`: Multi-filter inspection query with RBAC security (Admin views all/filtered; Volunteer receives self-scoped records).
  - `GET /api/inspections/{id}`: Detailed inspection lookup with volunteer access restriction.
  - `GET /api/rooms/{roomId}/inspections`: Historical inspections for a specific room.
  - `GET /api/volunteers/me/inspections`: Logged-in volunteer's personal audit submissions.
  - `GET /api/inspections/recent`: Most recent audits (top 20 system-wide for admin; top 20 self for volunteer).
- **Compliance Scoring Engine (`ComplianceScoringService`)**:
  - 100-point weighted algorithm: Wet Waste (25 pts), Dry Waste (25 pts), Sanitary/Reject (20 pts), Correct Bins (15 pts), Bins Labelled (10 pts), No Mixed Waste (5 pts).
  - Tiered classification: `EXCELLENT` (80-100), `GOOD` (60-79), `NEEDS_IMPROVEMENT` (40-59), `POOR` (0-39).
  - Automated penalty and severity tier assignment (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`).
- **Data Integrity & Duplicate Rule**:
  - Enforced 30-second per-room cooldown preventing accidental double-click duplicate submissions (returns HTTP 409 Conflict).
  - Server-generated timestamps and non-tamperable volunteer identity linked strictly to JWT `UserPrincipal`.
- **Automated Integration Tests (`InspectionWorkflowTest`)**:
  - Valid inspection submissions with score calculation verification.
  - Failing inspection automatic violation flag and admin resolution workflow.
  - 404 on invalid room ID.
  - 401 on unauthorized unauthenticated requests.
  - 400 on missing required fields.
  - 409 on rapid duplicate submissions.
  - Volunteer record isolation and `/api/volunteers/me/inspections` validation.

---

## [Phase 5: Location & Infrastructure Management] — 2026-10-08

### Added
- **Area Management REST APIs (`AreaController`, `AreaService`)**:
  - `GET /api/areas`: List all areas (Admin, Volunteer)
  - `GET /api/areas/{id}`: Get area details (Admin, Volunteer)
  - `POST /api/areas`: Create area with uniqueness checks (Admin only)
  - `PUT /api/areas/{id}`: Update area details (Admin only)
  - `DELETE /api/areas/{id}`: Remove area (Admin only)
- **Building Management REST APIs (`BuildingController`, `BuildingService`)**:
  - `GET /api/buildings`: List all buildings or filter by `areaId` (Admin, Volunteer)
  - `GET /api/buildings/{id}`: Get building details (Admin, Volunteer)
  - `GET /api/areas/{areaId}/buildings`: List buildings within an area (Admin, Volunteer)
  - `POST /api/buildings`: Create building with coordinates validation (Admin only)
  - `PUT /api/buildings/{id}`: Update building details (Admin only)
  - `DELETE /api/buildings/{id}`: Remove building (Admin only)
- **Room Management REST APIs (`RoomController`, `RoomService`)**:
  - `GET /api/rooms`: List all rooms or filter by `buildingId` (Admin, Volunteer)
  - `GET /api/rooms/{id}`: Get room details (Admin, Volunteer)
  - `GET /api/buildings/{buildingId}/rooms`: List rooms within a building (Admin, Volunteer)
  - `POST /api/rooms`: Create room with QR token and uniqueness checks (Admin only)
  - `PUT /api/rooms/{id}`: Update room details (Admin only)
  - `DELETE /api/rooms/{id}`: Remove room (Admin only)
- **DTOs & Validations**:
  - `AreaRequestDto`, `AreaResponseDto`
  - `BuildingRequestDto`, `BuildingResponseDto`
  - `RoomRequestDto`, `RoomResponseDto`
  - Jakarta validations (`@NotBlank`, `@NotNull`, `@Min`, `@DecimalMin`, `@DecimalMax`)
  - Duplicate conflict enforcement (409 Conflict) for area codes, building codes, and duplicate room numbers within the same building.
- **Automated Integration Tests (`InfrastructureManagementTest`)**:
  - 9 comprehensive tests covering CRUD operations, RBAC enforcement, duplicate detection, and 400/404/409 handling.

---

## [Phase 4: Authentication & Role-Based Security] — 2026-10-08

### Added
- **Spring Security Configuration (`SecurityConfig`)**:
  - Stateless JWT authentication filter chain with `SessionCreationPolicy.STATELESS`.
  - CORS configuration permitting local frontend development origins (`http://localhost:*`, `http://127.0.0.1:*`).
  - H2 console frame options enabled.
- **JWT Provider & Utilities (`JwtTokenProvider`)**:
  - JJWT 0.12.5 token issuance, signing (HMAC-SHA256), claims parsing, and validation.
- **Security Filter & Context (`JwtAuthenticationFilter`, `UserPrincipal`)**:
  - Extract Bearer tokens and populate Spring SecurityContext.
  - Role-based authorities (`ROLE_ADMIN`, `ROLE_VOLUNTEER`).
- **User Details Service (`CustomUserDetailsService`)**:
  - Support for login by username or email.
- **Authentication Controller & Service (`AuthController`, `AuthService`)**:
  - `POST /api/auth/login`: Authenticate and return JWT token and role details without exposing password hashes.
  - `GET /api/auth/me`: Retrieve currently authenticated user profile.
- **Automated Security Tests (`AuthControllerTest`)**:
  - Tests verifying valid logins, invalid credentials (401), and token verification.

---

## [Phase 3: Database Domain Model & H2 Persistence] — 2026-10-08

### Added
- **Core JPA Entities (`com.wastewatch.entity`)**:
  - `AppUser` (mapped to `APP_USERS`)
  - `Area` (mapped to `AREAS`)
  - `Building` (mapped to `BUILDINGS`)
  - `Room` (mapped to `ROOMS`, composite unique constraint on `building_id` + `room_number`)
  - `Inspection` (mapped to `INSPECTIONS`)
  - `Violation` (mapped to `VIOLATIONS`)
  - `Notification` (mapped to `NOTIFICATIONS`)
  - `AuditLog` (mapped to `AUDIT_LOGS`)
- **Domain Enums (`com.wastewatch.entity.enums`)**:
  - `UserRole`, `UserStatus`, `BuildingType`, `InspectionStatus`, `ComplianceClassification`, `ViolationSeverity`, `ViolationStatus`, `NotificationType`.
- **Spring Data JPA Repositories (`com.wastewatch.repository`)**:
  - `UserRepository`, `AreaRepository`, `BuildingRepository`, `RoomRepository`, `InspectionRepository`, `ViolationRepository`, `NotificationRepository`, `AuditLogRepository`.
- **Database Seeder (`DataInitializer`)**:
  - Automated seeding of admin (`admin` / `admin123`), volunteer (`volunteer` / `volunteer123`), areas, buildings, rooms, and sample inspection records.

---

## [Phase 2: Backend Foundation] — 2026-10-08

### Added
- **Spring Boot 3.2.5 Backend Project**:
  - Created `backend/pom.xml` with dependencies for Web, JPA, H2, Validation, Security, and JJWT.
  - Configured `backend/src/main/resources/application.yml` with in-memory H2 datasource (`jdbc:h2:mem:wastewatchdb`), H2 console, and JPA Hibernate dialect.
  - Created Maven wrapper scripts `mvnw.cmd` and `mvnw.ps1`.
- **Health Check Endpoint (`HealthController`)**:
  - `GET /api/health` returning `{"status": "UP", "application": "WasteWatch"}`.
- **Global Exception Handling (`GlobalExceptionHandler`)**:
  - `@RestControllerAdvice` handling `ResourceNotFoundException` (404), `DuplicateResourceException` (409), `InvalidRequestException` (400), validation errors (400), `BadCredentialsException` (401), and `AccessDeniedException` (403).
- **Application Startup Tests (`WasteWatchApplicationTests`, `HealthControllerTest`)**:
  - Context load and health endpoint verification.

---

## [Phase 1: Project Foundation] — 2026-10-08

### Added
- **Core Vision & Context (`PROJECT_CONTEXT.md`)**:
  - Detailed product definition for the WasteWatch waste-segregation monitoring and compliance platform.
  - Specified user personas and capabilities for `ADMIN` and `VOLUNTEER` roles.
  - Formulated the 7-level domain hierarchy: `City` ➔ `Area` ➔ `Building` ➔ `Floor` ➔ `Room` ➔ `Inspection` ➔ `Violation`.
  - Documented the planned 100-point compliance scoring model.
- **Architectural Blueprint (`ARCHITECTURE.md`)**:
  - Established layered flow: `Frontend (Vanilla JS)` ➔ `REST API` ➔ `Controller` ➔ `Service` ➔ `Repository` ➔ `Entity` ➔ `H2`.
- **Relational Database Design (`DATABASE_DESIGN.md`)**:
  - Designed planned database tables and relationships.
- **REST API Specifications (`API_DOCUMENTATION.md`)**:
  - Documented REST endpoints and response envelopes.
- **Status & Handoff Tracking (`DEVELOPMENT_STATUS.md`)**:
  - Created development status matrix and roadmap checklist.
