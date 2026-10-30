# WasteWatch — Source Segregation Compliance & Governance Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![Frontend](https://img.shields.io/badge/Frontend-Vanilla%20HTML5%20%2F%20CSS3%20%2F%20ES6+-blue.svg)](https://developer.mozilla.org/)
[![Database](https://img.shields.io/badge/Database-H2%20(File--Based%20Persistence)-yellow.svg)](https://h2database.com/)
[![Tests](https://img.shields.io/badge/Tests-161%2F161%20Passing-success.svg)](https://junit.org/)

> **WasteWatch** is an enterprise-grade source-level waste segregation monitoring, auditing, and municipal governance platform. Built as a high-impact CivicTech prototype, it equips city administrators with real-time compliance intelligence while providing civic volunteers with a mobile-first, eco-themed field audit application.

---

## 🌟 Key Capabilities

### 1. 📱 Volunteer Field App & Building Inspection Workflow
- **Streamlined Building-Level Audits**: Volunteers select or search for residential housing societies and commercial buildings directly (`City → Area → Building → Inspection → Violation`).
- **Live Dynamic SVG Score Ring**: Real-time compliance score dial that updates dynamically as segregation checklist criteria are toggled.
- **Accidental Cooldown Protection**: 30-second per-building cooldown per volunteer prevents double-tap duplicate submissions.
- **Identity Isolation**: Audit records are permanently bound to the authenticated JWT principal.

### 2. 📝 Volunteer Self-Registration & Verification Governance
- **Public Registration Portal**: Prospective volunteers apply via `/register.html` providing contact details, residential locality, and motivation.
- **Verification Lifecycle**: New accounts start in `PENDING` status with field audit submissions safely guarded.
- **Administrative Application Console**: Municipal officials review applicant details on `/admin/volunteers.html` and single-click **Approve** (`POST /api/volunteers/{id}/approve`) or **Reject** (`POST /api/volunteers/{id}/reject`).
- **Privilege Escalation Defense**: Anti-tampering guards prevent administrative accounts from being altered via volunteer governance endpoints.

### 3. ⚖️ Authoritative 100-Point Compliance Scoring Engine
- **Objective Criteria Weights**: Wet Waste Separated (25 pts), Dry Waste Separated (25 pts), Sanitary/Reject Waste Separated (20 pts), Correct Bins Used (15 pts), Bins Labelled (10 pts), No Mixed Contamination (5 pts).
- **Classification Tiers**: `EXCELLENT` (80–100), `GOOD` (60–79), `NEEDS_IMPROVEMENT` (40–59), `POOR` (0–39).
- **Contamination Guard**: Mixed waste points are guarded: cannot be awarded if all waste streams fail segregation.

### 4. 🚨 Automated Violation & Escalation Subsystem
- **Instant Automatic Ticketing**: Automatically generated whenever an audit score falls below 40 (`POOR`).
- **Severity Mapping**: 0–19 points (`CRITICAL`, assessed fine: ₹500), 20–39 points (`HIGH`, assessed fine: ₹250).
- **Repeat Non-Compliance Detection**: Facilities with 3 consecutive `POOR` scores trigger an automated `PERSISTENT_NON_COMPLIANCE` escalation alert for administrators.
- **Standardized Lifecycle**: `OPEN` ➔ `ACKNOWLEDGED` ➔ `ACTION_REQUIRED` ➔ `RESOLVED`.
- **Mandatory Resolution Notes**: Terminal resolution requires official corrective action notes and fine verification.

### 5. 🎨 Purpose-Built UI: Eco Field App & Glassmorphism Admin
- **Eco-Themed Field App**: Features custom vector eco scenery artwork (`inspect-bg-scene.svg`), botanical accents (`sidebar-leaves.svg`), and natural green design tokens (`volunteer-inspect.css`).
- **Glassmorphism Municipal Consoles**: 9 administrative consoles styled with translucent frosted-glass panels (`admin-theme.css`) and high-resolution environmental thematic renders (`assets/backgrounds/`).
- **Intro Splash Animation**: Interactive animated intro overlay in `index.html` with halo ring, glowing logo, and auto-dismiss/click-to-enter.

### 6. 🗺️ Interactive Geographic Map (Leaflet.js)
- Full-screen Leaflet.js OpenStreetMap console and embedded dashboard widget with CartoDB dark tiles.
- Color-coded compliance pins (`GREEN`, `YELLOW`, `ORANGE`, `RED`, `GRAY`).
- Animated pulsing rings (`.marker-hotspot`) for high-risk buildings and area hotspot sidebars.
- "Location unavailable" drawer isolating facilities lacking GPS coordinates to prevent map canvas failures.

### 7. 🧠 Decision Support Analytics
- **Standardized Area Health Score Formula**:
  $$\text{Area Health Score} = (0.70 \times \text{Avg Compliance}) + (0.20 \times \text{Inspection Coverage \%}) + (0.10 \times \text{Violation Resolution Rate \%})$$
- Coverage-based confidence ratings (`HIGH`, `MEDIUM`, `LOW`).
- Time-series trend analysis (daily, weekly, monthly) and period-over-period improvement tracking.

### 8. 📊 Multi-Dimensional Reporting & Streaming CSV Export
- Dynamic reporting across 7 perspectives: `overview`, `areas`, `buildings`, `coverage`, `violations`, `volunteers`, and `inspections`.
- Dynamic ISO-8601 date bounding (`from` and `to`).
- Streaming RFC-4180 compliant CSV exports with `Content-Disposition: attachment`.

### 9. 🛡️ Immutable Security Audit Trail
- Comprehensive audit logging of user logins, infrastructure mutations, volunteer approvals, inspection creations, and violation resolutions.
- Multi-criteria filtering by action, entity type, actor ID, and date range.
- Zero credential or token leakage.

### 10. 🏷️ QR Code Subsystem (Google ZXing)
- Standardized token format: `QR-{BUILDING_CODE}-{ROOM_NUMBER}` (e.g. `QR-GVH-101`).
- Server-side raw PNG byte streaming and Base64 data URLs.
- Safe metadata lookup preventing sensitive entity exposure.

---

## 🏗️ Technology Architecture

```
waste-watch/
├── backend/                     # Java 17 + Spring Boot 3.2.5 REST Service
│   ├── src/main/java/com/wastewatch/
│   │   ├── config/              # SecurityConfig, DataInitializer, DatabaseMigration
│   │   ├── controller/          # 14 REST Controllers (No business logic)
│   │   ├── dto/                 # Request & response data transfer contracts
│   │   ├── entity/              # 8 JPA entities (ANSI-SQL compatible)
│   │   ├── exception/           # GlobalExceptionHandler & ErrorResponse
│   │   ├── repository/          # Spring Data JPA repositories with JPQL queries
│   │   ├── security/            # Stateless JJWT filter, UserPrincipal, BCrypt
│   │   └── service/             # Domain logic, scoring algorithms, reporting
│   ├── data/                    # Local file-based H2 database (git-ignored)
│   │   ├── wastewatchdb.mv.db
│   │   └── wastewatchdb.trace.db
│   └── pom.xml                  # Maven dependencies & build plugins
└── frontend/                    # Vanilla Web Client (Zero-Framework Architecture)
    ├── index.html               # Main login portal with intro splash animation
    ├── login.html               # Redirect shim to index.html
    ├── register.html            # Volunteer self-registration portal
    ├── serve.json               # Clean URL configuration & rewrites
    ├── assets/                  # Brand assets, stickers, backgrounds & vector SVGs
    │   ├── backgrounds/         # 9 thematic glassmorphism background renders
    │   ├── inspect-bg-scene.svg # Eco skyline & 3-bin vector illustration
    │   ├── sidebar-leaves.svg   # Botanical leaf accent for volunteer sidebar
    │   └── sticker-*.png        # 7 civic-tech stickers (dustbin, truck, earth, etc.)
    ├── css/                     # Modular CSS stylesheets
    │   ├── variables.css        # Centralized CSS design tokens (HSL color palette)
    │   ├── base.css             # Resets, typography, utilities
    │   ├── layout.css           # Sidebar, topbar, grid shells
    │   ├── components.css       # Buttons, form controls, tables, modals, badges, toasts
    │   ├── admin-theme.css      # Glassmorphism theme for 9 municipal admin consoles
    │   └── volunteer-inspect.css# Eco-themed stylesheet for volunteer Field App
    ├── js/                      # JavaScript modules
    │   ├── api/client.js        # ApiClient singleton for all REST endpoints & token management
    │   ├── utils/helpers.js     # Auth, Toast, Fmt, and DOM utilities
    │   ├── utils/sidebar.js     # Dynamic role-based navigation sidebar injector
    │   └── lucide.min.js        # Offline Lucide SVG icon library
    ├── admin/                   # 9 Municipal Administrative Consoles
    │   ├── dashboard.html       # Executive KPI overview & embedded Leaflet compliance map
    │   ├── areas.html           # Areas & Buildings hierarchy management
    │   ├── inspections.html     # Historical audit records with filters & export
    │   ├── violations.html      # Violation management & resolution modal
    │   ├── volunteers.html      # Volunteer application review, approval & governance
    │   ├── map.html             # Full-screen Leaflet.js OpenStreetMap compliance map
    │   ├── analytics.html       # Decision support analytics & Area Health Scores
    │   ├── reports.html         # Multi-dimensional reporting & CSV export
    │   └── audit-logs.html      # Immutable security audit trail viewer
    └── volunteer/               # Mobile-First Volunteer Field App
        ├── inspect.html         # 3-step building waste audit with live SVG score dial
        └── history.html         # Personal audit history, KPI cards & export
```

---

## 🚀 Quick Start Guide

### Prerequisites
- **Java Development Kit (JDK)**: Java 17 or higher
- **Node.js**: (v16+) for frontend local static server

---

### 1. Launch the Backend Engine
Open a terminal in `backend/`:
```powershell
.\mvnw.cmd spring-boot:run
```
*The Spring Boot REST API starts on `http://localhost:8080`.*  
*The development profile uses file-based H2 persistence at `backend/data/wastewatchdb.mv.db`; records survive restarts. The H2 console is available locally at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/wastewatchdb`, user `sa`, empty password for a new database).*

#### Optional: Database Password
If an existing local database was created with a custom password, set it before starting the backend:
```powershell
$env:WASTEWATCH_DB_PASSWORD = "<your existing database password>"
```

#### Back Up or Restore Development Database
Stop the backend process first to ensure H2 has flushed all pending disk writes.
```powershell
# Backup database
Copy-Item .\backend\data\wastewatchdb.mv.db "$env:USERPROFILE\wastewatchdb-backup.mv.db"
```
To restore, copy the backup file back to `backend/data/wastewatchdb.mv.db` before restarting.

---

### 2. Launch the Frontend Client
Open a second terminal in `frontend/`:
```bash
npm start
# or
npm run dev
```
*The web portal opens at `http://localhost:3000`.*

---

## 🔑 Default Demo Credentials

| Role | Username | Password | Email | Status | Access Scope |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Municipal Administrator** | `admin` | `admin123` | `admin@wastewatch.org` | `ACTIVE` | Complete administrative consoles, reports, audit logs, violations, areas |
| **Field Inspector Volunteer** | `volunteer` | `volunteer123` | `volunteer@wastewatch.org` | `ACTIVE` | Building inspections, live score ring, personal audit history |
| **Field Inspector Volunteer #2** | `volunteer_priya` | `volunteer123` | `priya.sharma@wastewatch.org` | `ACTIVE` | Secondary auditor for multi-user compliance tracking |
| **Field Inspector Volunteer #3** | `volunteer_rohan` | `volunteer123` | `rohan.mehta@wastewatch.org` | `INACTIVE` | Inactive auditor account demonstrating governance state |

---

## 🧪 Automated Test Suite

The backend includes comprehensive unit, integration, and security tests across all 14 phases and recent architectural scope enhancements:

```powershell
cd backend
.\mvnw.cmd test
```

**Results: 161 tests run across 17 test suites, 0 failures, 0 errors, 0 skipped, 100% pass rate.**

To build the standalone production JAR:
```powershell
.\mvnw.cmd clean package
```
*Produces `backend/target/wastewatch-backend-0.1.0-SNAPSHOT.jar`.*

---

## 📄 Project Documentation Index

- [`PROJECT_CONTEXT.md`](PROJECT_CONTEXT.md) — Strategic problem statement, municipal personas, domain hierarchy, scoring model, and roadmap.
- [`DEVELOPMENT_STATUS.md`](DEVELOPMENT_STATUS.md) — Implementation tracking, test suite verification, phase checklist, and H2 persistence configuration.
- [`ARCHITECTURE.md`](ARCHITECTURE.md) — Tiered clean architecture, layer responsibilities, security models, and subsystem deep dives.
- [`DATABASE_DESIGN.md`](DATABASE_DESIGN.md) — Relational schema, ER diagrams, 8 table specifications, indexes, and runtime migrations.
- [`API_DOCUMENTATION.md`](API_DOCUMENTATION.md) — REST API catalog across all 14 controllers, request/response contracts, and error structures.
- [`CHANGELOG.md`](CHANGELOG.md) — Chronological version history through current release.
- [`backend/README.md`](backend/README.md) — Backend-specific architecture, packages, dependencies, configuration, and execution commands.
- [`frontend/README.md`](frontend/README.md) — Frontend-specific architecture, design token system, page inventory, and visual theme guide.
