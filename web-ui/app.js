const navItems = [
  { id: 'dashboard', label: 'Dashboard' },
  { id: 'products', label: 'Products' },
  { id: 'inventory', label: 'Inventory' },
  { id: 'sales', label: 'Sales' },
  { id: 'reports', label: 'Reports' },
  { id: 'login', label: 'Login' }
];

const dashboardMetrics = [
  { label: 'Today Revenue', value: '$4,280.50' },
  { label: 'Transactions', value: '143' },
  { label: 'Active Cashiers', value: '5' },
  { label: 'Low Stock Items', value: '8' }
];

const productRows = [
  { code: 'PRD-001', name: 'Premium Rice 5kg', category: 'Grocery', price: '$11.90' },
  { code: 'PRD-002', name: 'Cooking Oil 2L', category: 'Grocery', price: '$8.20' },
  { code: 'PRD-003', name: 'Laundry Soap', category: 'Household', price: '$2.10' }
];

const stockHealth = [
  { item: 'Premium Rice 5kg', level: 72, status: 'Healthy', statusClass: 'status-ok' },
  { item: 'Cooking Oil 2L', level: 34, status: 'Low', statusClass: 'status-low' },
  { item: 'Laundry Soap', level: 18, status: 'Critical', statusClass: 'status-critical' }
];

const recentSales = [
  { id: 'TX-1102', total: '$87.20', items: 5 },
  { id: 'TX-1101', total: '$53.40', items: 3 },
  { id: 'TX-1100', total: '$29.95', items: 2 }
];

const reportItems = [
  'Daily sales summary',
  'Inventory valuation',
  'Low stock reorder list'
];

const generatedAt = `${new Intl.DateTimeFormat('en-US', {
  dateStyle: 'medium',
  timeStyle: 'short',
  timeZone: 'UTC'
}).format(new Date())} UTC`;

function renderNav() {
  const nav = document.getElementById('nav');
  nav.innerHTML = navItems
    .map(
      (item) => `
        <a class="nav-link" data-page-link="${item.id}" href="#${item.id}">
          ${item.label}
        </a>
      `
    )
    .join('');
}

function renderDashboard() {
  document.getElementById('page-dashboard').innerHTML = `
    <div class="section-header">
      <div>
        <h2>Dashboard</h2>
        <p>Snapshot of store performance for the current day.</p>
      </div>
      <span class="chip">Prototype mode</span>
    </div>
    <div class="grid metrics-grid">
      ${dashboardMetrics
        .map(
          (metric) => `
            <article class="card">
              <h3>${metric.label}</h3>
              <p class="metric-value">${metric.value}</p>
            </article>
          `
        )
        .join('')}
    </div>
  `;
}

function renderProducts() {
  document.getElementById('page-products').innerHTML = `
    <article class="card">
      <div class="section-header">
        <div>
          <h2>Product Catalog</h2>
          <p>Prototype listing layout for item management.</p>
        </div>
      </div>
      <div class="button-row">
        <button class="button button-primary" type="button">Add product</button>
        <button class="button button-secondary" type="button">Import CSV</button>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Code</th>
              <th>Name</th>
              <th>Category</th>
              <th>Price</th>
            </tr>
          </thead>
          <tbody>
            ${productRows
              .map(
                (row) => `
                  <tr>
                    <td>${row.code}</td>
                    <td>${row.name}</td>
                    <td>${row.category}</td>
                    <td>${row.price}</td>
                  </tr>
                `
              )
              .join('')}
          </tbody>
        </table>
      </div>
    </article>
  `;
}

function renderInventory() {
  document.getElementById('page-inventory').innerHTML = `
    <article class="card">
      <div class="section-header">
        <div>
          <h2>Inventory Health</h2>
          <p>Quick stock visibility prototype.</p>
        </div>
      </div>
      <div class="progress-list">
        ${stockHealth
          .map(
            (item) => `
              <div class="progress-row">
                <div class="progress-meta">
                  <span>${item.item}</span>
                  <span class="status-badge ${item.statusClass}">${item.status} · ${item.level}%</span>
                </div>
                <div
                  class="progress-track"
                  role="progressbar"
                  aria-label="${item.item} stock level"
                  aria-valuemin="0"
                  aria-valuemax="100"
                  aria-valuenow="${item.level}"
                >
                  <div class="progress-bar" style="width: ${item.level}%"></div>
                </div>
              </div>
            `
          )
          .join('')}
      </div>
    </article>
  `;
}

function renderSales() {
  document.getElementById('page-sales').innerHTML = `
    <div class="grid split-grid">
      <article class="card">
        <h2>POS Terminal</h2>
        <p class="card-subtitle">Prototype cashier workflow.</p>
        <form class="form-grid">
          <div class="field">
            <label for="barcode">Barcode</label>
            <input id="barcode" type="text" placeholder="Scan product">
          </div>
          <div class="field">
            <label for="quantity">Qty</label>
            <input id="quantity" type="number" min="1" value="1">
          </div>
          <div class="field actions-field">
            <button class="button button-primary" type="button">Add item</button>
          </div>
        </form>
      </article>
      <article class="card">
        <h2>Recent Transactions</h2>
        <p class="card-subtitle">Latest activity from the checkout counter.</p>
        <ul class="list">
          ${recentSales
            .map(
              (sale) => `
                <li class="list-item">
                  <div>
                    <strong>${sale.id}</strong>
                    <br>
                    <small>${sale.items} items</small>
                  </div>
                  <strong>${sale.total}</strong>
                </li>
              `
            )
            .join('')}
        </ul>
      </article>
    </div>
  `;
}

function renderReports() {
  document.getElementById('page-reports').innerHTML = `
    <article class="card">
      <div class="section-header">
        <div>
          <h2>Reports</h2>
          <p>Generated ${generatedAt}</p>
        </div>
      </div>
      <ul class="list">
        ${reportItems
          .map(
            (item) => `
              <li class="list-item">
                <span>${item}</span>
                <span class="status-badge status-ok">Ready</span>
              </li>
            `
          )
          .join('')}
      </ul>
      <div class="report-actions">
        <button class="button button-secondary" type="button">Export JSON</button>
        <button class="button button-primary" type="button">View report</button>
      </div>
    </article>
  `;
}

function renderLogin() {
  document.getElementById('page-login').innerHTML = `
    <div class="login-layout">
      <article class="card">
        <h2>Sign in to JPOS</h2>
        <p class="card-subtitle">Prototype login form (UI only).</p>
        <form id="login-form" class="login-form">
          <div class="field">
            <label for="username">Username</label>
            <input id="username" type="text" placeholder="admin">
          </div>
          <div class="field">
            <label for="password">Password</label>
            <input id="password" type="password" placeholder="••••••••">
          </div>
          <div class="field">
            <label for="role">Role</label>
            <select id="role">
              <option value="admin">Admin</option>
              <option value="manager">Store manager</option>
              <option value="cashier">Cashier</option>
            </select>
          </div>
        </form>
        <div class="login-actions">
          <button class="button button-secondary" type="reset" form="login-form">Reset</button>
          <button class="button button-primary" type="button">Sign in</button>
        </div>
      </article>
    </div>
  `;
}

function activatePage(pageId) {
  const resolvedPage = navItems.some((item) => item.id === pageId) ? pageId : 'dashboard';

  document.querySelectorAll('.page').forEach((page) => {
    page.classList.toggle('active', page.id === `page-${resolvedPage}`);
  });

  document.querySelectorAll('[data-page-link]').forEach((link) => {
    link.classList.toggle('active', link.dataset.pageLink === resolvedPage);
  });
}

function syncPageWithHash() {
  activatePage(window.location.hash.replace('#', '') || 'dashboard');
}

renderNav();
renderDashboard();
renderProducts();
renderInventory();
renderSales();
renderReports();
renderLogin();
syncPageWithHash();

window.addEventListener('hashchange', syncPageWithHash);
