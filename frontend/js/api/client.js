/* ====================================================
   WasteWatch JS API Client
   All REST calls go through this module.
   ==================================================== */

const API_BASE = 'http://localhost:8080/api';

class ApiClient {
  constructor() {
    this._token = null;
    this.baseUrl = API_BASE;
  }

  setToken(token) {
    this._token = token;
    localStorage.setItem('ww_token', token);
  }

  getToken() {
    if (!this._token) {
      this._token = localStorage.getItem('ww_token');
    }
    return this._token;
  }

  clearToken() {
    this._token = null;
    localStorage.removeItem('ww_token');
    localStorage.removeItem('ww_user');
  }

  getHeaders(isJson = true) {
    const h = {};
    if (isJson) h['Content-Type'] = 'application/json';

    const token = this.getToken();
    if (token) {
      h['Authorization'] = `Bearer ${token}`;
    }

    return h;
  }

  async request(method, path, body = null) {
    const url = `${API_BASE}${path}`;

    const options = {
      method,
      headers: this.getHeaders()
    };

    if (body !== null) {
      options.body = JSON.stringify(body);
    }

    const res = await fetch(url, options);

    if (res.status === 401 && !path.startsWith('/auth/')) {
      this.clearToken();
      const loginUrl = window.location.pathname.includes('/admin/') || window.location.pathname.includes('/volunteer/')
        ? '../index.html'
        : './index.html';
      window.location.href = loginUrl;
      throw new Error('Unauthorized — redirecting to login');
    }

    let data;

    try {
      data = await res.json();
    } catch {
      data = null;
    }

    if (!res.ok) {
      let msg = data?.message;
      if (!msg && data?.validationErrors) {
        msg = Object.values(data.validationErrors).join('. ');
      }
      msg = msg || `HTTP ${res.status}`;
      const err = new Error(msg);
      err.data = data;
      err.status = res.status;
      throw err;
    }

    return data;
  }

  get(path) {
    return this.request('GET', path);
  }

  post(path, body) {
    return this.request('POST', path, body);
  }

  put(path, body) {
    return this.request('PUT', path, body);
  }

  delete(path) {
    return this.request('DELETE', path);
  }

  // ==================================================
  // Auth
  // ==================================================

  login(username, password) {
    return this.request('POST', '/auth/login', {
      usernameOrEmail: username,
      username,
      password
    });
  }

  register(data) {
    return this.post('/auth/register', data);
  }

  getMe() {
    return this.get('/auth/me');
  }

  // ==================================================
  // Dashboard
  // ==================================================

  getDashboardSummary() {
    return this.get('/dashboard/summary');
  }

  getDashboardTrends() {
    return this.get('/dashboard/trends');
  }

  getDashboardCompliance() {
    return this.get('/dashboard/compliance');
  }

  getDashboardAreas() {
    return this.get('/dashboard/areas');
  }

  getDashboardAttentionRequired() {
    return this.get('/dashboard/attention-required');
  }

  // ==================================================
  // Areas
  // ==================================================

  getAreas() {
    return this.get('/areas');
  }

  getArea(id) {
    return this.get(`/areas/${id}`);
  }

  createArea(data) {
    return this.post('/areas', data);
  }

  updateArea(id, data) {
    return this.put(`/areas/${id}`, data);
  }

  deleteArea(id) {
    return this.delete(`/areas/${id}`);
  }

  // ==================================================
  // Buildings
  // ==================================================

  getBuildings(areaId) {
    return this.get(
      areaId
        ? `/buildings?areaId=${encodeURIComponent(areaId)}`
        : '/buildings'
    );
  }

  searchBuildings(query) {
    return this.get(
      query
        ? `/buildings/search?query=${encodeURIComponent(query)}`
        : '/buildings'
    );
  }

  getBuilding(id) {
    return this.get(`/buildings/${id}`);
  }

  createBuilding(data) {
    return this.post('/buildings', data);
  }

  updateBuilding(id, data) {
    return this.put(`/buildings/${id}`, data);
  }

  deleteBuilding(id) {
    return this.delete(`/buildings/${id}`);
  }

  // ==================================================
  // Rooms
  // ==================================================

  getRooms(buildingId) {
    return this.get(
      buildingId
        ? `/rooms?buildingId=${encodeURIComponent(buildingId)}`
        : '/rooms'
    );
  }

  getRoom(id) {
    return this.get(`/rooms/${id}`);
  }

  getRoomByQr(code) {
    return this.get(`/rooms/qr/${encodeURIComponent(code)}`);
  }

  createRoom(data) {
    return this.post('/rooms', data);
  }

  updateRoom(id, data) {
    return this.put(`/rooms/${id}`, data);
  }

  deleteRoom(id) {
    return this.delete(`/rooms/${id}`);
  }

  regenerateRoomQr(id) {
    return this.post(`/rooms/${id}/regenerate-qr`);
  }

  getRoomQrImageUrl(id, size = 300) {
    return `${API_BASE}/rooms/${id}/qr-image?size=${encodeURIComponent(size)}`;
  }

  getQrTokenImageUrl(token, size = 300) {
    return `${API_BASE}/rooms/qr/${encodeURIComponent(token)}/image?size=${encodeURIComponent(size)}`;
  }

  // ==================================================
  // Inspections
  // ==================================================

  getInspections(params = {}) {
    const q = new URLSearchParams();

    if (params.roomId) {
      q.set('roomId', params.roomId);
    }

    if (params.volunteerId) {
      q.set('volunteerId', params.volunteerId);
    }

    if (params.buildingId) {
      q.set('buildingId', params.buildingId);
    }

    const qs = q.toString() ? `?${q}` : '';

    return this.get(`/inspections${qs}`);
  }

  getInspection(id) {
    return this.get(`/inspections/${id}`);
  }

  createInspection(data) {
    return this.post('/inspections', data);
  }

  getRoomInspections(roomId) {
    return this.get(`/rooms/${roomId}/inspections`);
  }

  getMyInspections() {
    return this.get('/volunteers/me/inspections');
  }

  getRecentInspections() {
    return this.get('/inspections/recent');
  }

  // ==================================================
  // Violations
  // ==================================================

  getViolations(params = {}) {
    const q = new URLSearchParams();

    if (params.status) {
      q.set('status', params.status);
    }

    if (params.severity) {
      q.set('severity', params.severity);
    }

    if (params.roomId) {
      q.set('roomId', params.roomId);
    }

    if (params.buildingId) {
      q.set('buildingId', params.buildingId);
    }

    if (params.areaId) {
      q.set('areaId', params.areaId);
    }

    if (params.from) {
      q.set('from', params.from);
    }

    if (params.to) {
      q.set('to', params.to);
    }

    const qs = q.toString() ? `?${q}` : '';

    return this.get(`/violations${qs}`);
  }

  getViolation(id) {
    return this.get(`/violations/${id}`);
  }

  updateViolationStatus(id, data) {
    return this.put(`/violations/${id}/status`, data);
  }

  resolveViolation(id, data) {
    return this.put(`/violations/${id}/resolve`, data);
  }

  // ==================================================
  // Map
  // ==================================================

  getMapBuildings(params = {}) {
    const q = new URLSearchParams();

    if (params.areaId) {
      q.set('areaId', params.areaId);
    }

    if (params.complianceStatus) {
      q.set('complianceStatus', params.complianceStatus);
    }

    if (params.buildingId) {
      q.set('buildingId', params.buildingId);
    }

    if (params.inspectionStatus) {
      q.set('inspectionStatus', params.inspectionStatus);
    }

    const qs = q.toString() ? `?${q}` : '';

    return this.get(`/map/buildings${qs}`);
  }

  getMapHotspots() {
    return this.get('/map/hotspots');
  }

  getMapAreas() {
    return this.get('/map/areas');
  }

  // ==================================================
  // Volunteers & User Governance
  // ==================================================

  getVolunteers(status = null) {
    const path = status ? `/volunteers?status=${encodeURIComponent(status)}` : '/volunteers';
    return this.get(path);
  }

  getVolunteerPerformance(id) {
    return this.get(`/volunteers/${id}/performance`);
  }

  updateVolunteerStatus(id, status) {
    return this.put(`/volunteers/${id}/status`, { status });
  }

  approveVolunteer(id) {
    return this.post(`/volunteers/${id}/approve`, {});
  }

  rejectVolunteer(id) {
    return this.post(`/volunteers/${id}/reject`, {});
  }

  // ==================================================
  // In-App Notifications
  // ==================================================

  getNotifications() {
    return this.get('/notifications');
  }

  getUnreadNotifications() {
    return this.get('/notifications/unread');
  }

  getUnreadNotificationCount() {
    return this.get('/notifications/count');
  }

  markNotificationRead(id) {
    return this.request('PATCH', `/notifications/${id}/read`);
  }

  markAllNotificationsRead() {
    return this.request('PATCH', '/notifications/read-all');
  }

  // ==================================================
  // Audit Logs
  // ==================================================

  getAuditLogs(params = {}) {
    const q = new URLSearchParams();

    if (params.action) {
      q.set('action', params.action);
    }

    if (params.entityType) {
      q.set('entityType', params.entityType);
    }

    if (params.userId) {
      q.set('userId', params.userId);
    }

    if (params.from) {
      q.set('from', params.from);
    }

    if (params.to) {
      q.set('to', params.to);
    }

    const qs = q.toString() ? `?${q}` : '';

    return this.get(`/audit-logs${qs}`);
  }

  // ==================================================
  // Reports & Governance
  // ==================================================

  getReportOverview(from, to) {
    const q = new URLSearchParams();

    if (from) {
      q.set('from', from);
    }

    if (to) {
      q.set('to', to);
    }

    const qs = q.toString() ? `?${q}` : '';

    return this.get(`/reports/overview${qs}`);
  }

  getReportAreas(from, to) {
    const q = new URLSearchParams();

    if (from) {
      q.set('from', from);
    }

    if (to) {
      q.set('to', to);
    }

    const qs = q.toString() ? `?${q}` : '';

    return this.get(`/reports/areas${qs}`);
  }

  getReportBuildings(from, to) {
    const q = new URLSearchParams();

    if (from) {
      q.set('from', from);
    }

    if (to) {
      q.set('to', to);
    }

    const qs = q.toString() ? `?${q}` : '';

    return this.get(`/reports/buildings${qs}`);
  }

  getReportCoverage() {
    return this.get('/reports/coverage');
  }

  getReportViolations(from, to) {
    const q = new URLSearchParams();

    if (from) {
      q.set('from', from);
    }

    if (to) {
      q.set('to', to);
    }

    const qs = q.toString() ? `?${q}` : '';

    return this.get(`/reports/violations${qs}`);
  }

  getReportVolunteers(from, to) {
    const q = new URLSearchParams();

    if (from) {
      q.set('from', from);
    }

    if (to) {
      q.set('to', to);
    }

    const qs = q.toString() ? `?${q}` : '';

    return this.get(`/reports/volunteers${qs}`);
  }

  getReportCsvUrl(type, from, to) {
    const q = new URLSearchParams({
      type: type || 'overview'
    });

    if (from) {
      q.set('from', from);
    }

    if (to) {
      q.set('to', to);
    }

    return `${API_BASE}/reports/export/csv?${q.toString()}`;
  }

  // ==================================================
  // Analytics Engine
  // ==================================================

  getAnalyticsOverview() {
    return this.get('/analytics/overview');
  }

  getAnalyticsAreas() {
    return this.get('/analytics/areas');
  }

  getAnalyticsBuildings() {
    return this.get('/analytics/buildings');
  }

  getAnalyticsTrends(period, days) {
    return this.get(
      `/analytics/trends?period=${encodeURIComponent(period || 'daily')}&days=${encodeURIComponent(days || 30)}`
    );
  }

  getAnalyticsImprovements(days) {
    return this.get(
      `/analytics/improvements?days=${encodeURIComponent(days || 14)}`
    );
  }

  getAnalyticsHotspots() {
    return this.get('/analytics/hotspots');
  }

  getAnalyticsVolunteers() {
    return this.get('/analytics/volunteers');
  }

  // ==================================================
  // Volunteer Management & Verification
  // ==================================================

  getVolunteers(status = null) {
    const q = status ? `?status=${encodeURIComponent(status)}` : '';
    return this.get(`/volunteers${q}`);
  }

  getVolunteerPerformance(id) {
    return this.get(`/volunteers/${id}/performance`);
  }

  approveVolunteer(id) {
    return this.post(`/volunteers/${id}/approve`, {});
  }

  rejectVolunteer(id) {
    return this.post(`/volunteers/${id}/reject`, {});
  }

  updateVolunteerStatus(id, status) {
    return this.put(`/volunteers/${id}/status`, { status });
  }
}

window.api = new ApiClient();