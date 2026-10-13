# WasteWatch — REST API Specification

> **STATUS: FULLY IMPLEMENTED & VERIFIED (PHASES 1–14)**  
> *All endpoints documented below are fully implemented in the Spring Boot backend, covered by 90 automated unit and integration tests, and integrated with the framework-free frontend client.*

---

## 1. API Standards & Conventions

- **Base URL**: `/api`
- **Transport**: HTTPS (HTTP for local development)
- **Data Format**: `application/json` for requests and responses (RFC-4180 `text/csv` for data exports)
- **Authentication**: Stateless Bearer JWT header: `Authorization: Bearer <token>`
- **HTTP Status Codes**:
  - `200 OK`: Request succeeded
  - `201 Created`: Resource successfully created
  - `400 Bad Request`: Validation failure or malformed payload
  - `401 Unauthorized`: Authentication missing, expired, or invalid
  - `403 Forbidden`: Insufficient role/privileges for resource
  - `404 Not Found`: Resource does not exist
  - `409 Conflict`: Unique constraint violation or duplicate submission cooldown
  - `500 Internal Server Error`: Unhandled server exception

### 1.1. Standard Response Envelope
```json
{
  "success": true,
  "message": "Resource retrieved successfully",
  "data": { ... },
  "timestamp": "2026-10-13T14:30:00Z"
}
```

### 1.2. Standard Error Envelope
```json
{
  "timestamp": "2026-10-13T14:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed on one or more fields",
  "path": "/api/inspections",
  "validationErrors": {
    "roomId": "Room ID is mandatory"
  }
}
```

---

## 2. Authentication & Session (`/api/auth`)

### `POST /api/auth/login`
- **Description**: Authenticate user credentials and return signed JWT token with user details.
- **Access**: Public
- **Request Body**:
  ```json
  {
    "username": "admin",
    "password": "admin123"
  }
  ```
- **Response (200 OK)**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "id": 1,
    "username": "admin",
    "email": "admin@wastewatch.gov",
    "fullName": "Municipal Administrator",
    "role": "ADMIN"
  }
  ```

### `GET /api/auth/me`
- **Description**: Retrieve authenticated user profile from security context.
- **Access**: Authenticated (`ADMIN`, `VOLUNTEER`)
- **Response (200 OK)**: User profile object matching login details.

---

## 3. Infrastructure Management

### 3.1. Areas (`/api/areas`)
- `GET /api/areas`: List all municipal wards/areas (Access: `ADMIN`, `VOLUNTEER`).
- `POST /api/areas`: Register a new area (Access: `ADMIN`).
- `GET /api/areas/{id}`: Get area details (Access: `ADMIN`, `VOLUNTEER`).
- `PUT /api/areas/{id}`: Update area details (Access: `ADMIN`).
- `DELETE /api/areas/{id}`: Delete an area (Access: `ADMIN`).

### 3.2. Buildings (`/api/buildings`)
- `GET /api/buildings?areaId={id}`: List buildings, optionally filtered by area (Access: `ADMIN`, `VOLUNTEER`).
- `POST /api/buildings`: Register a new building with GPS coordinates (Access: `ADMIN`).
- `GET /api/buildings/{id}`: Get building details (Access: `ADMIN`, `VOLUNTEER`).
- `PUT /api/buildings/{id}`: Update building (Access: `ADMIN`).
- `DELETE /api/buildings/{id}`: Delete building (Access: `ADMIN`).

### 3.3. Rooms & QR Identification (`/api/rooms`)
- `GET /api/rooms?buildingId={id}`: List rooms, optionally filtered by building (Access: `ADMIN`, `VOLUNTEER`).
- `POST /api/rooms`: Create a new room; automatically generates unique QR token (Access: `ADMIN`).
- `GET /api/rooms/{id}`: Get room summary (Access: `ADMIN`, `VOLUNTEER`).
- `GET /api/rooms/{id}/details`: Get comprehensive room details including building and area hierarchy (Access: `ADMIN`, `VOLUNTEER`).
- `GET /api/rooms/{id}/inspections`: Retrieve historical audit records for the room (Access: `ADMIN`, `VOLUNTEER`).
- `PUT /api/rooms/{id}`: Update room number, floor, or occupant details (Access: `ADMIN`).
- `DELETE /api/rooms/{id}`: Delete room (Access: `ADMIN`).

#### `GET /api/rooms/qr/{qrCodeToken}` (Phase 12)
- **Description**: Safely look up room metadata using scanned QR token identifier. Does not expose internal sensitive database IDs or passwords.
- **Access**: Authenticated (`ADMIN`, `VOLUNTEER`)
- **Path Parameter**: `qrCodeToken` (e.g., `WASTEWATCH-ROOM-BLD-NW-101-101`)
- **Response (200 OK)**:
  ```json
  {
    "id": 1,
    "roomNumber": "101",
    "floorNumber": 1,
    "qrCodeToken": "WASTEWATCH-ROOM-BLD-NW-101-101",
    "buildingId": 1,
    "buildingName": "Green Valley Heights",
    "buildingCode": "BLD-NW-101",
    "areaId": 1,
    "areaName": "North Ward - Sector 14"
  }
  ```

---

## 4. Volunteer Inspection System (`/api/inspections`)

### `POST /api/inspections`
- **Description**: Submit a new field audit. Evaluates 100-point compliance score, detects violations on low scores (<40), checks 30-second accidental cooldown, and creates audit log.
- **Access**: `ROLE_VOLUNTEER`
- **Request Body**:
  ```json
  {
    "roomId": 1,
    "wetWasteSeparated": true,
    "dryWasteSeparated": true,
    "sanitaryWasteSeparated": true,
    "correctBinsUsed": true,
    "binsLabelled": true,
    "noMixedWaste": true,
    "remarks": "Exemplary source segregation observed."
  }
  ```
- **Response (201 Created)**:
  ```json
  {
    "success": true,
    "message": "Inspection submitted successfully",
    "data": {
      "id": 8,
      "roomId": 1,
      "roomNumber": "101",
      "buildingName": "Green Valley Heights",
      "volunteerName": "Field Inspector Volunteer",
      "inspectionDate": "2026-10-13T22:15:00",
      "totalScore": 100,
      "classification": "EXCELLENT",
      "status": "COMPLETED",
      "violationCreated": false,
      "violationId": null,
      "breakdown": {
        "wetWasteScore": 25,
        "dryWasteScore": 25,
        "sanitaryWasteScore": 20,
        "correctBinsScore": 15,
        "binsLabelledScore": 10,
        "noMixedWasteScore": 5,
        "totalScore": 100
      }
    }
  }
  ```

### `GET /api/inspections`
- **Description**: Query inspection records with optional filters: `roomId`, `volunteerId`, `buildingId`.
- **Access**: `ROLE_ADMIN` (sees all), `ROLE_VOLUNTEER` (scoped strictly to own inspections).

### `GET /api/inspections/{id}`
- **Description**: Get inspection details by ID with granular criteria breakdown.
- **Access**: `ROLE_ADMIN`, or `ROLE_VOLUNTEER` if owner of inspection.

### `GET /api/volunteers/me/inspections`
- **Description**: Retrieve personal inspection history for current authenticated volunteer.
- **Access**: `ROLE_VOLUNTEER`

### `GET /api/inspections/recent`
- **Description**: Retrieve top 20 most recent inspections (system-wide for admin; self-scoped for volunteer).
- **Access**: Authenticated (`ADMIN`, `VOLUNTEER`)

---

## 5. Violation Management (`/api/violations`) (Phase 13)

### `GET /api/violations`
- **Description**: Retrieve list of non-compliance violations.
- **Access**: `ROLE_ADMIN`
- **Query Parameters**:
  - `severity`: `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`
  - `status`: `OPEN`, `ACKNOWLEDGED`, `ACTION_REQUIRED`, `RESOLVED`
  - `areaId`: Filter by area
  - `buildingId`: Filter by building
  - `startDate`, `endDate`: ISO-8601 date range filters

### `GET /api/violations/{id}`
- **Description**: Get specific violation details including room and inspection context.
- **Access**: `ROLE_ADMIN`

### `PUT /api/violations/{id}/status`
- **Description**: Transition violation lifecycle status.
- **Access**: `ROLE_ADMIN`
- **Request Body**:
  ```json
  {
    "status": "ACTION_REQUIRED",
    "notes": "Notice issued to building management."
  }
  ```

### `PUT /api/violations/{id}/resolve`
- **Description**: Resolve violation with mandatory resolution notes and penalty recording.
- **Access**: `ROLE_ADMIN`
- **Request Body**:
  ```json
  {
    "resolutionNotes": "Re-inspected on site. Three-bin system verified in working order.",
    "fineAmount": 250.00
  }
  ```

---

## 6. In-App Notifications (`/api/notifications`) (Phase 13)

- `GET /api/notifications`: Retrieve in-app notifications for authenticated user (Access: Authenticated).
- `GET /api/notifications/unread-count`: Returns integer count of unread notifications (Access: Authenticated).
- `PUT /api/notifications/{id}/read`: Mark notification as read (Access: Authenticated).
- `PUT /api/notifications/read-all`: Mark all notifications as read for current user (Access: Authenticated).

---

## 7. Volunteer Governance (`/api/volunteers`) (Phase 14)

### `GET /api/volunteers?status={ACTIVE|INACTIVE}`
- **Description**: List volunteer roster with active status, total audits completed, average score, unique rooms inspected, and last active timestamp.
- **Access**: `ROLE_ADMIN`

### `PUT /api/volunteers/{id}/status`
- **Description**: Activate or deactivate volunteer field access. Enforces privilege escalation defense: non-volunteer accounts cannot be modified.
- **Access**: `ROLE_ADMIN`
- **Request Body**:
  ```json
  {
    "status": "INACTIVE",
    "reason": "Volunteer transitioned to administrative duties."
  }
  ```

---

## 8. Multi-Dimensional Reporting & CSV Export (`/api/reports`) (Phase 14)

All report endpoints accept optional `startDate` and `endDate` query parameters (format: `YYYY-MM-DD`):

### `GET /api/reports/overview`
- **Description**: System-wide compliance KPIs: overall compliance %, total inspections, unique rooms audited, active violations.
- **Access**: `ROLE_ADMIN`

### `GET /api/reports/areas`
- **Description**: Area-by-area performance matrix: area code, total rooms, inspection count, coverage %, average score, compliance tier, and violation counts.
- **Access**: `ROLE_ADMIN`

### `GET /api/reports/buildings`
- **Description**: Building-level compliance scorecard with room counts and audit metrics.
- **Access**: `ROLE_ADMIN`

### `GET /api/reports/coverage`
- **Description**: Infrastructure coverage breakdown: total rooms, inspected rooms, uninspected rooms, coverage percentage.
- **Access**: `ROLE_ADMIN`

### `GET /api/reports/violations`
- **Description**: Violation distribution breakdown grouped by severity and status.
- **Access**: `ROLE_ADMIN`

### `GET /api/reports/volunteers`
- **Description**: Field volunteer activity summary: total inspections, average compliance score awarded, and active days.
- **Access**: `ROLE_ADMIN`

### `GET /api/reports/export/csv`
- **Description**: Authentic RFC-4180 streaming CSV export with `Content-Disposition: attachment; filename=...`.
- **Access**: `ROLE_ADMIN`
- **Query Parameters**:
  - `type`: `overview`, `areas`, `buildings`, `coverage`, `violations`, `volunteers`
  - `startDate`, `endDate`: Optional date bounds

---

## 9. Security Audit Trail (`/api/audit-logs`) (Phase 14)

### `GET /api/audit-logs`
- **Description**: Retrieve immutable audit log records tracking system operations.
- **Access**: `ROLE_ADMIN`
- **Query Parameters**:
  - `action`: e.g. `USER_LOGIN`, `CREATE_AREA`, `UPDATE_VOLUNTEER_STATUS`, `RESOLVE_VIOLATION`
  - `entityType`: e.g. `AppUser`, `Area`, `Building`, `Room`, `Inspection`, `Violation`
  - `startDate`, `endDate`: ISO-8601 date range
  - `search`: Free text search matching action, entity, user, or details

---

## 10. Decision Support Analytics (`/api/analytics`) & Dashboard (`/api/dashboard`)

### Dashboard APIs:
- `GET /api/dashboard/stats`: Executive summary KPI counts and rates (Access: `ROLE_ADMIN`).
- `GET /api/dashboard/trends`: 7-day historical inspection volume and score trends (Access: `ROLE_ADMIN`).

### Analytics APIs:
- `GET /api/analytics/overview`: Decision intelligence summary (Access: `ROLE_ADMIN`).
- `GET /api/analytics/areas`: Standardized Area Health Scores with coverage confidence ratings (Access: `ROLE_ADMIN`).
- `GET /api/analytics/buildings`: Building performance rankings (Access: `ROLE_ADMIN`).
- `GET /api/analytics/trends`: Grouped time-series compliance trends by daily/weekly/monthly intervals (Access: `ROLE_ADMIN`).
- `GET /api/analytics/improvements`: Period-over-period score and coverage improvements (Access: `ROLE_ADMIN`).
- `GET /api/analytics/hotspots`: Chronic repeat offenders and problem buildings (Access: `ROLE_ADMIN`).
- `GET /api/analytics/volunteers`: Volunteer productivity and audit distribution (Access: `ROLE_ADMIN`).

---

## 11. Geographic Map (`/api/map`) (Phase 11)

### `GET /api/map/markers`
- **Description**: Geographic coordinates and compliance status markers for all buildings with latitude/longitude.
- **Access**: Authenticated (`ADMIN`, `VOLUNTEER`)
- **Response (200 OK)**:
  ```json
  [
    {
      "buildingId": 1,
      "buildingCode": "BLD-NW-101",
      "buildingName": "Green Valley Heights",
      "address": "12 Forest Hill Road, Sector 14",
      "latitude": 19.07600000,
      "longitude": 72.87770000,
      "areaName": "North Ward - Sector 14",
      "buildingType": "RESIDENTIAL",
      "totalRooms": 2,
      "inspectedRooms": 2,
      "averageScore": 57.5,
      "status": "NEEDS_IMPROVEMENT"
    }
  ]
  ```
