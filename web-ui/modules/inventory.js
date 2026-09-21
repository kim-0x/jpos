export function renderProducts(container, products, formatCurrency) {
  container.innerHTML = `
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
            ${products
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

export function mountInventory(container, { formatCurrency, products, state, stockHealth }) {
  container.innerHTML = `
    <article class="card">
      <div class="section-header">
        <div>
          <h2>Inventory Health</h2>
          <p>Quick stock visibility prototype.</p>
        </div>
        <button id="toggle-stock-entry" class="button button-primary" type="button">Stock Entry</button>
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
      <section id="stock-entry-panel" class="stock-entry-panel hidden-panel" aria-hidden="true">
        <div class="section-header">
          <div>
            <h3>Stock Entry</h3>
            <p>Record incoming stock using product barcode, cost, and quantity.</p>
          </div>
          <button id="close-stock-entry" class="button button-secondary" type="button">Close</button>
        </div>
        <form id="stock-entry-form" class="stock-entry-grid">
          <div class="field">
            <label for="stock-barcode">Barcode</label>
            <input id="stock-barcode" type="text" inputmode="numeric" placeholder="Enter product barcode">
          </div>
          <div class="field">
            <label for="stock-cost">Cost</label>
            <input id="stock-cost" type="number" min="0" step="0.01" placeholder="0.00">
          </div>
          <div class="field">
            <label for="stock-quantity">Quantity</label>
            <input id="stock-quantity" type="number" min="1" step="1" value="1">
          </div>
          <div class="field actions-field">
            <button class="button button-primary" type="submit">Add Entry</button>
          </div>
        </form>
        <p id="stock-entry-feedback" class="sales-feedback" aria-live="polite"></p>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th scope="col">Barcode</th>
                <th scope="col">Product</th>
                <th scope="col">Cost</th>
                <th scope="col">Quantity</th>
              </tr>
            </thead>
            <tbody id="stock-entry-body"></tbody>
          </table>
        </div>
      </section>
    </article>
  `;

  const panel = container.querySelector('#stock-entry-panel');
  const toggleButton = container.querySelector('#toggle-stock-entry');
  const closeButton = container.querySelector('#close-stock-entry');
  const form = container.querySelector('#stock-entry-form');
  const barcodeField = container.querySelector('#stock-barcode');
  const costField = container.querySelector('#stock-cost');
  const quantityField = container.querySelector('#stock-quantity');
  const feedback = container.querySelector('#stock-entry-feedback');
  const body = container.querySelector('#stock-entry-body');

  function setPanelOpen(isOpen) {
    panel.classList.toggle('hidden-panel', !isOpen);
    panel.setAttribute('aria-hidden', String(!isOpen));

    if (isOpen) {
      barcodeField.focus();
    }
  }

  function renderEntries() {
    body.textContent = '';

    if (state.stockEntries.length === 0) {
      const emptyRow = document.createElement('tr');
      const emptyCell = document.createElement('td');
      emptyCell.colSpan = 4;
      emptyCell.className = 'empty-state-cell';
      emptyCell.textContent = 'No stock entries recorded yet.';
      emptyRow.appendChild(emptyCell);
      body.appendChild(emptyRow);
      return;
    }

    state.stockEntries.forEach((entry) => {
      const row = document.createElement('tr');
      [entry.barcode, entry.name, formatCurrency(entry.cost), String(entry.quantity)].forEach((value) => {
        const cell = document.createElement('td');
        cell.textContent = value;
        row.appendChild(cell);
      });
      body.appendChild(row);
    });
  }

  toggleButton.addEventListener('click', () => {
    setPanelOpen(true);
  });

  closeButton.addEventListener('click', () => {
    setPanelOpen(false);
    feedback.textContent = '';
    delete feedback.dataset.state;
  });

  form.addEventListener('submit', (event) => {
    event.preventDefault();

    const barcode = barcodeField.value.trim();
    const cost = Number(costField.value);
    const quantity = Number(quantityField.value);
    const product = products.find((item) => item.barcode === barcode);

    if (!product) {
      feedback.textContent = 'Barcode not found in the product catalog.';
      feedback.dataset.state = 'error';
      return;
    }

    if (Number.isNaN(cost) || cost < 0) {
      feedback.textContent = 'Cost must be zero or greater.';
      feedback.dataset.state = 'error';
      return;
    }

    if (!Number.isInteger(quantity) || quantity < 1) {
      feedback.textContent = 'Quantity must be a whole number greater than zero.';
      feedback.dataset.state = 'error';
      return;
    }

    state.stockEntries.unshift({
      barcode: product.barcode,
      cost,
      name: product.name,
      quantity
    });

    feedback.textContent = `${product.name} stock entry recorded.`;
    feedback.dataset.state = 'success';
    barcodeField.value = '';
    costField.value = '';
    quantityField.value = '1';
    renderEntries();
    barcodeField.focus();
  });

  renderEntries();
}
