# WasteWatch Frontend

Vanilla JS/HTML/CSS single-page application for the WasteWatch Waste Segregation Compliance Monitoring Platform.

## Pages

### Login
- **`index.html`** — Secure login with JWT auth, role-based redirection

### Admin Panel (`/admin/`)
| Page | Description |
|---|---|
| `dashboard.html` | Executive KPI dashboard with trend chart, score distribution |
| `areas.html` | Areas, Buildings & Rooms CRUD management with tabbed hierarchy |
| `inspections.html` | Full inspections history with search, classification filters, detail modal |
| `violations.html` | Non-compliance violation tracker with status updates |
| `volunteers.html` | Volunteer performance cards and detailed metrics |
| `map.html` | Live geographic compliance map (Leaflet + CartoDB dark tiles) |

### Volunteer Field App (`/volunteer/`)
| Page | Description |
|---|---|
| `inspect.html` | 3-step inspection flow: QR/Room → Checklist with live score ring → Result |
| `history.html` | Personal inspection history with stats, CSV & PDF export |

## Running Locally

### Prerequisites
- Backend running on `http://localhost:8080` (Spring Boot)
- Node.js (for static server)

### Start
```bash
cd frontend
npm run dev
```

This starts a static server at **http://localhost:3000**.

### Login Credentials (Dev)
| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Volunteer | `volunteer` | `volunteer123` |

## Architecture

```
frontend/
├── index.html              # Login page
├── package.json            # Dev server config
├── css/
│   ├── variables.css       # Design tokens (colors, spacing, radii)
│   ├── base.css            # Resets, typography, utilities
│   ├── layout.css          # Sidebar, topbar, page shell
│   └── components.css      # Cards, buttons, tables, modals, toasts
├── js/
│   ├── api/
│   │   └── client.js       # REST API client (JWT, all endpoints)
│   └── utils/
│       ├── helpers.js       # Auth, Toast, Fmt, DOM helpers
│       └── sidebar.js       # Dynamic sidebar injection
├── admin/
│   ├── dashboard.html
│   ├── areas.html
│   ├── inspections.html
│   ├── violations.html
│   ├── volunteers.html
│   └── map.html
└── volunteer/
    ├── inspect.html
    └── history.html
```

## Export Features (Phase 8)
- **CSV**: One-click download of inspection data as `.csv` file
- **PDF**: Opens print dialog with a professionally formatted inspection report
- Available on both Admin Inspections page and Volunteer History page
