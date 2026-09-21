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

export function renderInventory(container, stockHealth) {
  container.innerHTML = `
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
