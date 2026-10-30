# WasteWatch — Project Context & Vision

[![Project Stage](https://img.shields.io/badge/Status-Phase%2014%20Complete%20(Production--Ready)-success.svg)](#)
[![Domain](https://img.shields.io/badge/Domain-CivicTech%20%2F%20Waste%20Management-blue.svg)](#)
[![Architecture](https://img.shields.io/badge/Architecture-Building--Level%20Inspection%20Scope-green.svg)](#)

---

## 1. Executive Summary

**WasteWatch** is a source-level waste segregation monitoring, auditing, and municipal compliance governance platform. It directly tackles the critical bottleneck in municipal solid waste management: ensuring waste segregation at the source (housing societies, residential apartment complexes, commercial establishments, and institutions) through systematic field audits, objective compliance scoring, and real-time administrative intelligence.

The application serves two distinct user personas:
1. **Volunteers / Field Inspectors (`VOLUNTEER`)**: Perform structured, on-ground audits across buildings and housing societies, evaluate waste segregation criteria, view dynamic live scores, and submit audit observations.
2. **Administrators / Municipal Supervisors (`ADMIN`)**: Maintain operational oversight, review volunteer applications, monitor compliance intelligence via analytical dashboards, identify chronic non-compliance hotspots, resolve violation tickets, review security audit trails, and stream multi-dimensional CSV reports.

While initialized as a Capstone Engineering Project (CEP), WasteWatch is architected from day one with the modularity, maintainability, and clean domain boundaries required for evolution into a production-grade CivicTech SaaS platform.

---

## 2. Problem Statement

Improper waste segregation at source is the single largest point of failure in modern urban waste management:
- **Absence of Source-Level Accountability**: Municipalities lack visibility into which specific housing societies, apartments, or commercial facilities fail to segregate waste.
- **Subjective & Inconsistent Inspections**: Without structured criteria, field audits rely on qualitative impressions rather than quantifiable compliance scores.
- **Absence of Actionable Analytics**: Municipalities cannot easily identify repeat offending areas, track inspection coverage, or measure the impact of awareness campaigns.
- **Paper-Based or Ad-Hoc Audits**: Existing workflows suffer from delayed reporting, lost records, and zero spatial (map-based) insights.

WasteWatch solves this by replacing ad-hoc inspections with a standardized, data-driven workflow featuring weighted scoring, geo-spatial visualization, volunteer application verification, and automated violation escalation.

---

## 3. User Roles & Personas

### 3.1. ADMIN (Municipal Official / Ward Supervisor)
* **Objective**: Maintain complete operational oversight, verify volunteer applicants, allocate resources, and enforce municipal waste guidelines.
* **Key Responsibilities**:
  - Secure authentication and system administration.
  - Manage the geographic and structural hierarchy (Areas, Buildings).
  - Review volunteer registration applications and approve or reject applicants.
  - Review live aggregate statistics: Overall Compliance Rate, Area Health Scores, and Building Rankings.
  - Track violations, assess fines, and record official resolution notes upon re-inspection.
  - Analyze geographic maps with Leaflet.js (pinpoint high-risk buildings and compliance heatmaps).
  - Stream authentic RFC-4180 CSV reports across 7 operational dimensions.
  - Review immutable security audit logs for complete accountability.

### 3.2. VOLUNTEER (Field Inspector / Civic Auditor)
* **Objective**: Conduct rapid, standardized, on-ground inspections of housing societies on mobile or desktop devices.
* **Key Responsibilities**:
  - Self-register via the civic volunteer portal (`/register.html`).
  - View application status; submit inspections once verified and approved by administrators.
  - Search or select target housing societies and commercial buildings.
  - Execute audits using a standardized 6-point checklist evaluating waste segregation conditions.
  - View real-time compliance score updates on a dynamic SVG ring gauge.
  - Record observation remarks and attach optional photo evidence.
  - Review personal inspection history and performance metrics.

---

## 4. Core Domain Hierarchy & Scope

WasteWatch enforces a clean parent-child structural hierarchy:

```
City
 └── Area (Ward, Sector, Neighborhood)
      └── Building (Housing Society, Apartment Complex, Commercial Premise)
           └── Inspection (Audit event conducted at a timestamp)
                └── Violation (Logged automatically when an inspection score < 40)
```

### Hierarchy Rules:
1. A **City** comprises multiple administrative **Areas**.
2. An **Area** contains multiple **Buildings** (each associated with geographic GPS coordinates).
3. A **Building** is the primary audit entity and fundamental unit of inspection.
4. An **Inspection** is tied directly to a **Building**, executed by an authenticated **Volunteer** at a specific timestamp.
5. A **Violation** is generated automatically whenever a building inspection score breaches minimum acceptable thresholds (< 40 points).
6. **Scope Reduction on Rooms**: To streamline field inspections and align with municipal realities, the application workflow operates strictly at the **Building** level. Volunteers inspect buildings directly without room selection or scanning. The `Room` entity and database table are retained solely for backwards database compatibility.

---

## 5. Authoritative Compliance Scoring Engine

To eliminate subjective bias, WasteWatch evaluates waste segregation using a 100-point weighted scoring model implemented in `ComplianceScoringService`:

| Criteria | Weight | Evaluation Requirement |
| :--- | :---: | :--- |
| **Wet Waste Separated** | 25 pts | Biodegradable organic food scraps in dedicated green bin |
| **Dry Waste Separated** | 25 pts | Recyclable paper, plastic, metal, and glass in dedicated blue bin |
| **Sanitary / Reject Waste Separated** | 20 pts | Hazardous, medical, or sanitary waste wrapped in dedicated red bin |
| **Correct Bins Used** | 15 pts | Color-coded municipal standard bins present at collection point |
| **Bins Clearly Labelled** | 10 pts | Legible signage or visual iconography on receptacles |
| **No Mixed Contamination** | 5 pts | Zero visible co-mingling across waste streams |
| **Total Available** | **100 pts** | Maximum Compliance Score |

### Compliance Classification Tiers:
- **80 – 100 points**: `EXCELLENT` — Exemplary compliance; model society.
- **60 – 79 points**: `GOOD` — Acceptable compliance; minor improvements recommended.
- **40 – 59 points**: `NEEDS_IMPROVEMENT` — Non-compliant in key areas; formal advisory notice issued.
- **0 – 39 points**: `POOR` — Severe non-compliance; automated violation flag generated.

### Scoring Invariant & Result Model:
The scoring engine produces a structured `ComplianceResult` containing the computed total score, status tier, violation trigger, severity, and a granular `ScoreBreakdownDto`. No mixed waste points (+5) cannot be awarded if all waste streams fail segregation, as unsegregated waste is inherently co-mingled.

---

## 6. Volunteer Application & Governance Lifecycle

Civic volunteer participation follows a structured verification lifecycle:

```
[Public User] 
      │ 
      ▼ Submits /register.html (POST /api/auth/register)
[Account Created with status = PENDING]
      │
      ▼ Login permitted; Field audits blocked (403 Forbidden)
[Admin Reviews Application on /admin/volunteers.html]
      ├── Action: Approve (POST /api/volunteers/{id}/approve) ──► status = ACTIVE (Audits Enabled)
      └── Action: Reject  (POST /api/volunteers/{id}/reject)  ──► status = REJECTED (Audits Blocked)
```

- **Verification Guard**: Volunteers in `PENDING` status see a prominent notice (*"Your account is awaiting admin verification"*) on the Field App, and submission endpoints reject audits with HTTP 403 Forbidden.
- **Privilege Protection**: Administrative accounts cannot be modified through volunteer governance endpoints.

---

## 7. Technology Stack & Rationale

| Layer | Selected Technology | Architectural Rationale & Constraints |
| :--- | :--- | :--- |
| **Frontend UI** | **HTML5, CSS3, Vanilla JavaScript (ES6+)** | **Zero UI frameworks allowed** (No React, Angular, or Vue). Delivers zero-build-step simplicity, ultra-fast load times, complete transparency, and long-term maintainability. |
| **Backend Runtime** | **Java 17+ / Spring Boot 3.2.5** | Enterprise-grade, strongly typed, robust ecosystem with first-class REST, JPA, and security abstractions. |
| **Persistence** | **Spring Data JPA / Hibernate 6** | Object-Relational Mapping providing clean repository interfaces and ANSI SQL portability. |
| **Security** | **Spring Security 6 & JJWT 0.12.5** | Stateless JWT authentication, BCrypt password hashing, and role-based access control (`ADMIN`, `VOLUNTEER`). |
| **Database** | **H2 (File-Based for Dev; In-Memory for Tests)** | Embedded database with runtime persistence under `backend/data/wastewatchdb.mv.db`; isolated in-memory test database. |
| **QR Code Engine** | **Google ZXing 3.5.3** | Server-side PNG byte generation and Base64 data-URL formatting. |
| **Mapping** | **Leaflet.js & OpenStreetMap** | Open-source, lightweight mapping library without proprietary API key requirements. |
| **Testing** | **JUnit 5, Mockito, Spring Boot Test** | Thorough unit and integration test coverage across all 14 phases (**161/161 tests passing**). |

---

## 8. Development Roadmap & Implementation Status

### Implemented Capabilities (Phases 1–14 + Enhancements)
1. ✅ **Building-Level Field Inspection Workflow**: Direct building selection, 6-point checklist, and live score ring.
2. ✅ **Volunteer Self-Registration & Verification**: Public registration, `PENDING` state, and admin approve/reject actions.
3. ✅ **Authoritative 100-Point Scoring Engine**: Null-safe weighted scoring with granular point breakdowns.
4. ✅ **Automated Violation Generation**: Automatic ticketing on `POOR` scores (<40) with fine assessment.
5. ✅ **Repeat Non-Compliance Detection**: Flagging entities with 3 consecutive `POOR` scores as `PERSISTENT_NON_COMPLIANCE`.
6. ✅ **In-App Notification Engine**: Real-time alerts for critical violations, persistent non-compliance, and system alerts.
7. ✅ **Volunteer Performance Analytics**: Leaderboard tracking audits completed, average score given, and distinct buildings inspected.
8. ✅ **Interactive Geographic Map**: Leaflet.js compliance pins, building scorecards, hotspot rings, and unmapped drawer.
9. ✅ **Decision Support Analytics**: Standardized Area Health Score rankings, confidence ratings, and time-series trends.
10. ✅ **Multi-Dimensional Reporting & CSV Export**: Dynamic date bounding and streaming RFC-4180 CSV export across 7 dimensions.
11. ✅ **Immutable Security Audit Trail**: Audit logging of logins, mutations, status changes, and resolutions.
12. ✅ **Stateless JWT Security & RBAC**: HMAC-SHA256 tokens, BCrypt hashing, and volunteer record isolation.
13. ✅ **Eco-Themed Field App UI**: Vector eco artwork, botanical accents, responsive stepper, and live score gauge.
14. ✅ **Glassmorphism Municipal Admin Consoles**: Unified translucent theme across 9 admin pages with thematic environmental renders.
15. ✅ **File-Based H2 Persistence**: Development records persist across restarts at `backend/data/wastewatchdb.mv.db`.
16. ✅ **Automated Runtime Migration**: `@Order(1)` startup runner migrates constraints and backfills legacy data.

### Future Planned Capabilities
1. 🔮 **IoT Smart Bin Sensor Integration**: Hardware ultrasonic fill-level and gas sensor telemetry.
2. 🔮 **AI Camera Waste Classification**: On-device computer vision classification of waste photos.
3. 🔮 **SMS & WhatsApp Gateway Notifications**: Out-of-band alerts to housing society chairpersons.
4. 🔮 **Multi-Tenant SaaS Partitioning**: Multi-city tenancy with isolated municipal schemas.

---

## 9. Development Demo Accounts

| Role | Username | Password | Email | Status | Access Scope |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `admin123` | `admin@wastewatch.org` | `ACTIVE` | Complete municipal administrative consoles, reports, audit logs, violations |
| **Field Volunteer** | `volunteer` | `volunteer123` | `volunteer@wastewatch.org` | `ACTIVE` | Building inspections, live score ring, personal audit history |
| **Field Volunteer #2** | `volunteer_priya` | `volunteer123` | `priya.sharma@wastewatch.org` | `ACTIVE` | Secondary auditor for multi-user compliance tracking |
| **Field Volunteer #3** | `volunteer_rohan` | `volunteer123` | `rohan.mehta@wastewatch.org` | `INACTIVE` | Inactive auditor account demonstrating governance state |
