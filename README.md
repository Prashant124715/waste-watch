# WasteWatch — Source Segregation Compliance & Governance Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![Vanilla Web](https://img.shields.io/badge/Frontend-Vanilla%20HTML5%20%2F%20CSS3%20%2F%20ES6+-blue.svg)](https://developer.mozilla.org/)
[![Database](https://img.shields.io/badge/Database-H2%20(PostgreSQL--Ready)-yellow.svg)](https://h2database.com/)
[![Tests](https://img.shields.io/badge/Tests-90%2F90%20Passing-success.svg)](https://junit.org/)

> **WasteWatch** is a source-level waste segregation monitoring, auditing, and municipal governance platform. Built as a high-impact CivicTech prototype, it equips city administrators with real-time compliance intelligence while providing field volunteers with a mobile-first, QR-enabled audit tool.

---

## 🌟 Key Capabilities

### 1. 📱 Volunteer Field Audit & QR Workflow (Phases 6 & 12)
- **Deterministic QR Identification**: Scans or manually resolves tokens formatted as `WASTEWATCH-ROOM-{BLD}-{ROOM}` via safe lookup endpoint `GET /api/rooms/qr/{token}`.
- **Authoritative 100-Point Scoring Engine**: Evaluates wet waste (25), dry waste (25), sanitary/hazardous (20), color-coded bins (15), legible labels (10), and mixed contamination (5).
- **Accidental Cooldown Protection**: 30-second per-room cooldown prevents duplicate submissions.
- **Identity Isolation**: Audit records are permanently bound to the authenticated JWT identity.

### 2. 🚨 Automated Violation & Escalation Subsystem (Phase 13)
- **Automatic Incident Creation**: Triggered instantly whenever an audit score falls below 40 (`POOR`).
- **Severity Mapping**: 0–19 (`CRITICAL`), 20–39 (`HIGH`), 40–59 (`MEDIUM`).
- **Repeat Non-Compliance Detection**: Rooms with 3 consecutive `POOR` scores are flagged as `PERSISTENT_NON_COMPLIANCE` with `CRITICAL` severity.
- **Standardized Lifecycle**: `OPEN` ➔ `ACKNOWLEDGED` ➔ `ACTION_REQUIRED` ➔ `RESOLVED`.

### 3. 🔔 In-App Notifications Subsystem (Phase 13)
- Real-time alerts for critical violations, persistent repeat offenders, and low-coverage zones.
- Support for unread counts, mark-as-read, and batch dismissal.

### 4. 👥 Volunteer Governance & Privilege Protection (Phase 14)
- View volunteer roster, active status, total audits, and unique rooms inspected.
- Single-click activation/deactivation (`ACTIVE` / `INACTIVE`).
- **Privilege Escalation Defense**: Hardened backend rejects any attempt to modify administrative accounts through volunteer management endpoints.

### 5. 📊 Multi-Dimensional Reporting & Streaming CSV Export (Phase 14)
- Dynamic reports across 6 perspectives: Overview, Areas, Buildings, Coverage, Violations, and Volunteers.
- Dynamic date bounding (`startDate` to `endDate`).
- **Authentic CSV Downloads**: Server streams RFC-4180 compliant CSV files with `Content-Disposition: attachment`.

### 6. 🛡️ Immutable Security Audit Trail (Phase 14)
- Captures administrative logins, infrastructure mutations, volunteer toggles, and violation resolutions.
- Multi-criteria JPQL filtering by action, entity type, date range, and keyword.
- Zero password or credential leakage in audit logs.

### 7. 🗺️ Interactive Geographic Map (Phase 11)
- Leaflet.js map with CartoDB dark tiles.
- Color-coded compliance pins, building popup scorecards, and live filtering.

### 8. 🧠 Decision Support Analytics (Phase 10)
- **Area Health Score Formula**:
  $$\text{Area Health Score} = (0.70 \times \text{Avg Compliance}) + (0.20 \times \text{Inspection Coverage \%}) + (0.10 \times \text{Violation Resolution Rate \%})$$
- Coverage-based confidence ratings (`HIGH`, `MEDIUM`, `LOW`).
- Time-series trend analysis and repeat offender hotspot detection.

---

## 🏗️ Technology Architecture

```
waste-watch/
├── backend/                     # Java 17 + Spring Boot 3.2.5 REST Service
│   ├── src/main/java/com/wastewatch/
│   │   ├── config/              # Security, CORS, DataInitializer
│   │   ├── controller/          # Clean REST endpoints (No business logic)
│   │   ├── dto/                 # Request & response data transfer contracts
│   │   ├── entity/              # 8 JPA entities (ANSI-SQL compatible)
│   │   ├── repository/          # Spring Data JPA repositories
│   │   ├── security/            # Stateless JJWT filter, UserPrincipal, BCrypt
│   │   └── service/             # Business engine & scoring algorithms
│   └── pom.xml
└── frontend/                    # Vanilla Web Client (Zero-Framework)
    ├── css/                     # HSL Design tokens, component library, layout
    ├── js/                      # ApiClient, sidebar, helpers, QR generator
    ├── admin/                   # 9 Enterprise Administrative consoles
    ├── volunteer/               # Mobile-first field inspection pages
    └── index.html               # Role-based authentication portal
```

---

## 🚀 Quick Start Guide

### Prerequisites
- **Java Development Kit (JDK)**: Java 17 or higher
- **Node.js**: (optional, for frontend local dev server) or any static web server

### 1. Launch the Backend Engine
Open a terminal in `waste-watch/backend`:
```powershell
.\mvnw.cmd spring-boot:run
```
*The Spring Boot REST API starts on `http://localhost:8080`.*  
*H2 In-Memory Console available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:wastewatchdb`, User: `sa`, Password: `<empty>`).*

### 2. Launch the Frontend Client
Open a second terminal in `waste-watch/frontend`:
```powershell
npm start
```
*The web portal opens at `http://localhost:3000`.*

---

## 🔑 Demo Credentials

| Role | Username | Password | Access Scope |
| :--- | :--- | :--- | :--- |
| **Municipal Administrator** | `admin` | `admin123` | Full dashboard, reports, audit logs, violations, areas, volunteers |
| **Field Volunteer #1** | `volunteer` | `volunteer123` | Room audits, QR scanning, personal inspection history |
| **Field Volunteer #2** | `volunteer2` | `volunteer123` | Secondary auditor for multi-user compliance tracking |

---

## 🧪 Automated Test Suite

The backend includes comprehensive unit, integration, and security tests across all 14 phases:

```powershell
cd waste-watch/backend
.\mvnw.cmd test
```

**Results: 90 tests run, 0 failures, 0 errors, 100% pass rate.**

To build the production JAR:
```powershell
.\mvnw.cmd package
```
*Produces `backend/target/wastewatch-backend-0.1.0-SNAPSHOT.jar`.*

---

## 📄 Project Documentation Index

- [`PROJECT_CONTEXT.md`](PROJECT_CONTEXT.md) — Strategic problem statement, municipal persona models, domain hierarchy.
- [`DEVELOPMENT_STATUS.md`](DEVELOPMENT_STATUS.md) — Phase-by-phase implementation tracking, final hardening review, and H2 rationale.
- [`ARCHITECTURE.md`](ARCHITECTURE.md) — Tiered architecture, security model, and subsystem specifications.
- [`DATABASE_DESIGN.md`](DATABASE_DESIGN.md) — Relational schema, ER diagrams, entity mappings, and indexing.
- [`API_DOCUMENTATION.md`](API_DOCUMENTATION.md) — REST API catalog, request/response contracts, and status codes.
- [`CHANGELOG.md`](CHANGELOG.md) — Chronological development history through final release.
