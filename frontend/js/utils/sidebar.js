/* ====================================================
   WasteWatch — Shared Sidebar Partial (JS)
   Injected at runtime into every admin/volunteer page
   ==================================================== */

function buildAdminSidebar() {
  const loc = window.location.pathname;
  function isActive(href) {
    return loc.includes(href) ? 'active' : '';
  }
  return `
  <aside class="sidebar" id="sidebar">
    <div class="sidebar-header">
      <div class="sidebar-logo">♻️</div>
      <div>
        <div class="sidebar-brand">WasteWatch</div>
        <div class="sidebar-role-badge">Admin Panel</div>
      </div>
    </div>

    <nav class="sidebar-nav">
      <div class="sidebar-section-label">Overview</div>
      <a class="nav-item ${isActive('dashboard')}" href="/admin/dashboard.html">
        <span class="nav-icon">📊</span> Dashboard
      </a>
      <a class="nav-item ${isActive('analytics')}" href="/admin/analytics.html">
        <span class="nav-icon">📈</span> Analytics Engine
      </a>
      <a class="nav-item ${isActive('map')}" href="/admin/map.html">
        <span class="nav-icon">🗺️</span> Live Map
      </a>

      <div class="sidebar-section-label">Operations</div>
      <a class="nav-item ${isActive('areas')}" href="/admin/areas.html">
        <span class="nav-icon">🏙️</span> Areas & Buildings
      </a>
      <a class="nav-item ${isActive('inspections')}" href="/admin/inspections.html">
        <span class="nav-icon">📋</span> Inspections
      </a>
      <a class="nav-item ${isActive('violations')}" href="/admin/violations.html">
        <span class="nav-icon">⚠️</span> Violations
        <span class="nav-badge" id="open-viol-count" style="display:none"></span>
      </a>

      <div class="sidebar-section-label">People</div>
      <a class="nav-item ${isActive('volunteers')}" href="/admin/volunteers.html">
        <span class="nav-icon">👷</span> Volunteers
      </a>
    </nav>

    <div class="sidebar-footer">
      <div class="user-card">
        <div class="user-avatar" id="user-avatar">WW</div>
        <div class="user-info">
          <div class="user-name" id="user-name">Loading…</div>
          <div class="user-role" id="user-role">ADMIN</div>
        </div>
        <button class="logout-btn" onclick="Auth.logout()" title="Sign out">🚪</button>
      </div>
    </div>
  </aside>`;
}

function buildVolunteerSidebar() {
  const loc = window.location.pathname;
  function isActive(href) { return loc.includes(href) ? 'active' : ''; }
  return `
  <aside class="sidebar" id="sidebar">
    <div class="sidebar-header">
      <div class="sidebar-logo">♻️</div>
      <div>
        <div class="sidebar-brand">WasteWatch</div>
        <div class="sidebar-role-badge">Field App</div>
      </div>
    </div>

    <nav class="sidebar-nav">
      <div class="sidebar-section-label">Inspect</div>
      <a class="nav-item ${isActive('inspect')}" href="/volunteer/inspect.html">
        <span class="nav-icon">📝</span> New Inspection
      </a>
      <a class="nav-item ${isActive('history')}" href="/volunteer/history.html">
        <span class="nav-icon">🕐</span> My History
      </a>
    </nav>

    <div class="sidebar-footer">
      <div class="user-card">
        <div class="user-avatar" id="user-avatar">VL</div>
        <div class="user-info">
          <div class="user-name" id="user-name">Loading…</div>
          <div class="user-role" id="user-role">VOLUNTEER</div>
        </div>
        <button class="logout-btn" onclick="Auth.logout()" title="Sign out">🚪</button>
      </div>
    </div>
  </aside>`;
}

function injectSidebar(role) {
  const html = role === 'ADMIN' ? buildAdminSidebar() : buildVolunteerSidebar();
  document.body.insertAdjacentHTML('afterbegin', html);
  renderUserCard();

  // Load open violations count for admin badge
  if (role === 'ADMIN') {
    api.getViolations({ status: 'OPEN' }).then(list => {
      const badge = document.getElementById('open-viol-count');
      if (badge && list && list.length > 0) {
        badge.textContent = list.length > 99 ? '99+' : list.length;
        badge.style.display = 'inline-block';
      }
    }).catch(() => {});
  }
}

window.injectSidebar = injectSidebar;
