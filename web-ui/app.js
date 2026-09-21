import {
  APP_NAME,
  dashboardMetrics,
  demoUsers,
  products,
  reportItems,
  stockHealth
} from './data/app-data.js';
import { renderDashboard } from './modules/dashboard.js';
import { renderInventory, renderProducts } from './modules/inventory.js';
import { renderLogin, bindLoginInteractions } from './modules/login.js';
import { mountSales } from './modules/sales.js';
import { renderReports } from './modules/reports.js';
import { renderUsers } from './modules/users.js';

const navItems = [
  { id: 'dashboard', label: 'Dashboard' },
  { id: 'products', label: 'Products' },
  { id: 'inventory', label: 'Inventory' },
  { id: 'sales', label: 'Sales' },
  { id: 'reports', label: 'Reports' },
  { id: 'users', label: 'Users' },
  { id: 'login', label: 'Login' }
];

const appState = {
  currentUser: null,
  cart: [],
  transactions: []
};

const generatedAt = `${new Intl.DateTimeFormat('en-US', {
  dateStyle: 'medium',
  timeStyle: 'short',
  timeZone: 'UTC'
}).format(new Date())} UTC`;

function formatCurrency(value) {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD'
  }).format(value);
}

function getUserHomePage() {
  return appState.currentUser?.homePage || 'dashboard';
}

function getVisibleNavItems() {
  if (!appState.currentUser) {
    return navItems.filter((item) => item.id !== 'users');
  }

  return navItems.filter((item) => appState.currentUser.allowedPages.includes(item.id));
}

function resolvePage(pageId) {
  const fallbackPage = getUserHomePage();
  const visiblePageIds = getVisibleNavItems().map((item) => item.id);
  return visiblePageIds.includes(pageId) ? pageId : fallbackPage;
}

function renderNav() {
  const nav = document.getElementById('nav');
  nav.innerHTML = getVisibleNavItems()
    .map(
      (item) => `
        <a class="nav-link" data-page-link="${item.id}" href="#${item.id}">
          ${item.label}
        </a>
      `
    )
    .join('');
}

function updateTopbarAction() {
  const topbarLink = document.querySelector('.topbar-link');

  if (!topbarLink) {
    return;
  }

  if (appState.currentUser) {
    topbarLink.textContent = `${appState.currentUser.displayName} · Sign out`;
    topbarLink.href = '#';
    topbarLink.removeAttribute('data-page-link');
    topbarLink.classList.remove('active');
    topbarLink.removeAttribute('aria-current');
  } else {
    topbarLink.textContent = 'Sign in';
    topbarLink.href = '#login';
    topbarLink.dataset.pageLink = 'login';
  }
}

function activatePage(pageId) {
  const resolvedPage = resolvePage(pageId);

  document.querySelectorAll('.page').forEach((page) => {
    page.classList.toggle('active', page.id === `page-${resolvedPage}`);
  });

  document.querySelectorAll('[data-page-link]').forEach((link) => {
    const isActive = link.dataset.pageLink === resolvedPage;
    link.classList.toggle('active', isActive);
    link.setAttribute('aria-current', isActive ? 'page' : 'false');
  });
}

function renderPages() {
  renderDashboard(document.getElementById('page-dashboard'), dashboardMetrics);
  renderProducts(document.getElementById('page-products'), products, formatCurrency);
  renderInventory(document.getElementById('page-inventory'), stockHealth);
  mountSales(document.getElementById('page-sales'), {
    formatCurrency,
    products,
    state: appState
  });
  renderReports(document.getElementById('page-reports'), reportItems, generatedAt);
  renderUsers(document.getElementById('page-users'), demoUsers);
  renderLogin(document.getElementById('page-login'), APP_NAME);

  bindLoginInteractions(document.getElementById('page-login'), {
    demoUsers,
    onLogin: (user) => {
      appState.currentUser = user;
      renderNav();
      updateTopbarAction();
      window.location.hash = user.homePage;
      activatePage(user.homePage);
    }
  });
}

function bindTopbarAction() {
  const topbarLink = document.querySelector('.topbar-link');

  if (!topbarLink) {
    return;
  }

  topbarLink.addEventListener('click', (event) => {
    if (!appState.currentUser) {
      return;
    }

    event.preventDefault();
    appState.currentUser = null;
    appState.cart = [];
    renderNav();
    updateTopbarAction();
    renderPages();
    window.location.hash = 'login';
    activatePage('login');
  });
}

function syncPageWithHash() {
  activatePage(window.location.hash.replace('#', '') || getUserHomePage());
}

document.title = `${APP_NAME} Web UI Prototype`;
document.querySelector('.brand').textContent = APP_NAME;
document.querySelector('.eyebrow').textContent = 'Grocery operations prototype';
document.querySelector('.topbar h1').textContent = `${APP_NAME} Web UI Prototype`;

renderNav();
renderPages();
updateTopbarAction();
bindTopbarAction();
syncPageWithHash();

window.addEventListener('hashchange', syncPageWithHash);
