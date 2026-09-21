const navItems = [
  { id: 'dashboard', label: 'Dashboard' },
  { id: 'products', label: 'Products' },
  { id: 'inventory', label: 'Inventory' },
  { id: 'sales', label: 'Sales' },
  { id: 'reports', label: 'Reports' },
  { id: 'users', label: 'Users' },
  { id: 'login', label: 'Login' }
];

const dashboardMetrics = [
  { label: 'Today Revenue', value: '$4,280.50' },
  { label: 'Transactions', value: '143' },
  { label: 'Active Cashiers', value: '5' },
  { label: 'Low Stock Items', value: '8' }
];

const productRows = [
  { code: 'PRD-001', barcode: '1001001', name: 'Premium Rice 5kg', category: 'Grocery', price: 11.9 },
  { code: 'PRD-002', barcode: '1001002', name: 'Cooking Oil 2L', category: 'Grocery', price: 8.2 },
  { code: 'PRD-003', barcode: '1001003', name: 'Laundry Soap', category: 'Household', price: 2.1 },
  { code: 'PRD-004', barcode: '1001004', name: 'Brown Sugar 1kg', category: 'Grocery', price: 1.85 },
  { code: 'PRD-005', barcode: '1001005', name: 'Instant Noodles Pack', category: 'Grocery', price: 3.4 },
  { code: 'PRD-006', barcode: '1001006', name: 'Milk 1L', category: 'Dairy', price: 2.65 },
  { code: 'PRD-007', barcode: '1001007', name: 'Eggs 12pcs', category: 'Dairy', price: 3.95 },
  { code: 'PRD-008', barcode: '1001008', name: 'Orange Juice 1L', category: 'Beverage', price: 2.8 },
  { code: 'PRD-009', barcode: '1001009', name: 'Dishwashing Liquid', category: 'Household', price: 4.35 },
  { code: 'PRD-010', barcode: '1001010', name: 'Bath Tissue 6 Rolls', category: 'Household', price: 5.6 }
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

const demoUsers = [
  {
    username: 'admin',
    password: 'admin123',
    displayName: 'Ava Admin',
    role: 'Admin',
    homePage: 'dashboard',
    allowedPages: ['dashboard', 'products', 'inventory', 'sales', 'reports', 'users']
  },
  {
    username: 'stock.manager',
    password: 'stock123',
    displayName: 'Milo Stock Manager',
    role: 'Stock Manager',
    homePage: 'inventory',
    allowedPages: ['dashboard', 'products', 'inventory']
  },
  {
    username: 'cashier',
    password: 'cashier123',
    displayName: 'Casey Cashier',
    role: 'Cashier',
    homePage: 'sales',
    allowedPages: ['sales']
  }
];

const appState = {
  currentUser: null,
  cart: []
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

  if (visiblePageIds.includes(pageId)) {
    return pageId;
  }

  return fallbackPage;
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
              <th scope="col">Code</th>
              <th scope="col">Barcode</th>
              <th scope="col">Name</th>
              <th scope="col">Category</th>
              <th scope="col">Price</th>
            </tr>
          </thead>
          <tbody>
            ${productRows
              .map(
                (row) => `
                  <tr>
                    <td>${row.code}</td>
                    <td>${row.barcode}</td>
                    <td>${row.name}</td>
                    <td>${row.category}</td>
                    <td>${formatCurrency(row.price)}</td>
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
                  <div class="progress-indicators">
                    <span class="progress-value">${item.level}%</span>
                    <span class="status-badge ${item.statusClass}">${item.status}</span>
                  </div>
                </div>
                <div
                  class="progress-track"
                  role="progressbar"
                  aria-label="${item.item} stock level"
                  aria-valuemin="0"
                  aria-valuemax="100"
                  aria-valuenow="${item.level}"
                  aria-valuetext="${item.status}, ${item.level}%"
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
        <form id="sales-form" class="form-grid">
          <div class="field">
            <label for="barcode">Barcode</label>
            <input id="barcode" type="text" placeholder="Scan product barcode" inputmode="numeric">
          </div>
          <div class="field">
            <label for="quantity">Qty</label>
            <input id="quantity" type="number" min="1" value="1">
          </div>
          <div class="field actions-field">
            <button class="button button-primary" type="submit">Add item</button>
          </div>
        </form>
        <p id="sales-feedback" class="sales-feedback" aria-live="polite"></p>
        <div class="table-wrap cart-table">
          <table>
            <thead>
              <tr>
                <th scope="col">Product</th>
                <th scope="col">Quantity</th>
                <th scope="col">Price</th>
                <th scope="col">Subtotal</th>
              </tr>
            </thead>
            <tbody id="cart-body"></tbody>
          </table>
        </div>
      </article>
      <div class="sales-sidebar">
        <article class="card total-card">
          <p class="total-label">Grand Total</p>
          <p id="grand-total" class="grand-total-value">${formatCurrency(0)}</p>
          <p class="card-subtitle">Clearly visible for both cashier and customer.</p>
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

function renderUsers() {
  document.getElementById('page-users').innerHTML = `
    <article class="card">
      <div class="section-header">
        <div>
          <h2>Users</h2>
          <p>Mock user accounts with their assigned role permissions.</p>
        </div>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th scope="col">Username</th>
              <th scope="col">Role</th>
              <th scope="col">Allowed Pages</th>
            </tr>
          </thead>
          <tbody>
            ${demoUsers
              .map(
                (user) => `
                  <tr>
                    <td>${user.username}</td>
                    <td>${user.role}</td>
                    <td>${user.allowedPages.join(', ')}</td>
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

function renderLogin() {
  document.getElementById('page-login').innerHTML = `
    <div class="login-layout">
      <article class="card">
        <h2>Sign in to JPOS</h2>
        <p class="card-subtitle">Enter a mock account username and password. The role is resolved automatically from the account.</p>
        <form id="login-form" class="login-form">
          <div class="field">
            <label for="username">Username</label>
            <input id="username" type="text" placeholder="Enter username" autocomplete="username">
          </div>
          <div class="field">
            <label for="password">Password</label>
            <input id="password" type="password" placeholder="Enter password" autocomplete="current-password">
          </div>
        </form>
        <p class="helper-text">Use the configured mock credentials for this prototype.</p>
        <p id="login-feedback" class="login-feedback" aria-live="polite"></p>
        <div class="login-actions">
          <button class="button button-secondary" type="reset" form="login-form">Reset</button>
          <button id="sign-in-button" class="button button-primary" type="submit" form="login-form">Sign in</button>
        </div>
      </article>
    </div>
  `;
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

function renderCart() {
  const cartBody = document.getElementById('cart-body');
  const grandTotal = document.getElementById('grand-total');

  if (!cartBody || !grandTotal) {
    return;
  }

  cartBody.textContent = '';

  if (appState.cart.length === 0) {
    const emptyRow = document.createElement('tr');
    const emptyCell = document.createElement('td');
    emptyCell.colSpan = 4;
    emptyCell.className = 'empty-state-cell';
    emptyCell.textContent = 'No items in the cart yet.';
    emptyRow.appendChild(emptyCell);
    cartBody.appendChild(emptyRow);
  } else {
    appState.cart.forEach((item) => {
      const row = document.createElement('tr');
      const subtotal = item.price * item.quantity;

      [item.name, String(item.quantity), formatCurrency(item.price), formatCurrency(subtotal)].forEach((value) => {
        const cell = document.createElement('td');
        cell.textContent = value;
        row.appendChild(cell);
      });

      cartBody.appendChild(row);
    });
  }

  const total = appState.cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
  grandTotal.textContent = formatCurrency(total);
}

function bindSalesInteractions() {
  const salesForm = document.getElementById('sales-form');
  const barcodeInput = document.getElementById('barcode');
  const quantityInput = document.getElementById('quantity');
  const feedback = document.getElementById('sales-feedback');

  if (!salesForm || !barcodeInput || !quantityInput || !feedback) {
    return;
  }

  renderCart();

  salesForm.addEventListener('submit', (event) => {
    event.preventDefault();

    const barcode = barcodeInput.value.trim();
    const quantity = Number(quantityInput.value);
    const product = productRows.find((item) => item.barcode === barcode);

    if (!product) {
      feedback.textContent = 'Barcode not found. Use one of the products listed below.';
      feedback.dataset.state = 'error';
      return;
    }

    if (!Number.isInteger(quantity) || quantity < 1) {
      feedback.textContent = 'Quantity must be a whole number greater than zero.';
      feedback.dataset.state = 'error';
      return;
    }

    const existingItem = appState.cart.find((item) => item.code === product.code);

    if (existingItem) {
      existingItem.quantity += quantity;
    } else {
      appState.cart.push({
        code: product.code,
        name: product.name,
        price: product.price,
        quantity
      });
    }

    feedback.textContent = `${product.name} added to cart.`;
    feedback.dataset.state = 'success';
    barcodeInput.value = '';
    quantityInput.value = '1';
    renderCart();
    barcodeInput.focus();
  });
}

function bindLoginInteractions() {
  const loginForm = document.getElementById('login-form');
  const usernameField = document.getElementById('username');
  const passwordField = document.getElementById('password');
  const feedback = document.getElementById('login-feedback');

  if (!loginForm || !usernameField || !passwordField || !feedback) {
    return;
  }

  loginForm.addEventListener('submit', (event) => {
    event.preventDefault();

    const username = usernameField.value.trim();
    const matchedUser = demoUsers.find((user) => user.username === username);

    if (!matchedUser || passwordField.value !== matchedUser.password) {
      feedback.textContent = 'Enter a valid mock username and matching password.';
      feedback.dataset.state = 'error';
      return;
    }

    appState.currentUser = matchedUser;
    feedback.textContent = '';
    renderNav();
    updateTopbarAction();
    window.location.hash = matchedUser.homePage;
    activatePage(matchedUser.homePage);
  });

  loginForm.addEventListener('reset', () => {
    window.setTimeout(() => {
      usernameField.value = '';
      passwordField.value = '';
      feedback.textContent = '';
      delete feedback.dataset.state;
    }, 0);
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
    renderSales();
    renderLogin();
    bindSalesInteractions();
    bindLoginInteractions();
    window.location.hash = 'login';
    activatePage('login');
  });
}

function syncPageWithHash() {
  activatePage(window.location.hash.replace('#', '') || getUserHomePage());
}

renderNav();
renderDashboard();
renderProducts();
renderInventory();
renderSales();
renderReports();
renderUsers();
renderLogin();
updateTopbarAction();
bindSalesInteractions();
bindLoginInteractions();
bindTopbarAction();
syncPageWithHash();

window.addEventListener('hashchange', syncPageWithHash);
