# WasteWatch — Frontend Web Application

[![Frontend](https://img.shields.io/badge/Frontend-Vanilla%20HTML5%20%2F%20CSS3%20%2F%20ES6+-blue.svg)](https://developer.mozilla.org/)
[![UI Design](https://img.shields.io/badge/UI-Eco--Themed%20%26%20Glassmorphism-green.svg)](#)
[![Zero Frameworks](https://img.shields.io/badge/Framework%20Debt-Zero-brightgreen.svg)](#)

The **WasteWatch Frontend** is a high-performance, framework-free single-page web client engineered with **pure HTML5, modern CSS3, and ES6+ JavaScript modules**. It delivers a clean, responsive user experience for municipal administrators, supervisors, and on-ground volunteer auditors without any transpilation, bundler lock-in, or third-party frontend frameworks (no React, Angular, or Vue).

---

## 🌟 Visual Theme & Design Architecture

The interface features two purpose-built visual design subsystems:

1. **🌿 Eco-Themed Volunteer Field App (`/volunteer/`)**:
   - Styled via `css/volunteer-inspect.css` with a nature-inspired green palette, clean card elevations, and tactile checklist controls.
   - Vector eco scene illustration (`assets/inspect-bg-scene.svg`) depicting a modern green city, trees, and color-coded WET, DRY, and SANITARY bins.
   - Botanical leaf motif (`assets/sidebar-leaves.svg`) gracing the volunteer navigation sidebar.
   - Live SVG circular score ring calculating compliance in real time.

2. **💎 Glassmorphism Municipal Admin Consoles (`/admin/`)**:
   - Styled via `css/admin-theme.css` with translucent frosted-glass containers (`backdrop-filter: blur(16px)`), subtle border highlights, and elevated typography.
   - Individual high-resolution environmental thematic background renders (`assets/backgrounds/` in optimized WebP with PNG fallbacks) for each administrative console.

3. **✨ Opening Intro Splash Animation & Sticker Assets**:
   - Animated introductory overlay in `index.html` featuring a glowing logo halo, progress pulse, and auto-dismiss/click-to-enter controls.
   - Custom civic-tech sticker artwork (`assets/sticker-*.png`): clean dustbin, garbage truck, eco earth, recycling arrows, plant seedling, and municipal worker.

---

## 📁 Project Directory Structure

```
frontend/
├── index.html                   # Primary landing & login portal with opening intro splash
├── login.html                   # Redirect shim to index.html
├── register.html                # Volunteer self-registration application portal
├── package.json                 # Dev static server scripts & devDependencies
├── package-lock.json
├── serve.json                   # Clean URL routing & rewrite rules
├── convert-stickers.js          # Utility script for asset alpha-channel transparency
├── assets/                      # Graphic artwork, logos, backgrounds, stickers & icons
│   ├── favicon.png              # Browser tab icon
│   ├── logo.png                 # Main horizontal brand logo
│   ├── logo-icon.png            # Circular app icon
│   ├── logo-icon-192.png        # High-res PWA icon
│   ├── inspect-bg-scene.svg     # Eco skyline & 3-bin artwork for volunteer views
│   ├── sidebar-leaves.svg       # Botanical leaf accent for volunteer sidebar
│   ├── sticker-city.png         # Green city illustration sticker
│   ├── sticker-dustbin.png      # Segregation dustbin sticker
│   ├── sticker-earth.png        # Planet Earth eco sticker
│   ├── sticker-plant.png        # Plant sprout sticker
│   ├── sticker-recycle-arrows.png # Circular recycle icon sticker
│   ├── sticker-truck.png        # Municipal collection vehicle sticker
│   ├── sticker-worker.png       # Sanitation worker sticker
│   └── backgrounds/             # High-resolution glassmorphism background renders
│       ├── *.webp               # Modern high-performance WebP formats
│       └── *.png                # Lossless fallback PNG formats
├── css/                         # Modular CSS3 stylesheet architecture
│   ├── variables.css            # Centralized CSS design tokens (HSL palette, spacing, radii)
│   ├── base.css                 # Reset, typography, utility classes
│   ├── layout.css               # Sidebar, topbar, grid layout shells
│   ├── components.css           # Buttons, form controls, tables, modals, badges, toasts
│   ├── admin-theme.css          # Glassmorphism theme for all 9 municipal admin consoles
│   └── volunteer-inspect.css    # Full eco stylesheet for volunteer inspection & history
├── js/                          # Client JavaScript logic
│   ├── lucide.min.js            # Offline Lucide icon pack
│   ├── api/
│   │   └── client.js            # ApiClient singleton for all REST endpoints & token storage
│   └── utils/
│       ├── helpers.js           # Auth state, Toast toasts, Fmt formatters, DOM helpers
│       └── sidebar.js           # Dynamic role-based navigation sidebar injector
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

## 🖥️ Complete Page Inventory & Capabilities

### 1. Landing & Authentication

| Page | Path | Target Roles | Key Capabilities |
| :--- | :--- | :--- | :--- |
| **Login & Landing** | `index.html` | Public | • Opening intro splash animation overlay with auto-dismiss.<br>• Username/password authentication with JWT storage in `localStorage`.<br>• Password visibility toggle.<br>• One-click demo credentials chips (`Admin`, `Volunteer`).<br>• Automatic role-based redirect (`/admin/dashboard.html` or `/volunteer/inspect.html`). |
| **Login Redirect** | `login.html` | Public | • Lightweight redirection shim automatically redirecting to `index.html`. |
| **Volunteer Registration** | `register.html` | Public | • Full self-registration form for civic volunteers.<br>• Fields: Full Name, Username, Email, 10-Digit Mobile Number, State, City, Locality/Society Address, Password, Confirm Password.<br>• Submits to `POST /api/auth/register` with account created in `PENDING` status.<br>• Displays instant confirmation card instructing applicant on municipal review. |

---

### 2. Municipal Admin Consoles (`/admin/`)

| Page | File | Key Features |
| :--- | :--- | :--- |
| **Executive Dashboard** | `dashboard.html` | • Real-time KPI summary counters: Total Audits, Average Compliance, Active Buildings, Open Violations.<br>• 7-day inspection volume trend line chart.<br>• Compliance classification distribution progress bars.<br>• Embedded Leaflet.js interactive compliance map with color-coded markers.<br>• Recent field audits stream with direct detail modal links. |
| **Areas & Buildings** | `areas.html` | • Tabbed hierarchy navigation: **Areas** and **Buildings**.<br>• Area CRUD: Code, Name, City, Pincode, Description.<br>• Building CRUD: Code, Area selection, Name, Address, GPS Latitude/Longitude, Building Type, Floor count.<br>• Filter buildings by area and instant client-side search. |
| **Inspections Console** | `inspections.html` | • Full audit records table: Building, Area, Inspector, Score, Classification, Status, Violation flag, Date.<br>• Search by building, area, or inspector name.<br>• Filter by classification (`EXCELLENT`, `GOOD`, `NEEDS_IMPROVEMENT`, `POOR`).<br>• Detailed inspection view modal with 6-criteria score breakdown.<br>• One-click client CSV and printable PDF export. |
| **Violations Management** | `violations.html` | • Real-time non-compliance escalation tracker.<br>• Multi-criteria filtering by Status (`OPEN`, `ACKNOWLEDGED`, `ACTION_REQUIRED`, `RESOLVED`) and Severity (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`).<br>• Violation management modal to assess fines and input official resolution notes. |
| **Volunteer Governance** | `volunteers.html` | • Volunteer applications review and status management.<br>• Tab navigation: All, Pending Verification, Active, Suspended/Inactive, Rejected.<br>• Applicant review cards displaying applicant name, contact info, city/state, society area, and application timestamp.<br>• Action buttons: **Approve Application** (`POST /api/volunteers/{id}/approve`), **Reject Application** (`POST /api/volunteers/{id}/reject`), **Activate**, **Suspend**.<br>• Performance metrics: Audits completed, average score awarded, distinct buildings audited. |
| **Geographic Map** | `map.html` | • Full-screen Leaflet.js OpenStreetMap with CartoDB dark tile layer.<br>• Real-time building pins color-coded by compliance tier (`GREEN`, `YELLOW`, `ORANGE`, `RED`, `GRAY`).<br>• Filter markers by Area, Compliance status, Building, and Inspection status (`INSPECTED` vs `UNINSPECTED`).<br>• Interactive popup scorecards with direct links to audits.<br>• Area Hotspot Analysis console sidebar.<br>• "Location unavailable" drawer isolating facilities lacking GPS coordinates. |
| **Analytics Engine** | `analytics.html` | • Decision support intelligence console.<br>• Standardized Area Health Score rankings ($0.70 \times \text{Compliance} + 0.20 \times \text{Coverage} + 0.10 \times \text{Resolution}$).<br>• Inspection coverage confidence ratings (`HIGH`, `MEDIUM`, `LOW`).<br>• Time-series trend grouping by daily, weekly, and monthly periods.<br>• Period-over-period improvement leaderboard.<br>• Repeat offender and chronic hotspot building identification. |
| **Reports Console** | `reports.html` | • Multi-dimensional reporting across 6 perspectives: Overview, Areas, Buildings, Coverage, Violations, Volunteers.<br>• Dynamic date bounding presets: Last 7 Days, Last 30 Days, Year-to-Date, All-Time.<br>• Server-side RFC-4180 streaming CSV export (`/api/reports/export/csv`). |
| **Security Audit Logs** | `audit-logs.html` | • Immutable security audit trail table.<br>• Filter by Action (`USER_LOGIN`, `CREATE_AREA`, `CREATE_BUILDING`, `UPDATE_VOLUNTEER_STATUS`, `RESOLVE_VIOLATION`, `CREATE_INSPECTION`).<br>• Filter by Entity Type, User ID, and date range.<br>• Detailed audit context modal without credential leakage. |

---

### 3. Volunteer Field App (`/volunteer/`)

| Page | File | Key Features |
| :--- | :--- | :--- |
| **Building Waste Inspection** | `inspect.html` | • Mobile-first 3-step field inspection stepper:<br>  1. **Select Building**: Search by name or code, choose from dropdown, or tap quick-select chips; preview building address and floors.<br>  2. **Checklist**: 6 criteria checkboxes (Wet Waste 25 pts, Dry Waste 25 pts, Sanitary 20 pts, Standard Bins 15 pts, Labels 10 pts, Contamination Guard 5 pts), optional photo evidence URL, observations text area.<br>  3. **Result Summary**: Instant score feedback, tier badge, violation escalation notice, return to new audit.<br>• **Live Score Dial**: Dynamic SVG ring gauge calculating score and classification tier in real time as checkboxes toggle.<br>• **Account Status Banners**:<br>  - `PENDING`: Warning banner *"Your account is awaiting admin verification"* with submission controls disabled.<br>  - `REJECTED`: Alert banner informing applicant that registration was not approved. |
| **My Inspection History** | `history.html` | • Personal audit log displaying historical submissions tied to authenticated volunteer.<br>• Frosted-glass KPI summary cards: Total Audits, Average Score Given, High Compliance %, Flagged Audits.<br>• Search and filter audits by building, area, and classification.<br>• Client-side CSV export and printable PDF summary. |

---

## 🔌 API Client Architecture (`js/api/client.js`)

All communication between the frontend and the Spring Boot REST backend is managed through the `ApiClient` singleton (`window.api`):

- **Base URL Resolution**: Automatically points to `http://localhost:8080/api` (or relative `/api` in production).
- **Authentication**: Injects `Authorization: Bearer <token>` into HTTP headers.
- **Token Lifecycle**: Evaluates JWT expiration via base64 payload decoding (`isTokenExpired()`). Automatically clears tokens and redirects to `index.html` on HTTP 401 Unauthorized responses.
- **Standardized Error Handling**: Unpacks server validation error dictionaries (`validationErrors`) and displays clear user feedback.
- **Full Endpoint Coverage**: Wraps all 14 controller domains (Auth, Dashboard, Areas, Buildings, Rooms, Inspections, Violations, Map, Volunteers, Notifications, Audit Logs, Reports, Analytics).

---

## 🚀 Running the Frontend Locally

### Prerequisites
- Node.js (v16+) for serving static files locally.
- Backend running on `http://localhost:8080`.

### Development Server
```bash
# Navigate to frontend directory
cd frontend

# Start local server via npm script
npm run dev
# or
npm start
```

*The frontend opens at `http://localhost:3000`.*

### Clean URL Rewrites (`serve.json`)
The application includes `serve.json` configuring clean URLs:
- `/` ➔ `/index.html`
- `/login` ➔ `/index.html`
- `/register` ➔ `/register.html`

---

## 🔑 Demo Login Accounts

| Role | Username | Password | Default Redirect |
| :--- | :--- | :--- | :--- |
| **Municipal Administrator** | `admin` | `admin123` | `/admin/dashboard.html` |
| **Field Volunteer (Active)** | `volunteer` | `volunteer123` | `/volunteer/inspect.html` |
| **Field Volunteer #2 (Active)**| `volunteer_priya`| `volunteer123` | `/volunteer/inspect.html` |
| **Field Volunteer #3 (Inactive)**| `volunteer_rohan`| `volunteer123` | Login permitted; audits disabled |
