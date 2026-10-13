# WasteWatch — REST API Specification

> **STATUS: PARTIALLY IMPLEMENTED (PHASE 2–5)**  
> *Endpoints for Health (`/api/health`), Authentication (`/api/auth/**`), Areas (`/api/areas/**`), Buildings (`/api/buildings/**`), and Rooms (`/api/rooms/**`) are fully implemented and verified with automated integration tests. Endpoints for Inspection submission, Dashboard metrics, Leaflet Map geo-data, and CSV reports remain PLANNED.*

---

## 1. API Standards & Conventions

- **Base URL**: `/api/v1` (or `/api`)
- **Transport**: HTTPS (HTTP for local development)
- **Data Format**: `application/json` for requests and responses
- **Authentication**: Bearer Token (JWT) or session cookie passed in `Authorization: Bearer <token>`
- **HTTP Status Codes**:
  - `200 OK`: Request succeeded
  - `201 Created`: Resource successfully created
  - `400 Bad Request`: Validation failure or malformed payload
  - `401 Unauthorized`: Authentication missing or invalid
  - `403 Forbidden`: Insufficient role/privileges for resource
  - `404 Not Found`: Resource does not exist
  - `409 Conflict`: Unique constraint violation (e.g., duplicate username or room number)
  - `500 Internal Server Error`: Unhandled server exception

### 1.1. Standard Response Envelope

#### Success Envelope:
```json
{
  "success": true,
  "message": "Resource retrieved successfully",
  "data": { ... },
  "timestamp": "2026-10-08T14:30:00Z"
}
```

#### Error Envelope:
```json
{
  "success": false,
  "error": "BAD_REQUEST",
  "message": "Validation failed on one or more fields",
  "fieldErrors": {
    "wetWasteSeparated": "Field is required"
  },
  "timestamp": "2026-10-08T14:30:00Z"
}
```

---

## 2. Planned Endpoints Catalog

### 2.1. Authentication & Session (`/api/auth`)

#### `POST /api/auth/login` [PLANNED]
* **Description**: Authenticate user credentials and return auth token and user role.
* **Access**: Public
* **Request Body**:
  ```json
  {
    "username": "admin_user",
    "password": "Password123!"
  }
  ```
* **Response (200 OK)**:
  ```json
  {
    "success": true,
    "data": {
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "user": {
        "id": 1,
        "username": "admin_user",
        "fullName": "Admin Officer",
        "role": "ADMIN"
      }
    }
  }
  ```

#### `GET /api/auth/me` [PLANNED]
* **Description**: Retrieve profile information for the currently authenticated user.
* **Access**: Authenticated (`ADMIN`, `VOLUNTEER`)
* **Response (200 OK)**:
  ```json
  {
    "success": true,
    "data": {
      "id": 1,
      "username": "admin_user",
      "fullName": "Admin Officer",
      "email": "admin@wastewatch.org",
      "role": "ADMIN"
    }
  }
  ```

---

### 2.2. Executive Dashboard (`/api/dashboard`)

#### `GET /api/dashboard/summary` [PLANNED]
* **Description**: Aggregated KPI metrics for admin overview.
* **Access**: `ADMIN`
* **Response (200 OK)**:
  ```json
  {
    "success": true,
    "data": {
      "totalInspections": 1420,
      "overallComplianceRate": 78.4,
      "totalAreasAudited": 12,
      "totalBuildingsAudited": 85,
      "totalRoomsCovered": 1240,
      "openViolationsCount": 18,
      "activeVolunteersCount": 34,
      "scoreDistribution": {
        "excellent": 620,
        "good": 480,
        "needsImprovement": 220,
        "poor": 100
      }
    }
  }
  ```

#### `GET /api/dashboard/trends` [PLANNED]
* **Description**: Daily or weekly compliance score averages over time.
* **Access**: `ADMIN`
* **Query Parameters**: `period=30d` (optional, default `30d`)
* **Response (200 OK)**:
  ```json
  {
    "success": true,
    "data": [
      { "date": "2026-10-01", "averageScore": 74.2, "inspectionsCount": 42 },
      { "date": "2026-10-02", "averageScore": 76.5, "inspectionsCount": 51 }
    ]
  }
  ```

---

### 2.3. Geographic Areas (`/api/areas`)

#### `GET /api/areas` [PLANNED]
* **Description**: List all municipal areas with aggregate building counts.
* **Access**: Authenticated (`ADMIN`, `VOLUNTEER`)
* **Response (200 OK)**:
  ```json
  {
    "success": true,
    "data": [
      {
        "id": 1,
        "areaCode": "ZONE-N-01",
        "name": "North Ward - Sector 5",
        "city": "Metro City",
        "pincode": "400076",
        "totalBuildings": 14,
        "averageComplianceScore": 81.2
      }
    ]
  }
  ```

#### `POST /api/areas` [PLANNED]
* **Description**: Create a new municipal area.
* **Access**: `ADMIN`
* **Request Body**:
  ```json
  {
    "areaCode": "ZONE-N-01",
    "name": "North Ward - Sector 5",
    "city": "Metro City",
    "pincode": "400076",
    "description": "Residential zone surrounding Green Park"
  }
  ```
* **Response (201 Created)**: Created area object.

---

### 2.4. Buildings (`/api/buildings`)

#### `GET /api/buildings` [PLANNED]
* **Description**: Retrieve buildings, optionally filtered by `areaId`.
* **Access**: Authenticated
* **Query Parameters**: `areaId=1` (optional)
* **Response (200 OK)**:
  ```json
  {
    "success": true,
    "data": [
      {
        "id": 10,
        "areaId": 1,
        "buildingCode": "BLD-GH-101",
        "name": "Green Heights Tower A",
        "address": "42 Pine Street",
        "latitude": 19.0760,
        "longitude": 72.8777,
        "buildingType": "RESIDENTIAL",
        "totalFloors": 12,
        "totalRooms": 48,
        "recentComplianceScore": 84.0
      }
    ]
  }
  ```

#### `POST /api/buildings` [PLANNED]
* **Description**: Register a new building.
* **Access**: `ADMIN`

---

### 2.5. Rooms (`/api/rooms`)

#### `GET /api/rooms` [PLANNED]
* **Description**: List rooms filtered by `buildingId`.
* **Access**: Authenticated
* **Query Parameters**: `buildingId=10`

#### `GET /api/rooms/qr/{code}` [PLANNED]
* **Description**: Fetch room metadata by scanning QR code token.
* **Access**: Authenticated (`VOLUNTEER`, `ADMIN`)
* **Response (200 OK)**:
  ```json
  {
    "success": true,
    "data": {
      "roomId": 105,
      "buildingId": 10,
      "buildingName": "Green Heights Tower A",
      "floorNumber": 3,
      "roomNumber": "Flat 302",
      "lastInspectedAt": "2026-09-28T10:15:00Z",
      "lastScore": 85
    }
  }
  ```

---

### 2.6. Inspections (`/api/inspections`)

#### `POST /api/inspections` [IMPLEMENTED]
* **Description**: Submit a new room inspection. Authoritative 100-point compliance score calculated server-side. Auto-generates a `Violation` if score < 40. Prevents duplicate submissions within 30-second cooldown window.
* **Access**: `VOLUNTEER`, `ADMIN`
* **Request Body**:
  ```json
  {
    "roomId": 1,
    "wetWasteSeparated": true,
    "dryWasteSeparated": true,
    "sanitaryWasteSeparated": true,
    "correctBinsUsed": true,
    "binsLabelled": false,
    "noMixedWaste": true,
    "remarks": "Clean bins, but missing labels on dry waste bin",
    "evidenceImageUrl": null
  }
  ```
* **Response (201 Created)**:
  ```json
  {
    "success": true,
    "message": "Inspection recorded successfully",
    "data": {
      "id": 501,
      "roomId": 1,
      "roomNumber": "Flat 101",
      "floorNumber": 1,
      "buildingId": 1,
      "buildingName": "Green Valley Heights",
      "areaId": 1,
      "areaName": "North Ward - Sector 5",
      "volunteerId": 2,
      "volunteerName": "Field Inspector Volunteer",
      "inspectionDate": "2026-10-09T10:00:00",
      "wetWasteSeparated": true,
      "dryWasteSeparated": true,
      "sanitaryWasteSeparated": true,
      "correctBinsUsed": true,
      "binsLabelled": false,
      "noMixedWaste": true,
      "totalScore": 90,
      "classification": "EXCELLENT",
      "status": "COMPLETED",
      "remarks": "Clean bins, but missing labels on dry waste bin",
      "breakdown": {
        "wetWaste": 25,
        "dryWaste": 25,
        "sanitaryWaste": 20,
        "correctBins": 15,
        "labelledBins": 0,
        "noMixedWaste": 5
      },
      "violationCreated": false,
      "violationId": null
    }
  }
  ```

#### `GET /api/inspections` [IMPLEMENTED]
* **Description**: List historical inspections. Admins can view all or filter by `roomId`, `volunteerId`, `buildingId`. Volunteers receive only their own submissions.
* **Access**: Authenticated (`ADMIN`, `VOLUNTEER`)
* **Query Parameters**: `roomId`, `volunteerId`, `buildingId` (optional)

#### `GET /api/inspections/{id}` [IMPLEMENTED]
* **Description**: Get full audit details of a specific inspection record. Volunteers can only access their own inspections.
* **Access**: Authenticated (`ADMIN`, `VOLUNTEER` self)

#### `GET /api/rooms/{roomId}/inspections` [IMPLEMENTED]
* **Description**: Retrieve audit history for a specific room.
* **Access**: Authenticated (`ADMIN`, `VOLUNTEER`)

#### `GET /api/volunteers/me/inspections` [IMPLEMENTED]
* **Description**: Retrieve all inspections conducted by the currently authenticated volunteer.
* **Access**: Authenticated (`VOLUNTEER`, `ADMIN`)

#### `GET /api/inspections/recent` [IMPLEMENTED]
* **Description**: Retrieve up to 20 most recent inspections (system-wide for admin; self-scoped for volunteer).
* **Access**: Authenticated (`ADMIN`, `VOLUNTEER`)

---

### 2.7. Violations (`/api/violations`)

#### `GET /api/violations` [PLANNED]
* **Description**: List non-compliance violations by status (`OPEN`, `NOTIFIED`, `RESOLVED`, `ESCALATED`).
* **Access**: `ADMIN`

#### `PUT /api/violations/{id}/status` [PLANNED]
* **Description**: Update violation status and add resolution remarks.
* **Access**: `ADMIN`
* **Request Body**:
  ```json
  {
    "status": "RESOLVED",
    "resolutionNotes": "Re-inspected on Oct 8. Compliant bins installed and labelled.",
    "fineAmount": 0.00
  }
  ```

---

### 2.8. Map & Geographic Visualization (`/api/map`)

#### `GET /api/map/buildings` [PLANNED]
* **Description**: Fetch all buildings with latitude, longitude, and health status for Leaflet.js map markers.
* **Access**: `ADMIN`
* **Response (200 OK)**:
  ```json
  {
    "success": true,
    "data": [
      {
        "id": 10,
        "name": "Green Heights Tower A",
        "latitude": 19.0760,
        "longitude": 72.8777,
        "latestScore": 84.0,
        "healthStatus": "EXCELLENT",
        "openViolations": 0
      }
    ]
  }
  ```

---

### 2.9. Volunteer Management & Analytics (`/api/volunteers`)

#### `GET /api/volunteers` [PLANNED]
* **Description**: List registered volunteers and their performance stats.
* **Access**: `ADMIN`

#### `GET /api/volunteers/{id}/performance` [PLANNED]
* **Description**: Detail individual volunteer inspection count, hours, and audit accuracy.
* **Access**: `ADMIN`, `VOLUNTEER` (self)

---

### 2.10. Reports & Exports (`/api/reports`)

#### `GET /api/reports/compliance`
* **Description**: Generate area/building compliance summaries.
* **Access**: `ADMIN`
* **Query Parameters**: `areaId`, `startDate`, `endDate`, `format=csv|json`

---

### 2.11. Analytics & Decision Support Engine (`/api/analytics`) [IMPLEMENTED]

#### `GET /api/analytics/overview`
* **Description**: High-level executive compliance summary, system health score, inspection coverage, and classification distribution.
* **Access**: `ADMIN`
* **Response (200 OK)**:
  ```json
  {
    "success": true,
    "data": {
      "totalInspections": 10,
      "totalRooms": 25,
      "inspectedRooms": 15,
      "totalAreas": 2,
      "totalBuildings": 4,
      "totalVolunteers": 3,
      "overallComplianceRate": 78.5,
      "overallInspectionCoverage": 60.0,
      "overallViolationResolutionRate": 85.0,
      "systemHealthScore": 75.5,
      "openViolationsCount": 1,
      "resolvedViolationsCount": 4,
      "classificationCounts": {
        "EXCELLENT": 5,
        "GOOD": 3,
        "NEEDS_IMPROVEMENT": 1,
        "POOR": 1
      }
    }
  }
  ```

#### `GET /api/analytics/areas`
* **Description**: Comprehensive area breakdown including Area Health Scores, inspection coverage, poor room counts, and confidence ratings.
* **Access**: `ADMIN`
* **Formula**: `Area Health Score = (0.70 * Compliance) + (0.20 * Coverage) + (0.10 * Resolution Rate)`

#### `GET /api/analytics/buildings`
* **Description**: Building-level compliance statistics, inspection coverage percentage, average score, and open violations.
* **Access**: `ADMIN`

#### `GET /api/analytics/trends`
* **Description**: Time-series trend analysis across daily, weekly, or monthly aggregations.
* **Access**: `ADMIN`
* **Query Parameters**: `period=daily|weekly|monthly`, `days=7|14|30|90`

#### `GET /api/analytics/improvements` / `GET /api/analytics/improved-areas`
* **Description**: Period-over-period comparative analysis detecting most improved and declining areas.
* **Access**: `ADMIN`
* **Query Parameters**: `days=14`

#### `GET /api/analytics/hotspots`
* **Description**: Persistent problem detection across Repeat Offender Rooms (failed >= 2), Problem Buildings (low score / high poor unit rate), and Critical Areas.
* **Access**: `ADMIN`

#### `GET /api/analytics/volunteers`
* **Description**: Volunteer productivity, active status, unique rooms audited, and average score awarded.
* **Access**: `ADMIN`
