/* ====================================================
   WasteWatch — Shared Utilities
   ==================================================== */

// ── Auth helpers ──
const Auth = {
  getUser()  { return JSON.parse(localStorage.getItem('ww_user') || 'null'); },
  setUser(u) { localStorage.setItem('ww_user', JSON.stringify(u)); },
  isAdmin()  { const u = Auth.getUser(); return u?.role === 'ADMIN'; },
  isLoggedIn() { return !!api.getToken(); },
  getToken() { return api.getToken(); },
  _loginPath() {
    return window.location.pathname.includes('/admin/') || window.location.pathname.includes('/volunteer/')
      ? '../index.html'
      : './index.html';
  },
  requireLogin() {
    if (!Auth.isLoggedIn()) {
      window.location.href = Auth._loginPath();
      return false;
    }
    return true;
  },
  requireAdmin() {
    if (!Auth.requireLogin()) return false;
    if (!Auth.isAdmin()) {
      window.location.href = window.location.pathname.includes('/admin/')
        ? '../volunteer/inspect.html'
        : './volunteer/inspect.html';
      return false;
    }
    return true;
  },
  logout() {
    api.clearToken();
    window.location.href = Auth._loginPath();
  }
};

// ── Toast notifications ──
const Toast = {
  _ensureContainer() {
    if (!document.getElementById('toast-container')) {
      const c = document.createElement('div');
      c.id = 'toast-container';
      document.body.appendChild(c);
    }
  },
  show(type, title, message = '', duration = 4000) {
    this._ensureContainer();
    const icons = {
      success: '<i data-lucide="check-circle-2" class="icon-md"></i>',
      error: '<i data-lucide="alert-circle" class="icon-md"></i>',
      warning: '<i data-lucide="triangle-alert" class="icon-md"></i>',
      info: '<i data-lucide="info" class="icon-md"></i>'
    };
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
      <span class="toast-icon">${icons[type] || '<i data-lucide="bell" class="icon-md"></i>'}</span>
      <div class="toast-content">
        <div class="toast-title">${title}</div>
        ${message ? `<div class="toast-message">${message}</div>` : ''}
      </div>
    `;
    document.getElementById('toast-container').appendChild(toast);
    renderIcons(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(32px)';
      toast.style.transition = '0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, duration);
  },
  success(title, msg)  { this.show('success', title, msg); },
  error(title, msg)    { this.show('error', title, msg); },
  warning(title, msg)  { this.show('warning', title, msg); },
  info(title, msg)     { this.show('info', title, msg); }
};

// ── Formatters ──
const Fmt = {
  date(dt) {
    if (!dt) return '—';
    try {
      return new Date(dt).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
    } catch {
      return String(dt);
    }
  },
  datetime(dt) {
    if (!dt) return '—';
    try {
      return new Date(dt).toLocaleString('en-IN', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
    } catch {
      return String(dt);
    }
  },
  score(s) {
    if (s == null) return '—';
    return `${s}/100`;
  },
  classify(c) {
    const map = {
      EXCELLENT: 'Excellent',
      GOOD: 'Good',
      NEEDS_IMPROVEMENT: 'Needs Improvement',
      POOR: 'Poor'
    };
    return map[c] || c;
  },
  classifyBadge(c) {
    const cls = { EXCELLENT: 'excellent', GOOD: 'good', NEEDS_IMPROVEMENT: 'needs-improvement', POOR: 'poor' };
    return cls[c] || 'neutral';
  },
  violationStatus(s) {
    const map = { OPEN: 'Open', NOTIFIED: 'Notified', ESCALATED: 'Escalated', RESOLVED: 'Resolved' };
    return map[s] || s;
  },
  violationStatusBadge(s) {
    const map = { OPEN: 'danger', NOTIFIED: 'warning', ESCALATED: 'needs-improvement', RESOLVED: 'excellent' };
    return map[s] || 'neutral';
  },
  severity(s) {
    const map = { CRITICAL: 'danger', HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' };
    return map[s] || 'neutral';
  },
  buildingType(t) {
    const map = { RESIDENTIAL: 'Residential', COMMERCIAL: 'Commercial', INDUSTRIAL: 'Industrial', MIXED: 'Mixed Use', EDUCATIONAL: 'Educational', HEALTHCARE: 'Healthcare' };
    return map[t] || t;
  },
  avatar(name) {
    if (!name) return 'WW';
    const parts = name.trim().split(' ');
    return parts.length >= 2
      ? (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
      : name.slice(0, 2).toUpperCase();
  },
  formatDate(dt) {
    return formatDate(dt);
  },
  formatDateTime(dt) {
    return formatDateTime(dt);
  },
  formatScore(s) {
    return formatScore(s);
  },
  escapeHtml(str) {
    return escapeHtml(str);
  }
};

// ── Date Formatting ──
function formatDate(dt) {
  if (!dt) return 'Never';
  try { return new Date(dt).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }); }
  catch { return String(dt); }
}

function formatDateTime(dt) {
  return Fmt.datetime(dt);
}

function formatScore(s) {
  return Fmt.score(s);
}

// ── HTML Escaping ──
function escapeHtml(str) {
  if (str == null) return '';
  return String(str).replace(/[&<>"']/g, c =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

// ── DOM helpers ──
const $ = (sel, ctx = document) => ctx.querySelector(sel);
const $$ = (sel, ctx = document) => [...ctx.querySelectorAll(sel)];

function el(tag, attrs = {}, ...children) {
  const e = document.createElement(tag);
  for (const [k, v] of Object.entries(attrs)) {
    if (k === 'class') e.className = v;
    else if (k === 'html') e.innerHTML = v;
    else if (k === 'text') e.textContent = v;
    else e.setAttribute(k, v);
  }
  for (const c of children) {
    if (c instanceof Element) e.appendChild(c);
    else if (c != null) e.appendChild(document.createTextNode(c));
  }
  return e;
}

// ── Sidebar active state ──
function setActiveSidebarLink(href) {
  $$('.nav-item').forEach(n => {
    n.classList.remove('active');
    const h = n.getAttribute('href') || n.dataset.page;
    if (h && (href.endsWith(h) || href.includes(h))) {
      n.classList.add('active');
    }
  });
}

// ── Render user card ──
function renderUserCard() {
  const user = Auth.getUser();
  if (!user) return;
  const avatar = document.getElementById('user-avatar');
  const name   = document.getElementById('user-name');
  const role   = document.getElementById('user-role');
  if (avatar) avatar.textContent = Fmt.avatar(user.fullName || user.username);
  if (name)   name.textContent   = user.fullName || user.username;
  if (role)   role.textContent   = user.role;
}

// ── Generic modal open/close ──
function openModal(id) {
  const m = document.getElementById(id);
  if (m) { m.classList.remove('hidden'); m.focus?.(); }
}
function closeModal(id) {
  const m = document.getElementById(id);
  if (m) m.classList.add('hidden');
}
function closeAllModals() {
  $$('.modal-overlay').forEach(m => m.classList.add('hidden'));
}

// close on overlay click
document.addEventListener('click', e => {
  if (e.target.classList.contains('modal-overlay')) {
    e.target.classList.add('hidden');
  }
});

// ── Score ring helper ──
function renderScoreRing(container, score) {
  const radius = 40;
  const circumference = 2 * Math.PI * radius;
  const pct = Math.max(0, Math.min(100, score ?? 0));
  const offset = circumference - (pct / 100) * circumference;

  const cls = pct >= 80 ? 'excellent' : pct >= 60 ? 'good' : pct >= 40 ? 'needs-improvement' : 'poor';
  const colorMap = { excellent: '#2ECC71', good: '#00BFFF', 'needs-improvement': '#F39C12', poor: '#E74C3C' };
  const color = colorMap[cls];

  container.innerHTML = `
    <div class="score-ring-wrap">
      <svg width="100" height="100" viewBox="0 0 100 100">
        <circle class="score-ring-bg" cx="50" cy="50" r="${radius}" fill="none" stroke-width="8"/>
        <circle class="score-ring-fill" cx="50" cy="50" r="${radius}" fill="none"
          stroke="${color}" stroke-width="8"
          stroke-dasharray="${circumference}"
          stroke-dashoffset="${offset}"/>
      </svg>
      <span class="score-ring-text">${pct}</span>
    </div>
    <div class="badge badge-${cls}" style="margin-top:8px;">${Fmt.classify(score >= 80 ? 'EXCELLENT' : score >= 60 ? 'GOOD' : score >= 40 ? 'NEEDS_IMPROVEMENT' : 'POOR')}</div>
  `;
}

// ── Icon rendering helper ──
function renderIcons(container = document) {
  if (typeof lucide !== 'undefined' && typeof lucide.createIcons === 'function') {
    lucide.createIcons({
      root: container
    });
  }
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => renderIcons());
} else {
  renderIcons();
}

window.Auth = Auth;
window.Toast = Toast;
window.Fmt = Fmt;
window.$ = $;
window.$$ = $$;
window.el = el;
window.openModal = openModal;
window.closeModal = closeModal;
window.closeAllModals = closeAllModals;
window.renderUserCard = renderUserCard;
window.setActiveSidebarLink = setActiveSidebarLink;
window.renderScoreRing = renderScoreRing;
window.renderIcons = renderIcons;
window.escapeHtml = escapeHtml;
window.formatDate = formatDate;
window.formatDateTime = formatDateTime;
window.formatScore = formatScore;

