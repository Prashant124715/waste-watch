# WasteWatch — Backend REST API & Domain Engine

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![Database](https://img.shields.io/badge/Database-H2%20(File--Based%20Persistence)-yellow.svg)](https://h2database.com/)
[![Tests](https://img.shields.io/badge/Tests-161%2F161%20Passing-success.svg)](https://junit.org/)

The **WasteWatch Backend** is an enterprise-grade, strongly typed REST API service built with **Spring Boot 3.2.5** and **Java 17**. It delivers source-level waste segregation auditing, authoritative 100-point compliance scoring, automated violation escalation, in-app notifications, decision support analytics, geographic mapping, volunteer application governance, and immutable security audit trails.

---

## 🏗️ Technology Stack

- **Language / Runtime**: Java 17 LTS
- **Framework**: Spring Boot 3.2.5
- **Web Layer**: Spring MVC REST Controllers (`spring-boot-starter-web`)
- **Persistence Layer**: Spring Data JPA & Hibernate 6 (`spring-boot-starter-data-jpa`)
- **Security & Authorization**: Spring Security 6 (`spring-boot-starter-security`) with stateless JJWT 0.12.5 and BCrypt password hashing
- **Validation**: Jakarta Validation (`spring-boot-starter-validation` with Hibernate Validator)
- **Database Engine**: H2 Database 2.x (runtime file-based persistence for `dev`, in-memory for `test`)
- **QR Code Engine**: Google ZXing 3.5.3 (`zxing:core`, `zxing:javase`)
- **Testing Framework**: JUnit 5, Mockito, Spring Security Test, Spring Boot Test (`spring-boot-starter-test`)
- **Build Tool**: Apache Maven (via `mvnw.cmd` / `mvnw.ps1` / `mvnw`)

---

## 📁 Package Architecture

```
backend/
├── pom.xml                                     # Maven dependencies and build plugins
├── data/                                       # Local file-based H2 database (git-ignored)
│   ├── wastewatchdb.mv.db
│   └── wastewatchdb.trace.db
└── src/
    ├── main/
    │   ├── java/com/wastewatch/
    │   │   ├── WasteWatchApplication.java      # Application bootstrap class
    │   │   ├── config/                         # Configuration and startup runners
    │   │   │   ├── DataInitializer.java        # Development demo data seeder
    │   │   │   ├── DatabaseMigration.java      # Runtime check constraint & nullability migrator
    │   │   │   └── SecurityConfig.java         # Spring Security filter chain, CORS & RBAC
    │   │   ├── controller/                     # REST API Controllers (14 Controllers)
    │   │   │   ├── AnalyticsController.java    # Decision support analytics endpoints
    │   │   │   ├── AreaController.java         # Municipal areas and wards CRUD
    │   │   │   ├── AuditLogController.java     # Governance audit log query endpoint
    │   │   │   ├── AuthController.java         # Login, registration, and user profile
    │   │   │   ├── BuildingController.java     # Buildings and housing societies CRUD
    │   │   │   ├── DashboardController.java    # Executive summary metrics and trends
    │   │   │   ├── HealthController.java       # Health check probe (/api/health)
    │   │   │   ├── InspectionController.java   # Building-level inspection workflows
    │   │   │   ├── MapController.java          # Geo-markers and hotspot spatial data
    │   │   │   ├── NotificationController.java # In-app notifications and read toggles
    │   │   │   ├── ReportController.java       # Multi-dimensional reporting & CSV streaming
    │   │   │   ├── RoomController.java         # Room metadata, QR tokens & image generation
    │   │   │   ├── ViolationController.java    # Non-compliance tracking & resolution
    │   │   │   └── VolunteerController.java    # Volunteer roster, verification & performance
    │   │   ├── dto/                            # Data Transfer Objects (contracts by domain)
    │   │   │   ├── ApiResponse.java            # Standard generic response wrapper
    │   │   │   ├── HealthResponseDto.java      # Health status envelope
    │   │   │   ├── analytics/                  # Overview, area, building, trend, hotspot DTOs
    │   │   │   ├── area/                       # AreaRequestDto, AreaResponseDto
    │   │   │   ├── audit/                      # AuditLogResponseDto
    │   │   │   ├── auth/                       # LoginRequestDto, RegisterRequestDto, UserProfileDto, AuthResponseDto
    │   │   │   ├── building/                   # BuildingRequestDto, BuildingResponseDto
    │   │   │   ├── dashboard/                  # DashboardSummaryDto, AreaComplianceDto, DailyTrendDto, AttentionRequiredDto
    │   │   │   ├── inspection/                 # InspectionRequestDto, InspectionResponseDto, ScoreBreakdownDto, ComplianceResult
    │   │   │   ├── map/                        # BuildingMapMarkerDto, AreaHotspotDto, AreaMapSummaryDto
    │   │   │   ├── notification/               # NotificationResponseDto
    │   │   │   ├── report/                     # Overview, Area, Building, Coverage, Violation, Volunteer report DTOs
    │   │   │   ├── room/                       # RoomRequestDto, RoomResponseDto, RoomDetailDto
    │   │   │   ├── violation/                  # ViolationResponseDto, ViolationStatusUpdateDto, ViolationResolveDto
    │   │   │   └── volunteer/                  # VolunteerSummaryDto, VolunteerStatusUpdateDto
    │   │   ├── entity/                         # JPA Relational Entities (8 Entities)
    │   │   │   ├── AppUser.java                # System users (Admin, Volunteers)
    │   │   │   ├── Area.java                   # Administrative wards and geographic sectors
    │   │   │   ├── Building.java               # Housing societies and commercial buildings
    │   │   │   ├── Room.java                   # Deprecated unit entity (schema compatibility)
    │   │   │   ├── Inspection.java             # Field audits tied directly to Building
    │   │   │   ├── Violation.java              # Non-compliance incidents tied to Building
    │   │   │   ├── Notification.java           # In-app alerts for admins and volunteers
    │   │   │   ├── AuditLog.java               # Immutable security governance logs
    │   │   │   └── enums/                      # Domain enums
    │   │   │       ├── BuildingType.java       # RESIDENTIAL, COMMERCIAL, MIXED, INSTITUTIONAL
    │   │   │       ├── ComplianceClassification.java # EXCELLENT, GOOD, NEEDS_IMPROVEMENT, POOR
    │   │   │       ├── InspectionStatus.java   # COMPLETED, PENDING, FLAGGED
    │   │   │       ├── NotificationType.java   # INSPECTION_ASSIGNED, VIOLATION_RAISED, VIOLATION_SEVERITY_HIGH, PERSISTENT_NON_COMPLIANCE, COVERAGE_ALERT, SYSTEM
    │   │   │       ├── UserRole.java           # ADMIN, VOLUNTEER
    │   │   │       ├── UserStatus.java         # ACTIVE, INACTIVE, SUSPENDED, PENDING, REJECTED
    │   │   │       ├── ViolationSeverity.java  # LOW, MEDIUM, HIGH, CRITICAL
    │   │   │       └── ViolationStatus.java    # OPEN, ACKNOWLEDGED, ACTION_REQUIRED, RESOLVED
    │   │   ├── exception/                      # Global exception handling & custom exceptions
    │   │   │   ├── ErrorResponse.java          # Standard machine-readable error envelope
    │   │   │   ├── GlobalExceptionHandler.java # @RestControllerAdvice mapping status codes
    │   │   │   ├── ResourceNotFoundException.java # HTTP 404
    │   │   │   ├── DuplicateResourceException.java# HTTP 409
    │   │   │   └── InvalidRequestException.java   # HTTP 400
    │   │   ├── repository/                     # Spring Data JPA Repositories
    │   │   │   ├── AppUserRepository.java
    │   │   │   ├── AreaRepository.java
    │   │   │   ├── AuditLogRepository.java
    │   │   │   ├── BuildingRepository.java
    │   │   │   ├── InspectionRepository.java
    │   │   │   ├── NotificationRepository.java
    │   │   │   ├── RoomRepository.java
    │   │   │   └── ViolationRepository.java
    │   │   ├── security/                       # Security & JWT Token Infrastructure
    │   │   │   ├── CustomUserDetailsService.java # User loader by username or email
    │   │   │   ├── JwtAuthenticationEntryPoint.java # 401 Unauthorized handler
    │   │   │   ├── JwtAuthenticationFilter.java  # Bearer token validation per request
    │   │   │   ├── JwtTokenProvider.java         # JJWT sign & parse utility
    │   │   │   └── UserPrincipal.java            # Spring Security UserDetails adapter
    │   │   └── service/                        # Business Logic & Algorithms
    │   │       ├── AnalyticsService.java       # Analytics interface
    │   │       ├── AnalyticsServiceImpl.java   # Health scores, trends, hotspots engine
    │   │       ├── AreaService.java            # Area management logic
    │   │       ├── AuditLogService.java        # Governance audit trail logging
    │   │       ├── AuthService.java            # Login and volunteer registration logic
    │   │       ├── BuildingService.java        # Building lookup and search
    │   │       ├── ComplianceScoringService.java# Authoritative 100-point scoring algorithm
    │   │       ├── DashboardService.java       # Executive KPI calculations
    │   │       ├── InspectionService.java      # Building-level inspection workflow
    │   │       ├── MapService.java             # Geographic data & marker builder
    │   │       ├── NotificationService.java    # In-app alert dispatching
    │   │       ├── QrCodeService.java          # ZXing PNG byte & data-URL generation
    │   │       ├── ReportService.java          # Multi-dimensional reports & CSV generator
    │   │       ├── RoomService.java            # Legacy room CRUD & QR code endpoints
    │   │       ├── ViolationService.java       # Auto-violation, repeat detection, fines
    │   │       └── VolunteerService.java       # Volunteer applications, approval & stats
    │   └── resources/
    │       ├── application.yml                 # Base server, profile, logging & JWT secrets
    │       └── application-dev.yml             # Development profile (file-based H2 persistence)
    └── test/
        ├── java/com/wastewatch/                # 17 automated test suites (161 tests)
        └── resources/
            └── application.yml                 # Test profile (isolated in-memory H2)
```

---

## ⚙️ Configuration & Database Profiles

### 1. Development Profile (`dev` — Active by default)
- **Database Engine**: File-based H2 stored at `backend/data/wastewatchdb.mv.db`.
- **JDBC URL**: `jdbc:h2:file:./data/wastewatchdb;DB_CLOSE_DELAY=-1`
- **Persistence**: All data persists across application restarts.
- **Hibernate DDL Auto**: `update` (preserves existing rows, applies non-destructive schema changes).
- **H2 Web Console**: Enabled at `http://localhost:8080/h2-console` (local access only).
- **Environment Variables**:
  - `WASTEWATCH_DB_USERNAME`: Database user (defaults to `sa`).
  - `WASTEWATCH_DB_PASSWORD`: Database password (defaults to empty string for new databases).
- **Startup Migration (`DatabaseMigration`)**:
  Executes automatically at startup (`@Order(1)`). Drops legacy H2 `APP_USERS.STATUS` check constraints and adds `CHECK_APP_USERS_STATUS` with `('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING', 'REJECTED')`. Updates `INSPECTIONS.ROOM_ID` and `VIOLATIONS.ROOM_ID` nullability and backfills `BUILDING_ID`.
- **Startup Seeding (`DataInitializer`)**:
  Runs conditionally only if `userRepository.count() == 0`. Automatically seeds 1 Admin, 3 Volunteers, 3 Areas, 4 Buildings, 8 Rooms, 7 Inspections, 3 Violations, 3 Notifications, and 6 Audit Logs.

### 2. Test Profile (`test`)
- **Database Engine**: In-memory H2 (`jdbc:h2:mem:wastewatchdb_test;DB_CLOSE_DELAY=-1`).
- **Hibernate DDL Auto**: `create-drop` (fresh schema per test run).
- **H2 Console**: Disabled during testing.

---

## 🔑 Default Seed Accounts & Credentials

| Role | Username | Password | Email | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Municipal Administrator** | `admin` | `admin123` | `admin@wastewatch.org` | `ACTIVE` |
| **Field Inspector Volunteer** | `volunteer` | `volunteer123` | `volunteer@wastewatch.org` | `ACTIVE` |
| **Field Inspector Volunteer #2** | `volunteer_priya` | `volunteer123` | `priya.sharma@wastewatch.org` | `ACTIVE` |
| **Field Inspector Volunteer #3** | `volunteer_rohan` | `volunteer123` | `rohan.mehta@wastewatch.org` | `INACTIVE` |

---

## 🌿 Core Business Subsystems

### 1. Authoritative 100-Point Compliance Scoring (`ComplianceScoringService`)
Field audits evaluate six distinct waste segregation criteria:
- **Wet Waste Separated**: 25 points
- **Dry Waste Separated**: 25 points
- **Sanitary / Reject Waste Separated**: 20 points
- **Correct Bins Used**: 15 points
- **Bins Clearly Labelled**: 10 points
- **No Mixed Contamination**: 5 points (guarded: only awarded if at least one waste stream is separated)

**Classification Tiers**:
- **80 – 100 points**: `EXCELLENT`
- **60 – 79 points**: `GOOD`
- **40 – 59 points**: `NEEDS_IMPROVEMENT`
- **0 – 39 points**: `POOR` (triggers automated violation ticket)

### 2. Building-Level Inspection Architecture
- Audits are conducted directly at the **Building** level (`POST /api/inspections` requires `buildingId`).
- `roomId` is optional and deprecated for backward database compatibility.
- 30-second submission cooldown per building per volunteer prevents accidental double-tap duplicates.
- Anti-impersonation: Inspection records are permanently bound to the authenticated JWT principal.
- Volunteer verification guard: Volunteers with status `PENDING` or `REJECTED` are denied submission rights.

### 3. Automated Violation & Repeat Non-Compliance Engine (`ViolationService`)
- Automated ticket generation on `POOR` scores (<40).
- Severity calculation:
  - 0–19 points: `CRITICAL` (assessed fine: ₹500.00)
  - 20–39 points: `HIGH` (assessed fine: ₹250.00)
- Lifecycle transitions: `OPEN` ➔ `ACKNOWLEDGED` ➔ `ACTION_REQUIRED` ➔ `RESOLVED`.
- Persistent Non-Compliance: If a building or room registers 3 consecutive `POOR` inspections, an automated `PERSISTENT_NON_COMPLIANCE` alert is dispatched to municipal administrators.

### 4. Decision Support Analytics & Health Score (`AnalyticsServiceImpl`)
Standardized Area Health Score Formula:
$$\text{Area Health Score} = (0.70 \times \text{Avg Compliance}) + (0.20 \times \text{Inspection Coverage \%}) + (0.10 \times \text{Violation Resolution Rate \%})$$

### 5. Multi-Dimensional Reporting & Streaming CSV (`ReportService`)
Streams authentic RFC-4180 compliant CSV exports with `Content-Disposition: attachment; filename="wastewatch-<type>-report.csv"` across 7 report perspectives: `overview`, `areas`, `buildings`, `coverage`, `violations`, `volunteers`, and `inspections`.

---

## 🚀 Execution & Build Commands

### Prerequisites
- **JDK**: Java 17 or higher
- **Maven**: Maven wrapper (`mvnw.cmd` on Windows, `./mvnw` on Linux/macOS) is included.

### Start Backend Engine
```powershell
# From the backend directory
.\mvnw.cmd spring-boot:run
```
*Backend runs on port `8080`. API Base: `http://localhost:8080/api`.*

### Run Automated Tests
```powershell
.\mvnw.cmd test
```
*Executes all 161 automated unit, integration, and security tests. 100% pass rate.*

### Build Production JAR
```powershell
.\mvnw.cmd clean package
```
*Generates executable standalone artifact at `backend/target/wastewatch-backend-0.1.0-SNAPSHOT.jar`.*

### Run Packaged JAR
```powershell
java -jar target/wastewatch-backend-0.1.0-SNAPSHOT.jar
```
