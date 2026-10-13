/* ====================================================
   WasteWatch JS API Client
   All REST calls go through this module.
   ==================================================== */

const API_BASE = 'http://localhost:8080/api';

class ApiClient {
  constructor() {
    this._token = null;
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
    if (token) h['Authorization'] = `Bearer ${token}`;
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

    if (res.status === 401) {
      this.clearToken();
      window.location.href = '/index.html';
      throw new Error('Unauthorized — redirecting to login');
    }

    let data;
    try {
      data = await res.json();
    } catch {
      data = null;
    }

    if (!res.ok) {
      const msg = data?.message || `HTTP ${res.status}`;
      throw new Error(msg);
    }

    return data;
  }

  get(path)         { return this.request('GET', path); }
  post(path, body)  { return this.request('POST', path, body); }
  put(path, body)   { return this.request('PUT', path, body); }
  delete(path)      { return this.request('DELETE', path); }

  // ── Auth ──
  login(username, password) {
    return this.request('POST', '/auth/login', { username, password });
  }
  getMe() { return this.get('/auth/me'); }

  // ── Dashboard ──
  getDashboardSummary()           { return this.get('/dashboard/summary'); }
  getDashboardTrends()            { return this.get('/dashboard/trends'); }
  getDashboardCompliance()        { return this.get('/dashboard/compliance'); }
  getDashboardAreas()             { return this.get('/dashboard/areas'); }
  getDashboardAttentionRequired() { return this.get('/dashboard/attention-required'); }

  // ── Areas ──
  getAreas()             { return this.get('/areas'); }
  getArea(id)            { return this.get(`/areas/${id}`); }
  createArea(data)       { return this.post('/areas', data); }
  updateArea(id, data)   { return this.put(`/areas/${id}`, data); }
  deleteArea(id)         { return this.delete(`/areas/${id}`); }

  // ── Buildings ──
  getBuildings(areaId)   { return this.get(areaId ? `/buildings?areaId=${areaId}` : '/buildings'); }
  getBuilding(id)        { return this.get(`/buildings/${id}`); }
  createBuilding(data)   { return this.post('/buildings', data); }
  updateBuilding(id, d)  { return this.put(`/buildings/${id}`, d); }
  deleteBuilding(id)     { return this.delete(`/buildings/${id}`); }

  // ── Rooms ──
  getRooms(buildingId)   { return this.get(buildingId ? `/rooms?buildingId=${buildingId}` : '/rooms'); }
  getRoom(id)            { return this.get(`/rooms/${id}`); }
  getRoomByQr(code)      { return this.get(`/rooms/qr/${code}`); }
  createRoom(data)       { return this.post('/rooms', data); }
  updateRoom(id, data)   { return this.put(`/rooms/${id}`, data); }
  deleteRoom(id)         { return this.delete(`/rooms/${id}`); }

  // ── Inspections ──
  getInspections(params = {}) {
    const q = new URLSearchParams();
    if (params.roomId)      q.set('roomId', params.roomId);
    if (params.volunteerId) q.set('volunteerId', params.volunteerId);
    if (params.buildingId)  q.set('buildingId', params.buildingId);
    const qs = q.toString() ? `?${q}` : '';
    return this.get(`/inspections${qs}`);
  }
  getInspection(id)       { return this.get(`/inspections/${id}`); }
  createInspection(data)  { return this.post('/inspections', data); }
  getRoomInspections(roomId) { return this.get(`/rooms/${roomId}/inspections`); }
  getMyInspections()      { return this.get('/volunteers/me/inspections'); }
  getRecentInspections()  { return this.get('/inspections/recent'); }

  // ── Violations ──
  getViolations(params = {}) {
    const q = new URLSearchParams();
    if (params.status) q.set('status', params.status);
    if (params.roomId) q.set('roomId', params.roomId);
    const qs = q.toString() ? `?${q}` : '';
    return this.get(`/violations${qs}`);
  }
  getViolation(id)                { return this.get(`/violations/${id}`); }
  updateViolationStatus(id, data) { return this.put(`/violations/${id}/status`, data); }

  // ── Map ──
  getMapBuildings() { return this.get('/map/buildings'); }

  // ── Volunteers ──
  getVolunteers()              { return this.get('/volunteers'); }
  getVolunteerPerformance(id)  { return this.get(`/volunteers/${id}/performance`); }

  // ── Analytics Engine (Phase 10) ──
  getAnalyticsOverview()               { return this.get('/analytics/overview'); }
  getAnalyticsAreas()                  { return this.get('/analytics/areas'); }
  getAnalyticsBuildings()              { return this.get('/analytics/buildings'); }
  getAnalyticsTrends(period, days)     { return this.get(`/analytics/trends?period=${period || 'daily'}&days=${days || 30}`); }
  getAnalyticsImprovements(days)       { return this.get(`/analytics/improvements?days=${days || 14}`); }
  getAnalyticsHotspots()               { return this.get('/analytics/hotspots'); }
  getAnalyticsVolunteers()             { return this.get('/analytics/volunteers'); }
}

window.api = new ApiClient();
