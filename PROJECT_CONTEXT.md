# WasteWatch — Project Context & Vision

## 1. Executive Summary

**WasteWatch** is a waste-segregation monitoring, auditing, and compliance management platform. It addresses the critical challenge of municipal solid waste management: ensuring waste segregation at the source (individual rooms, flats, offices, and buildings) through systematic inspections, objective compliance scoring, and real-time administrative intelligence.

The application serves two primary user categories:
1. **Volunteers / Field Inspectors**: Perform structured on-ground audits across rooms and buildings, evaluate waste segregation criteria, and record observations.
2. **Administrators / Municipal Supervisors**: Monitor macro and micro compliance metrics via an analytical dashboard, identify chronic non-compliance hotspots, track volunteer activity, and manage corrective enforcement.

While initialized as an academic / Capstone Engineering Project (CEP), WasteWatch is architected from day one with the modularity, maintainability, and clean domain boundaries required for evolution into a production-grade CivicTech SaaS platform.

---

## 2. Problem Statement

Improper waste segregation at source is the single largest bottleneck in modern urban waste management:
- **Lack of Source-Level Accountability**: Municipalities lack visibility into which specific households, apartments, or commercial units fail to segregate waste.
- **Subjective & Inconsistent Inspections**: Without structured criteria, field inspections rely on qualitative impressions rather than quantifiable compliance scores.
- **Absence of Actionable Analytics**: Administrative bodies cannot easily identify repeat offending areas, track inspection coverage, or measure the impact of awareness campaigns.
- **Paper-Based or Ad-Hoc Audits**: Existing workflows suffer from delayed reporting, lost records, and zero spatial (map-based) insights.

WasteWatch solves this by replacing ad-hoc inspections with a standardized, data-driven workflow featuring weighted scoring, geo-spatial visualization, and automated violation escalation.

---

## 3. User Roles & Personas

### 3.1. ADMIN (Supervisor / Municipal Official)
* **Objective**: Maintain complete operational oversight, allocate resources, and enforce municipal waste guidelines.
* **Key Responsibilities**:
  - Secure authentication and system administration.
  - Manage the geographic and structural hierarchy (Cities, Areas, Buildings, Floors, Rooms).
  - Manage volunteer accounts, verify credentials, and track inspection coverage.
  - Review live aggregate statistics: Overall Compliance Rate, Area-wise Health, Building-wise Leaderboards.
  - Inspect historical room-level audit trails and violation histories.
  - Analyze geographic maps with Leaflet.js (pinpoint high-risk buildings and compliance heatmaps).
  - Manage and track violation resolution workflows.
  - Generate exportable compliance summaries and operational reports.
  - Review system audit logs for accountability and traceability.

### 3.2. VOLUNTEER (Field Inspector / Auditor)
* **Objective**: Conduct rapid, standardized, on-ground inspections on mobile or tablet devices.
* **Key Responsibilities**:
  - Secure authentication and profile access.
  - View assigned inspection zones, buildings, and rooms.
  - Identify target units (manual selection or upcoming QR code scanning).
  - Execute inspections using a standardized checklist evaluating waste segregation conditions.
  - Record immediate remarks and capture photographic evidence (future phase).
  - Automatically calculate and present an instant compliance score (0–100).
  - Submit audit records with instant synchronization to the backend.
  - Review self-inspection history and performance milestones.

---

## 4. Core Domain Hierarchy

WasteWatch enforces a strict parent-child structural hierarchy to represent physical environments:

```
City
 └── Area (Ward, Sector, Neighborhood)
      └── Building (Society, Apartment Complex, Commercial Office)
           └── Floor
                └── Room / Unit (Flat, Office Suite, Tenement)
                     └── Inspection (Audit event conducted at a timestamp)
                          └── Violation (Logged when an inspection fails compliance thresholds)
```

### Hierarchy Rules:
- A **City** comprises multiple administrative **Areas**.
- An **Area** contains multiple **Buildings** (each associated with geographic coordinates).
- A **Building** contains multiple **Floors**.
- A **Floor** contains individual **Rooms** (the fundamental unit of inspection).
- An **Inspection** is tied strictly to a **Room**, executed by a **Volunteer** at a specific timestamp.
- A **Violation** is generated automatically or manually whenever an inspection score breaches minimum acceptable thresholds.

---

## 5. Authoritative Compliance Scoring Engine
 
To eliminate subjective bias, WasteWatch evaluates room segregation using a 100-point weighted scoring model implemented in `ComplianceScoringService`:
 
| Criteria | Weight | Evaluation Requirement |
| :--- | :---: | :--- |
| **Wet Waste Separated** | 25 pts | Biodegradable / organic waste stored exclusively in dedicated container |
| **Dry Waste Separated** | 25 pts | Recyclable paper, plastic, metal, and glass separated from moisture |
| **Sanitary / Reject Waste Separated** | 20 pts | Hazardous, medical, or sanitary waste wrapped and segregated |
| **Correct Bins Used** | 15 pts | Color-coded bins compliant with local norms (e.g., Green/Wet, Blue/Dry) |
| **Bins Clearly Labelled** | 10 pts | Legible labels or visual iconography on waste receptacles |
| **No Mixed Contamination** | 5 pts | Zero visible cross-contamination across segregated bins |
| **Total Available** | **100 pts** | Maximum Compliance Score |
 
### Compliance Classification Categories:
- **80 – 100 points**: `EXCELLENT` — Full compliance; model citizen/unit.
- **60 – 79 points**: `GOOD` — Acceptable compliance; minor improvements recommended.
- **40 – 59 points**: `NEEDS_IMPROVEMENT` — Non-compliant in key areas; formal advisory required.
- **0 – 39 points**: `POOR` — Severe non-compliance; automated violation flag generated.

### Scoring Result & Breakdown Model:
The scoring engine produces a structured `ComplianceResult` containing the computed total score, status tier, violation trigger, severity, and a granular `ScoreBreakdownDto` (`wetWaste`, `dryWaste`, `sanitaryWaste`, `correctBins`, `labelledBins`, `noMixedWaste`).

---

## 6. Technology Stack & Rationale

| Layer | Selected Technology | Architectural Rationale & Constraints |
| :--- | :--- | :--- |
| **Frontend UI** | **HTML5, CSS3, Vanilla JavaScript (ES6+)** | **Zero UI frameworks allowed** (No React, Angular, or Vue). Delivers zero-build-step simplicity, ultra-fast load times, complete transparency, and long-term maintainability for academic evaluation and lightweight mobile deployment. |
| **Backend Runtime** | **Java 17+ / Spring Boot 3.x** | Enterprise-grade, strongly typed, robust ecosystem with first-class REST, JPA, and security abstractions. |
| **Web / REST** | **Spring Web (MVC REST Controllers)** | Standardized JSON RESTful API endpoints following REST best practices. |
| **Persistence** | **Spring Data JPA / Hibernate** | Object-Relational Mapping providing clean repository interfaces and database portability. |
| **Security** | **Spring Security** | Robust Role-Based Access Control (`ROLE_ADMIN`, `ROLE_VOLUNTEER`) and authentication. |
| **Database** | **H2 (In-Memory / File-Mode)** | Lightweight, zero-external-dependency database for academic execution; easily switched to PostgreSQL/MySQL via JPA dialects. |
| **Build & Dependencies**| **Apache Maven** | Standardized Java dependency and lifecycle management. |
| **Mapping (Future)** | **Leaflet.js & OpenStreetMap** | Open-source, lightweight mapping library without proprietary API key requirements. |
| **Testing** | **JUnit 5, Mockito, Spring Boot Test**| Thorough unit and integration test coverage across service and controller layers. |

---

## 7. Important Architectural Decisions & Constraints

1. **Strict Separation of Concerns**:
   - `Frontend (Vanilla JS)` ➔ `REST API (JSON)` ➔ `Controller` ➔ `Service` ➔ `Repository` ➔ `Entity` ➔ `H2`.
   - Business logic must reside **strictly** within the Service layer — never in Controllers or Repositories.
2. **Database Portability**:
   - Standard JPA mappings with generic SQL conventions. Avoid vendor-specific H2 SQL dialects so moving to PostgreSQL or MySQL requires only `pom.xml` dependency and `application.yml` updates.
3. **No Frontend Frameworks**:
   - Strict constraint: **Do NOT use React, Angular, Vue, or Svelte**. Frontend components will be written in standards-compliant HTML5, modular CSS3, and modern Vanilla JavaScript.
4. **Mobile-First Volunteer Experience**:
   - Field inspectors operate on smartphones. Volunteer workflows must be fully responsive and touch-optimized.

---

## 8. Planned Feature Roadmap

The platform architecture is designed to accommodate the following 18 future capabilities:

1. **QR Code Identification**: Scan QR badges affixed at room/flat entryways to directly load the inspection form.
2. **Mobile-First Volunteer UI**: Fast, touch-friendly, offline-resilient inspection forms.
3. **Photo & Evidence Upload**: Attach visual verification to audits and violation reports.
4. **Dynamic Compliance Scoring**: Automated computation using the 100-point algorithm.
5. **Area Health Index**: Aggregated compliance metric reflecting regional hygiene levels.
6. **Inspection Coverage Analytics**: Percentage of registered rooms audited within a 30-day window.
7. **Repeat Violation Detection**: Automated flagging of entities with consecutive low scores.
8. **Violation Resolution Workflow**: Status tracking (`OPEN`, `NOTIFIED`, `ESCALATED`, `RESOLVED`).
9. **Notification System**: In-app and simulated alerts for scheduled audits and violations.
10. **Volunteer Performance Analytics**: Leaderboard tracking audits completed, accuracy, and active hours.
11. **Interactive Geographic Map**: Leaflet.js-powered visual mapping of audited buildings.
12. **Heatmap & Hotspot Analysis**: Density visualization of non-compliant zones.
13. **Inspection Scheduling**: Task assignment by admins to specific volunteers.
14. **Compliance & Audit Reports**: PDF/CSV exportable compliance audits.
15. **System Audit Logs**: Immutable log tracking user actions and critical entity modifications.
16. **Fine-Grained Role-Based Access Control**: Secure token-based authorization.
17. **Production Database Migration**: Frictionless deployment with PostgreSQL.
18. **Multi-Tenant / SaaS Expansion**: Future support for multiple municipalities/universities.

---

## 9. Current Status: Phase 7 — Compliance Scoring Engine (COMPLETED)

As of the completion of Phases 2 through 7:
- **Phase 1**: Architectural blueprint and documentation foundation established.
- **Phase 2**: Spring Boot 3.2.5 backend foundation initialized with H2 database, Maven wrapper, global exception handling, and `/api/health`.
- **Phase 3**: Relational domain entities (`AppUser`, `Area`, `Building`, `Room`, `Inspection`, `Violation`, `Notification`, `AuditLog`), enums, and JPA repositories implemented with automated dev seed data.
- **Phase 4**: Spring Security with stateless JWT authentication, BCrypt password hashing, role-based authorization (`ADMIN`, `VOLUNTEER`), `/api/auth/login`, and `/api/auth/me`.
- **Phase 5**: Full RESTful infrastructure management APIs implemented for `Area`, `Building`, and `Room`.
- **Phase 6**: Volunteer inspection submission with duplicate cooldown (30s) and anti-impersonation rules.
- **Phase 7**: Authoritative 100-point compliance scoring service (`ComplianceScoringService`) with granular point breakdown (`ScoreBreakdownDto`), null-safe evaluation, and full unit test coverage.
- **Development Test Accounts**:
  - Administrator: `admin` / `admin123` (`admin@wastewatch.org`)
  - Volunteer: `volunteer` / `volunteer123` (`volunteer@wastewatch.org`)

