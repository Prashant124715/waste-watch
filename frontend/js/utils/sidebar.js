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
        <span class="nav-icon"><i data-lucide="layout-dashboard"></i></span> Dashboard
      </a>
      <a class="nav-item ${isActive('analytics')}" href="./analytics.html">
        <span class="nav-icon"><i data-lucide="line-chart"></i></span> Analytics Engine
      </a>
      <a class="nav-item ${isActive('map')}" href="./map.html">
        <span class="nav-icon"><i data-lucide="map"></i></span> Live Map
      </a>

      <div class="sidebar-section-label">Operations</div>
      <a class="nav-item ${isActive('areas')}" href="./areas.html">
        <span class="nav-icon"><i data-lucide="building-2"></i></span> Areas & Buildings
      </a>
      <a class="nav-item ${isActive('inspections')}" href="./inspections.html">
        <span class="nav-icon"><i data-lucide="clipboard-check"></i></span> Inspections
      </a>
      <a class="nav-item ${isActive('violations')}" href="./violations.html">
        <span class="nav-icon"><i data-lucide="triangle-alert"></i></span> Violations
        <span class="nav-badge" id="open-viol-count" style="display:none"></span>
      </a>

      <div class="sidebar-section-label">People</div>
      <a class="nav-item ${isActive('volunteers')}" href="./volunteers.html">
        <span class="nav-icon"><i data-lucide="user-check"></i></span> Volunteer Verification
        <span class="nav-badge" id="pending-vol-count" style="display:none; background:var(--color-warning);"></span>
      </a>

      <div class="sidebar-section-label">Governance</div>
      <a class="nav-item ${isActive('reports')}" href="./reports.html">
        <span class="nav-icon"><i data-lucide="file-text"></i></span> Reports & Exports
      </a>
      <a class="nav-item ${isActive('audit-logs')}" href="./audit-logs.html">
        <span class="nav-icon"><i data-lucide="shield-check"></i></span> Audit Logs
      </a>
    </nav>

    <div class="sidebar-footer">
      <div class="user-card">
        <div class="user-avatar" id="user-avatar">WW</div>
        <div class="user-info">
          <div class="user-name" id="user-name">Loading…</div>
          <div class="user-role" id="user-role">ADMIN</div>
        </div>
        <button class="logout-btn" onclick="Auth.logout()" title="Sign out" aria-label="Sign out"><i data-lucide="log-out"></i></button>
      </div>
    </div>
  </aside>`;
}

function buildVolunteerSidebar() {
  const loc = window.location.pathname;
  const assetPrefix = (loc.includes('/admin/') || loc.includes('/volunteer/')) ? '../' : './';
  function isActive(href) { return loc.includes(href) ? 'active' : ''; }
  return `
  <aside class="sidebar volunteer-sidebar" id="sidebar">
    <div class="sidebar-header">
      <div class="sidebar-logo">
        <img src="${assetPrefix}assets/logo-icon.png" alt="WasteWatch" class="sidebar-logo-img"/>
      </div>
      <div class="sidebar-brand-group">
        <div class="sidebar-brand sidebar-brand-name">Waste<span>Watch</span></div>
        <div class="sidebar-role-badge sidebar-app-tag">FIELD APP</div>
      </div>
    </div>

    <nav class="sidebar-nav">
      <div class="sidebar-section-label sidebar-section-title">INSPECT</div>
      <a class="nav-item sidebar-nav-item ${isActive('inspect')}" href="./inspect.html">
        <span class="nav-icon nav-item-icon"><i data-lucide="clipboard-pen"></i></span>
        <span>New Inspection</span>
      </a>
      <a class="nav-item sidebar-nav-item ${isActive('history')}" href="./history.html">
        <span class="nav-icon nav-item-icon"><i data-lucide="clock-3"></i></span>
        <span>My History</span>
      </a>
    </nav>

    <!-- Subtle botanical leaf accent at bottom-left corner -->
    <img src="${assetPrefix}assets/sidebar-leaves.svg" alt="" class="sidebar-botanical-bg" aria-hidden="true"/>

    <div class="sidebar-footer">
      <div class="user-card volunteer-profile-card">
        <div class="user-avatar volunteer-profile-avatar" id="user-avatar">FV</div>
        <div class="user-info volunteer-profile-meta">
          <div class="user-name volunteer-profile-name" id="user-name">Field Inspector</div>
          <div class="user-role volunteer-profile-sub" id="user-role">Volunteer</div>
          <span class="volunteer-profile-status" id="user-status-badge">Active</span>
        </div>
        <button class="volunteer-profile-logout logout-btn" onclick="Auth.logout()" title="Sign out" aria-label="Sign out">
          <i data-lucide="log-out" style="width:16px;height:16px;"></i>
        </button>
      </div>
    </div>
  </aside>`;
}

function injectSidebar(role) {
  const html = role === 'ADMIN' ? buildAdminSidebar() : buildVolunteerSidebar();
  document.body.insertAdjacentHTML('afterbegin', html);
  renderUserCard();

  // If Admin, ensure background artwork layer, admin-page class, and theme data-page are set
  if (role === 'ADMIN') {
    document.body.classList.add('admin-page');
    if (!document.body.getAttribute('data-page')) {
      const p = window.location.pathname.split('/').pop().replace('.html', '') || 'dashboard';
      document.body.setAttribute('data-page', p);
    }
    if (!document.querySelector('.admin-bg-layer')) {
      const bg = document.createElement('div');
      bg.className = 'admin-bg-layer';
      bg.setAttribute('aria-hidden', 'true');
      document.body.prepend(bg);
    }

    // Attach smooth page transition on navigation
    attachAdminPageTransitions();
  }

  const sidebarEl = document.getElementById('sidebar');
  if (sidebarEl && typeof lucide !== 'undefined' && lucide.createIcons) {
    lucide.createIcons({ root: sidebarEl });
  }

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

// ── Subtle Admin Page Transitions (200–300ms) ──
function attachAdminPageTransitions() {
  document.addEventListener('click', (e) => {
    const link = e.target.closest('a[href]');
    if (!link) return;
    const href = link.getAttribute('href');
    if (!href || href.startsWith('#') || href.startsWith('javascript:') || link.target === '_blank' || link.hasAttribute('download')) {
      return;
    }
    // Only intercept internal admin HTML pages
    const isAdminNav = href.endsWith('.html') || href.includes('dashboard') || href.includes('analytics') ||
                       href.includes('map') || href.includes('areas') || href.includes('inspections') ||
                       href.includes('violations') || href.includes('volunteers') || href.includes('reports') || href.includes('audit-logs');
    if (!isAdminNav || e.ctrlKey || e.metaKey || e.shiftKey || e.altKey) {
      return;
    }

    // Visual feedback on sidebar nav
    const navItem = link.closest('.nav-item');
    if (navItem) {
      document.querySelectorAll('.sidebar-nav .nav-item').forEach(el => el.classList.remove('active'));
      navItem.classList.add('active');
    }

    const main = document.querySelector('.main-content');
    if (main) {
      e.preventDefault();
      main.classList.add('page-nav-exit');
      setTimeout(() => {
        window.location.href = href;
      }, 180);
    }
  });

  // Restore opacity if user returns via back/forward cache
  window.addEventListener('pageshow', () => {
    const main = document.querySelector('.main-content');
    if (main) {
      main.classList.remove('page-nav-exit');
    }
  });
}

window.injectSidebar = injectSidebar;
