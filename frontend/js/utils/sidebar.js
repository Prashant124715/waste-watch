/* ====================================================
   WasteWatch — Shared Sidebar Partial (JS)
   Injected at runtime into every admin/volunteer page
   ==================================================== */

function buildAdminSidebar() {
  const loc = window.location.pathname;
  const assetPrefix = (loc.includes('/admin/') || loc.includes('/volunteer/')) ? '../' : './';
  function isActive(href) {
    return loc.includes(href) ? 'active' : '';
  }
  return `
  <aside class="sidebar" id="sidebar">
    <div class="sidebar-header">
      <div class="sidebar-logo">
        <img src="${assetPrefix}assets/logo-icon.png" alt="WasteWatch" class="sidebar-logo-img"/>
      </div>
      <div>
        <div class="sidebar-brand">WasteWatch</div>
        <div class="sidebar-role-badge">Admin Panel</div>
      </div>
    </div>

    <nav class="sidebar-nav">
      <div class="sidebar-section-label">Overview</div>
      <a class="nav-item ${isActive('dashboard')}" href="./dashboard.html">
        <span class="nav-icon">📊</span> Dashboard
      </a>
      <a class="nav-item ${isActive('analytics')}" href="./analytics.html">
        <span class="nav-icon">📈</span> Analytics Engine
      </a>
      <a class="nav-item ${isActive('map')}" href="./map.html">
        <span class="nav-icon">🗺️</span> Live Map
      </a>

      <div class="sidebar-section-label">Operations</div>
      <a class="nav-item ${isActive('areas')}" href="./areas.html">
        <span class="nav-icon">🏙️</span> Areas & Buildings
      </a>
      <a class="nav-item ${isActive('inspections')}" href="./inspections.html">
        <span class="nav-icon">📋</span> Inspections
      </a>
      <a class="nav-item ${isActive('violations')}" href="./violations.html">
        <span class="nav-icon">⚠️</span> Violations
        <span class="nav-badge" id="open-viol-count" style="display:none"></span>
      </a>

      <div class="sidebar-section-label">People</div>
      <a class="nav-item ${isActive('volunteers')}" href="./volunteers.html">
        <span class="nav-icon">🤝</span> Volunteer Verification
        <span class="nav-badge" id="pending-vol-count" style="display:none; background:var(--color-warning);"></span>
      </a>

      <div class="sidebar-section-label">Governance</div>
      <a class="nav-item ${isActive('reports')}" href="./reports.html">
        <span class="nav-icon">📑</span> Reports & Exports
      </a>
      <a class="nav-item ${isActive('audit-logs')}" href="./audit-logs.html">
        <span class="nav-icon">🛡️</span> Audit Logs
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
  const assetPrefix = (loc.includes('/admin/') || loc.includes('/volunteer/')) ? '../' : './';
  function isActive(href) { return loc.includes(href) ? 'active' : ''; }
  return `
  <aside class="sidebar" id="sidebar">
    <div class="sidebar-header">
      <div class="sidebar-logo">
        <img src="${assetPrefix}assets/logo-icon.png" alt="WasteWatch" class="sidebar-logo-img"/>
      </div>
      <div>
        <div class="sidebar-brand">Waste<span>Watch</span></div>
        <div class="sidebar-role-badge">Field App</div>
      </div>
    </div>

    <nav class="sidebar-nav">
      <div class="sidebar-section-label">Inspect</div>
      <a class="nav-item ${isActive('inspect')}" href="./inspect.html">
        <span class="nav-icon">📝</span> New Inspection
      </a>
      <a class="nav-item ${isActive('history')}" href="./history.html">
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

  // Load open violations & pending volunteer count for admin badges
  if (role === 'ADMIN') {
    api.getViolations({ status: 'OPEN' }).then(list => {
      const badge = document.getElementById('open-viol-count');
      if (badge && list && list.length > 0) {
        badge.textContent = list.length > 99 ? '99+' : list.length;
        badge.style.display = 'inline-block';
      }
    }).catch(() => {});

    api.getVolunteers('PENDING').then(res => {
      const list = res?.data || res || [];
      const badge = document.getElementById('pending-vol-count');
      if (badge && list && list.length > 0) {
        badge.textContent = list.length > 99 ? '99+' : list.length;
        badge.style.display = 'inline-block';
      }
    }).catch(() => {});
  }
}

window.injectSidebar = injectSidebar;
