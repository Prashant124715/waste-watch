# WasteWatch — REST API Specification

> **STATUS: FULLY IMPLEMENTED & SYNCHRONIZED (PHASES 1–14)**  
> *All 14 REST Controllers, endpoints, request/response contracts, and error structures documented below match the active Spring Boot implementation in `com.wastewatch.controller`, verified against 161 automated unit, integration, and architecture tests.*

---

## 1. API Standards & Conventions

- **Base URL**: `/api` (or `http://localhost:8080/api` for local development)
- **Transport**: HTTP/HTTPS
- **Default Content Type**: `application/json; charset=UTF-8` (RFC-4180 `text/csv; charset=UTF-8` for CSV streaming; `image/png` for QR code image generation)
- **Authentication**: Stateless Bearer JWT header:
  ```http
  Authorization: Bearer <token>
  ```
- **Standard HTTP Status Codes**:
  - `200 OK`: Successful retrieval or update
  - `201 Created`: Resource successfully created
  - `400 Bad Request`: Validation failure, missing required field, or invalid arguments
  - `401 Unauthorized`: Token missing, malformed, or expired
  - `403 Forbidden`: Account awaiting verification (`PENDING`), rejected, or insufficient privileges
  - `404 Not Found`: Target resource does not exist
  - `409 Conflict`: Unique constraint violation or 30-second submission duplicate cooldown
  - `500 Internal Server Error`: Unhandled server exception

### 1.1. Standard Success Response Envelope (`ApiResponse<T>`)
```json
{
  "success": true,
  "message": "Resource retrieved successfully",
  "data": { ... },
  "timestamp": "2026-10-30T14:30:00Z"
}
```

### 1.2. Standard Machine-Readable Error Envelope (`ErrorResponse`)
```json
{
  "timestamp": "2026-10-30T14:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed on one or more fields",
  "path": "/api/inspections",
  "validationErrors": {
    "buildingId": "Building ID is required"
  }
}
```

---

## 2. Authentication & Identity (`/api/auth`)

### `POST /api/auth/login`
* **Description**: Authenticate user credentials and return a signed HMAC-SHA256 JWT token with identity metadata.
* **Access**: Public
* **Request Body** (`LoginRequestDto`):
  ```json
  {
    "username": "admin",
    "password": "admin123"
  }
  ```
  *(Note: `usernameOrEmail` field is also accepted)*
* **Response (200 OK)** (`AuthResponseDto`):
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "id": 1,
    "username": "admin",
    "email": "admin@wastewatch.org",
    "fullName": "Administrator",
    "role": "ADMIN"
  }
  ```

---

### `POST /api/auth/register`
* **Description**: Register a new civic volunteer auditor. Account is created in `PENDING` status awaiting municipal administrator review.
* **Access**: Public
* **Request Body** (`RegisterRequestDto`):
  ```json
  {
    "fullName": "Vikram Patil",
    "username": "vikram_p",
    "email": "vikram@wastewatch.org",
    "phoneNumber": "9876543210",
    "password": "password123",
    "confirmPassword": "password123",
    "city": "Metro City",
    "state": "Maharashtra",
    "address": "Green Valley CHS, Sector 14",
    "reason": "Want to ensure source segregation in our residential society."
  }
  ```
* **Response (201 Created)** (`ApiResponse<UserProfileDto>`):
  ```json
  {
    "success": true,
    "message": "Volunteer registration submitted successfully. Your account is pending administrator verification.",
    "data": {
      "id": 5,
      "username": "vikram_p",
      "email": "vikram@wastewatch.org",
      "fullName": "Vikram Patil",
      "phoneNumber": "9876543210",
      "role": "VOLUNTEER",
      "status": "PENDING",
      "city": "Metro City",
      "state": "Maharashtra"
    },
    "timestamp": "2026-10-30T14:35:00Z"
  }
  ```

---

### `GET /api/auth/me`
* **Description**: Retrieve the current authenticated user's profile from the Spring Security context.
* **Access**: Authenticated (`ADMIN`, `VOLUNTEER`)
* **Response (200 OK)** (`UserProfileDto`):
  ```json
  {
    "id": 1,
    "username": "admin",
    "email": "admin@wastewatch.org",
    "fullName": "Administrator",
    "phoneNumber": "9999000001",
    "role": "ADMIN",
    "status": "ACTIVE",
    "city": "Metro City",
    "state": "Maharashtra"
  }
  ```

---

## 3. Location & Infrastructure Management

### 3.1. Areas (`/api/areas`)

| HTTP Method | Endpoint URL | Access Level | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/areas` | `ADMIN`, `VOLUNTEER` | List all registered geographic areas/wards |
| `GET` | `/api/areas/{id}` | `ADMIN`, `VOLUNTEER` | Retrieve area details by primary key ID |
| `POST` | `/api/areas` | `ADMIN` only | Create a new area (`AreaRequestDto`) |
| `PUT` | `/api/areas/{id}` | `ADMIN` only | Update an existing area by ID |
| `DELETE` | `/api/areas/{id}` | `ADMIN` only | Delete area by ID (returns `ApiResponse<Void>`) |

* **Area Request Contract** (`AreaRequestDto`):
  ```json
  {
    "areaCode": "ZONE-NORTH-01",
    "name": "North Ward - Sector 14",
    "city": "Metro City",
    "pincode": "400001",
    "description": "Residential & commercial sector with active green initiatives"
  }
  ```

---

### 3.2. Buildings (`/api/buildings`)

| HTTP Method | Endpoint URL | Access Level | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/buildings` | `ADMIN`, `VOLUNTEER` | List buildings with optional `areaId` and `search` query parameters |
| `GET` | `/api/buildings/search?query={q}` | `ADMIN`, `VOLUNTEER` | Fast search buildings by name or code |
| `GET` | `/api/buildings/{id}` | `ADMIN`, `VOLUNTEER` | Retrieve building details by ID |
| `GET` | `/api/areas/{areaId}/buildings` | `ADMIN`, `VOLUNTEER` | List all buildings within a specific area |
| `POST` | `/api/buildings` | `ADMIN` only | Create building with GPS coordinates (`BuildingRequestDto`) |
| `PUT` | `/api/buildings/{id}` | `ADMIN` only | Update building details |
| `DELETE` | `/api/buildings/{id}` | `ADMIN` only | Delete building by ID |

* **Building Request Contract** (`BuildingRequestDto`):
  ```json
  {
    "areaId": 1,
    "buildingCode": "BLD-NW-101",
    "name": "Green Valley Heights",
    "address": "12 Forest Hill Road, Sector 14",
    "latitude": 19.07600000,
    "longitude": 72.87770000,
    "buildingType": "RESIDENTIAL",
    "totalFloors": 5
  }
  ```

---

### 3.3. Rooms & QR Identification (`/api/rooms`)

| HTTP Method | Endpoint URL | Access Level | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/rooms` | `ADMIN`, `VOLUNTEER` | List rooms with optional `buildingId` query param |
| `GET` | `/api/rooms/{id}` | `ADMIN`, `VOLUNTEER` | Retrieve room details by ID |
| `GET` | `/api/buildings/{buildingId}/rooms` | `ADMIN`, `VOLUNTEER` | List all rooms inside a specific building |
| `GET` | `/api/rooms/qr/{code}` | `ADMIN`, `VOLUNTEER` | Look up room metadata by QR code token (`RoomDetailDto`) |
| `GET` | `/api/rooms/{id}/qr-image?size=300` | `ADMIN`, `VOLUNTEER` | Generate raw PNG bytes of room QR code image |
| `GET` | `/api/rooms/qr/{code}/image?size=300` | `ADMIN`, `VOLUNTEER` | Generate raw PNG bytes of QR token image |
| `POST` | `/api/rooms/{id}/regenerate-qr` | `ADMIN` only | Regenerate a fresh QR identifier token for a room |
| `POST` | `/api/rooms` | `ADMIN` only | Create a new room record (`RoomRequestDto`) |
| `PUT` | `/api/rooms/{id}` | `ADMIN` only | Update room details |
| `DELETE` | `/api/rooms/{id}` | `ADMIN` only | Delete room by ID |

---

## 4. Field Inspection Subsystem (`/api/inspections`)

### `POST /api/inspections`
* **Description**: Submit a building-level waste segregation compliance audit. Evaluates the 100-point scoring algorithm, generates an automated violation ticket if the score is `POOR` (<40), enforces 30-second submission duplicate cooldown, and appends a security audit trail log.
* **Access**: `ADMIN`, `VOLUNTEER` (volunteers with status `PENDING` or `REJECTED` receive HTTP 403 Forbidden).
* **Request Body** (`InspectionRequestDto`):
  ```json
  {
    "buildingId": 1,
    "wetWasteSeparated": true,
    "dryWasteSeparated": true,
    "sanitaryWasteSeparated": true,
    "correctBinsUsed": true,
    "binsLabelled": true,
    "noMixedWaste": true,
    "remarks": "Exemplary 3-bin source segregation and clear signage observed.",
    "evidenceImageUrl": "https://example.com/photos/bld-101-audit.jpg"
  }
  ```
  *(Note: `roomId` is optional and deprecated for backward database compatibility)*
* **Response (201 Created)** (`ApiResponse<InspectionResponseDto>`):
  ```json
  {
    "success": true,
    "message": "Inspection recorded successfully",
    "data": {
      "id": 8,
      "buildingId": 1,
      "buildingName": "Green Valley Heights",
      "areaId": 1,
      "areaName": "North Ward - Sector 14",
      "buildingLevelInspection": true,
      "volunteerId": 2,
      "volunteerName": "Field Inspector Volunteer",
      "inspectionDate": "2026-10-30T14:40:00",
      "wetWasteSeparated": true,
      "dryWasteSeparated": true,
      "sanitaryWasteSeparated": true,
      "correctBinsUsed": true,
      "binsLabelled": true,
      "noMixedWaste": true,
      "totalScore": 100,
      "classification": "EXCELLENT",
      "status": "COMPLETED",
      "remarks": "Exemplary 3-bin source segregation and clear signage observed.",
      "evidenceImageUrl": "https://example.com/photos/bld-101-audit.jpg",
      "violationCreated": false,
      "violationId": null,
      "createdAt": "2026-10-30T14:40:00"
    },
    "timestamp": "2026-10-30T14:40:00Z"
  }
  ```

---

### Inspection Query Endpoints

| HTTP Method | Endpoint URL | Access Level | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/inspections` | `ADMIN`, `VOLUNTEER` | List inspections with optional `buildingId`, `volunteerId`, `roomId` filters. Volunteers see only their own audits. |
| `GET` | `/api/inspections/{id}` | `ADMIN`, `VOLUNTEER` | Get inspection details by ID (volunteers restricted to own records). |
| `GET` | `/api/buildings/{buildingId}/inspections` | `ADMIN`, `VOLUNTEER` | List all historical inspections conducted on a building. |
| `GET` | `/api/rooms/{roomId}/inspections` | `ADMIN`, `VOLUNTEER` | Deprecated legacy inspection query by room. |
| `GET` | `/api/volunteers/me/inspections` | `ADMIN`, `VOLUNTEER` | List personal inspection history of current authenticated volunteer. |
| `GET` | `/api/inspections/recent` | `ADMIN`, `VOLUNTEER` | Top recent inspection records (system-wide for admin; self for volunteer). |

---

## 5. Violation Escalation & Resolution (`/api/violations`)

All violation endpoints require the **`ROLE_ADMIN`** authority.

### `GET /api/violations`
* **Description**: Retrieve non-compliance violations matching multi-criteria query parameters.
* **Access**: `ROLE_ADMIN`
* **Query Parameters**:
  - `status`: `OPEN`, `ACKNOWLEDGED`, `ACTION_REQUIRED`, `RESOLVED`
  - `severity`: `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`
  - `buildingId`: Filter by building ID
  - `areaId`: Filter by municipal area ID
  - `roomId`: Filter by room ID (legacy)
  - `from`: ISO-8601 lower timestamp bound (e.g. `2026-10-01T00:00:00`)
  - `to`: ISO-8601 upper timestamp bound (e.g. `2026-10-30T23:59:59`)
* **Response (200 OK)**: List of `ViolationResponseDto`.

---

### `GET /api/violations/{id}`
* **Description**: Retrieve violation detail by primary key ID.
* **Access**: `ROLE_ADMIN`

---

### `PUT /api/violations/{id}/status`
* **Description**: Transition a violation through its operational lifecycle.
* **Access**: `ROLE_ADMIN`
* **Request Body** (`ViolationStatusUpdateDto`):
  ```json
  {
    "status": "ACTION_REQUIRED",
    "resolutionNotes": "Notice served to society secretary. Re-inspection scheduled.",
    "fineAmount": 250.00
  }
  ```
* **Response (200 OK)**: `ApiResponse<ViolationResponseDto>`

---

### `PUT /api/violations/{id}/resolve`
* **Description**: Terminal resolution endpoint requiring mandatory resolution notes. Automatically sets `resolvedAt` timestamp.
* **Access**: `ROLE_ADMIN`
* **Request Body** (`ViolationResolveDto`):
  ```json
  {
    "resolutionNotes": "Fine paid. Society committee verified installation of dedicated sanitary waste bin.",
    "fineAmount": 500.00
  }
  ```
* **Response (200 OK)**: `ApiResponse<ViolationResponseDto>`

---

## 6. Volunteer Applications & Governance (`/api/volunteers`)

### `GET /api/volunteers`
* **Description**: List volunteer roster, optionally filtered by application/account status.
* **Access**: `ROLE_ADMIN`
* **Query Parameter**: `status` (`ACTIVE`, `INACTIVE`, `SUSPENDED`, `PENDING`, `REJECTED`)
* **Response (200 OK)**: `ApiResponse<List<VolunteerSummaryDto>>`

---

### `GET /api/volunteers/{id}/performance`
* **Description**: Retrieve comprehensive performance metrics for a volunteer.
* **Access**: `ROLE_ADMIN`, or the authenticated `VOLUNTEER` for their own ID.
* **Response (200 OK)** (`ApiResponse<VolunteerSummaryDto>`):
  ```json
  {
    "success": true,
    "data": {
      "id": 2,
      "username": "volunteer",
      "fullName": "Field Inspector Volunteer",
      "email": "volunteer@wastewatch.org",
      "phoneNumber": "9999000002",
      "status": "ACTIVE",
      "city": "Metro City",
      "state": "Maharashtra",
      "inspectionsCount": 14,
      "averageScoreGiven": 82.5,
      "distinctBuildingsInspected": 4,
      "violationsFlagged": 2,
      "lastInspectionDate": "2026-10-30T12:00:00"
    }
  }
  ```

---

### `PUT /api/volunteers/{id}/status`
* **Description**: Administrative status toggle (`ACTIVE`, `INACTIVE`, `SUSPENDED`). Enforces privilege escalation defense: non-volunteer accounts cannot be modified.
* **Access**: `ROLE_ADMIN`
* **Request Body** (`VolunteerStatusUpdateDto`):
  ```json
  {
    "status": "INACTIVE"
  }
  ```

---

### `POST /api/volunteers/{id}/approve`
* **Description**: Approve a pending volunteer application. Sets `status = ACTIVE` and records an immutable audit log.
* **Access**: `ROLE_ADMIN`
* **Response (200 OK)**: `ApiResponse<VolunteerSummaryDto>` with message `"Volunteer application approved successfully"`.

---

### `POST /api/volunteers/{id}/reject`
* **Description**: Reject a volunteer application. Sets `status = REJECTED`.
* **Access**: `ROLE_ADMIN`
* **Response (200 OK)**: `ApiResponse<VolunteerSummaryDto>` with message `"Volunteer application rejected"`.

---

## 7. In-App Notifications (`/api/notifications`)

All notification endpoints require authentication (`ADMIN`, `VOLUNTEER`).

| HTTP Method | Endpoint URL | Access Level | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/notifications` | Authenticated | Retrieve all notifications for current user (newest first) |
| `GET` | `/api/notifications/unread` | Authenticated | Retrieve unread notifications for current user |
| `GET` | `/api/notifications/count` | Authenticated | Returns unread counter badge: `{"unreadCount": 3}` |
| `PATCH`| `/api/notifications/{id}/read` | Authenticated | Mark a single notification as read |
| `PATCH`| `/api/notifications/read-all` | Authenticated | Mark all notifications for current user as read |

---

## 8. Executive Dashboard (`/api/dashboard`)

All dashboard endpoints require the **`ROLE_ADMIN`** authority.

| HTTP Method | Endpoint URL | Access Level | Return Type | Description |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/dashboard/summary` | `ROLE_ADMIN` | `ApiResponse<DashboardSummaryDto>` | Executive counters: total inspections, compliance %, buildings, open violations |
| `GET` | `/api/dashboard/stats` | `ROLE_ADMIN` | `ApiResponse<DashboardSummaryDto>` | Alias endpoint for `/summary` |
| `GET` | `/api/dashboard/compliance`| `ROLE_ADMIN` | `ApiResponse<ComplianceDistributionDto>` | Count and percentage breakdown across 4 classification tiers |
| `GET` | `/api/dashboard/areas` | `ROLE_ADMIN` | `ApiResponse<List<AreaComplianceDto>>` | Area compliance scores and building inspection counts |
| `GET` | `/api/dashboard/trends` | `ROLE_ADMIN` | `ApiResponse<List<DailyTrendDto>>` | 7-day historical inspection volume and average score trend points |
| `GET` | `/api/dashboard/attention-required` | `ROLE_ADMIN` | `ApiResponse<List<AttentionRequiredDto>>` | Immediate alert items: persistent non-compliance and critical violations |

---

## 9. Decision Support Analytics (`/api/analytics`)

All analytics endpoints require the **`ROLE_ADMIN`** authority.

| HTTP Method | Endpoint URL | Query Parameters | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/analytics/overview` | None | Decision intelligence summary KPIs |
| `GET` | `/api/analytics/areas` | None | Standardized Area Health Scores and confidence ratings (`HIGH`, `MEDIUM`, `LOW`) |
| `GET` | `/api/analytics/buildings` | None | Building-level performance rankings and inspection counts |
| `GET` | `/api/analytics/trends` | `period` (`daily`, `weekly`, `monthly`), `days` (default `30`) | Grouped time-series compliance trend curves |
| `GET` | `/api/analytics/improvements` | `days` (default `14`) | Period-over-period score and coverage improvement deltas |
| `GET` | `/api/analytics/improved-areas`| `days` (default `14`) | Alias of `/improvements` |
| `GET` | `/api/analytics/hotspots` | None | Chronic repeat offenders and problem buildings |
| `GET` | `/api/analytics/volunteers` | None | Volunteer auditor productivity distribution |

---

## 10. Interactive Geographic Map (`/api/map`)

| HTTP Method | Endpoint URL | Access Level | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/map/buildings` | `ADMIN`, `VOLUNTEER` | Spatial markers with coordinates, compliance score, status, coverage %, poor room counts, and open violations. Supports query filters: `areaId`, `complianceStatus`, `buildingId`, `inspectionStatus`. |
| `GET` | `/api/map/hotspots` | `ADMIN`, `VOLUNTEER` | Area-level hotspot analysis: center coordinates, average score, poor density, severity (`CRITICAL`, `WARNING`, `ADVISORY`, `NONE`). |
| `GET` | `/api/map/areas` | `ROLE_ADMIN` | Aggregate area map summaries with health scores and violation metrics. |

---

## 11. Reporting & CSV Export (`/api/reports`)

All report endpoints require the **`ROLE_ADMIN`** authority and accept optional ISO-8601 date bounds:
- `@RequestParam(required = false) LocalDateTime from`
- `@RequestParam(required = false) LocalDateTime to`

| HTTP Method | Endpoint URL | Description |
| :--- | :--- | :--- |
| `GET` | `/api/reports/overview` | Executive system compliance report |
| `GET` | `/api/reports/areas` | Ward performance and health score report |
| `GET` | `/api/reports/buildings` | Building compliance matrix |
| `GET` | `/api/reports/coverage` | Building & area inspection coverage breakdown |
| `GET` | `/api/reports/violations`| Violation distribution by severity and status |
| `GET` | `/api/reports/volunteers`| Volunteer audit productivity and score averages |
| `GET` | `/api/reports/export/csv` | Streams authentic RFC-4180 CSV attachment with `Content-Disposition`. Query parameter `type`: `overview`, `areas`, `buildings`, `coverage`, `violations`, `volunteers`, or `inspections`. |

---

## 12. Security Audit Trail (`/api/audit-logs`)

### `GET /api/audit-logs`
* **Description**: Retrieve immutable governance audit logs recording user authentication, administrative changes, and status transitions.
* **Access**: `ROLE_ADMIN`
* **Query Parameters**:
  - `action`: Operation code (e.g. `USER_LOGIN`, `CREATE_AREA`, `CREATE_BUILDING`, `CREATE_INSPECTION`, `RESOLVE_VIOLATION`, `VOLUNTEER_STATUS_CHANGED`)
  - `entityType`: Target entity class (`AppUser`, `Area`, `Building`, `Room`, `Inspection`, `Violation`)
  - `userId`: Filter by actor user ID
  - `from`: Lower ISO-8601 datetime bound
  - `to`: Upper ISO-8601 datetime bound
* **Response (200 OK)**: List of `AuditLogResponseDto` ordered chronologically descending.

---

## 13. System Health Check (`/api/health`)

### `GET /api/health`
* **Description**: Liveness and readiness health probe.
* **Access**: Public
* **Response (200 OK)** (`HealthResponseDto`):
  ```json
  {
    "status": "UP",
    "application": "WasteWatch"
  }
  ```
