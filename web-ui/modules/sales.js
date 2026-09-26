const changeDenominations = [
  { label: '$100 bill', value: 100 },
  { label: '$50 bill', value: 50 },
  { label: '$20 bill', value: 20 },
  { label: '$10 bill', value: 10 },
  { label: '$5 bill', value: 5 },
  { label: '$1 bill', value: 1 },
  { label: '25¢ coin', value: 0.25 },
  { label: '10¢ coin', value: 0.1 },
  { label: '5¢ coin', value: 0.05 },
  { label: '1¢ coin', value: 0.01 }
];

function formatCount(label, count) {
  return `${count} × ${label}`;
}

function calculateChange(changeDue) {
  let remaining = Math.round(changeDue * 100);

  return changeDenominations
    .map((denomination) => {
      const denominationCents = Math.round(denomination.value * 100);
      const count = Math.floor(remaining / denominationCents);
      remaining -= count * denominationCents;

      return count > 0 ? formatCount(denomination.label, count) : null;
    })
    .filter(Boolean);
}

function nextTransactionId(transactions) {
  return `TX-${String(1100 + transactions.length + 1).padStart(4, '0')}`;
}

function renderTransactionsTable() {
  return `
    <article class="card">
      <h2>Sales Transactions</h2>
      <p class="card-subtitle">Completed transactions for the current session.</p>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th scope="col">Transaction</th>
              <th scope="col">Items</th>
              <th scope="col">Total</th>
              <th scope="col">Cash</th>
              <th scope="col">Change</th>
            </tr>
          </thead>
          <tbody id="transaction-body"></tbody>
        </table>
      </div>
    </article>
  `;
}

export function mountSales(container, { currentUser, products, state, formatCurrency }) {
  const role = currentUser?.role;
  const showTerminal = role === 'Cashier';
  const showTransactions = role === 'Admin';

  container.innerHTML = `
    <div class="grid split-grid">
      ${
        showTerminal
          ? `
            <article class="card">
              <h2>Sales Terminal</h2>
              <p class="card-subtitle">Prototype cashier workflow for grocery checkout.</p>
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
          `
          : `
            <article class="card">
              <h2>Sales</h2>
              <p class="card-subtitle">Transaction oversight for the current session.</p>
            </article>
          `
      }
      <div class="sales-sidebar">
        ${
          showTerminal
            ? `
              <article class="card total-card">
                <p class="total-label">Grand Total</p>
                <p id="grand-total" class="grand-total-value">${formatCurrency(0)}</p>
              </article>
              <article class="card">
                <h2>Payment</h2>
                <p class="card-subtitle">Collect cash and confirm the optimal change breakdown.</p>
                <form id="payment-form" class="payment-grid">
                  <div class="field">
                    <label for="cash-received">Cash Received</label>
                    <input id="cash-received" type="number" min="0" step="0.01" placeholder="0.00">
                  </div>
                  <div class="field actions-field">
                    <button class="button button-primary" type="submit">Pay</button>
                  </div>
                </form>
                <p id="payment-feedback" class="sales-feedback" aria-live="polite"></p>
                <div class="change-panel">
                  <div class="change-summary">
                    <span>Change Due</span>
                    <strong id="change-due">${formatCurrency(0)}</strong>
                  </div>
                  <ul id="change-breakdown" class="change-breakdown">
                    <li>No change breakdown yet.</li>
                  </ul>
                </div>
              </article>
            `
            : ''
        }
        ${showTransactions ? renderTransactionsTable() : ''}
      </div>
    </div>
  `;

  const transactionBody = container.querySelector('#transaction-body');

  function renderTransactions() {
    if (!transactionBody) {
      return;
    }

    transactionBody.textContent = '';

    if (state.transactions.length === 0) {
      const emptyRow = document.createElement('tr');
      const emptyCell = document.createElement('td');
      emptyCell.colSpan = 5;
      emptyCell.className = 'empty-state-cell';
      emptyCell.textContent = 'No completed sales yet.';
      emptyRow.appendChild(emptyCell);
      transactionBody.appendChild(emptyRow);
      return;
    }

    state.transactions.forEach((transaction) => {
      const row = document.createElement('tr');
      [
        transaction.id,
        String(transaction.items),
        formatCurrency(transaction.total),
        formatCurrency(transaction.cashReceived),
        formatCurrency(transaction.changeDue)
      ].forEach((value) => {
        const cell = document.createElement('td');
        cell.textContent = value;
        row.appendChild(cell);
      });
      transactionBody.appendChild(row);
    });
  }

  if (!showTerminal) {
    renderTransactions();
    return;
  }

  const salesForm = container.querySelector('#sales-form');
  const barcodeInput = container.querySelector('#barcode');
  const quantityInput = container.querySelector('#quantity');
  const salesFeedback = container.querySelector('#sales-feedback');
  const cartBody = container.querySelector('#cart-body');
  const grandTotal = container.querySelector('#grand-total');
  const paymentForm = container.querySelector('#payment-form');
  const cashReceivedInput = container.querySelector('#cash-received');
  const paymentFeedback = container.querySelector('#payment-feedback');
  const changeDue = container.querySelector('#change-due');
  const changeBreakdown = container.querySelector('#change-breakdown');

  function renderCart() {
    cartBody.textContent = '';

    if (state.cart.length === 0) {
      const emptyRow = document.createElement('tr');
      const emptyCell = document.createElement('td');
      emptyCell.colSpan = 4;
      emptyCell.className = 'empty-state-cell';
      emptyCell.textContent = 'No items in the cart yet.';
      emptyRow.appendChild(emptyCell);
      cartBody.appendChild(emptyRow);
    } else {
      state.cart.forEach((item) => {
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

    const total = state.cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
    grandTotal.textContent = formatCurrency(total);
  }

  function renderChangeBreakdown(lines) {
    changeBreakdown.textContent = '';

    lines.forEach((line) => {
      const item = document.createElement('li');
      item.textContent = line;
      changeBreakdown.appendChild(item);
    });
  }

  function resetPaymentFeedback() {
    paymentFeedback.textContent = '';
    delete paymentFeedback.dataset.state;
  }

  salesForm.addEventListener('submit', (event) => {
    event.preventDefault();

    const barcode = barcodeInput.value.trim();
    const quantity = Number(quantityInput.value);
    const product = products.find((item) => item.barcode === barcode);

    if (!product) {
      salesFeedback.textContent = 'Barcode not found. Use a configured inventory barcode.';
      salesFeedback.dataset.state = 'error';
      return;
    }

    if (!Number.isInteger(quantity) || quantity < 1) {
      salesFeedback.textContent = 'Quantity must be a whole number greater than zero.';
      salesFeedback.dataset.state = 'error';
      return;
    }

    const existingItem = state.cart.find((item) => item.code === product.code);

    if (existingItem) {
      existingItem.quantity += quantity;
    } else {
      state.cart.push({
        code: product.code,
        name: product.name,
        price: product.price,
        quantity
      });
    }

    salesFeedback.textContent = `${product.name} added to cart.`;
    salesFeedback.dataset.state = 'success';
    barcodeInput.value = '';
    quantityInput.value = '1';
    renderCart();
    resetPaymentFeedback();
    changeDue.textContent = formatCurrency(0);
    renderChangeBreakdown(['No change breakdown yet.']);
    barcodeInput.focus();
  });

  paymentForm.addEventListener('submit', (event) => {
    event.preventDefault();

    const total = state.cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
    const cashReceived = Number(cashReceivedInput.value);

    if (state.cart.length === 0) {
      paymentFeedback.textContent = 'Add cart items before completing payment.';
      paymentFeedback.dataset.state = 'error';
      return;
    }

    if (Number.isNaN(cashReceived) || cashReceived <= 0) {
      paymentFeedback.textContent = 'Enter a valid cash amount.';
      paymentFeedback.dataset.state = 'error';
      return;
    }

    if (cashReceived < total) {
      paymentFeedback.textContent = 'Cash received is less than the grand total.';
      paymentFeedback.dataset.state = 'error';
      return;
    }

    const change = Number((cashReceived - total).toFixed(2));
    const changeLines = change === 0 ? ['No change due.'] : calculateChange(change);

    state.transactions.unshift({
      id: nextTransactionId(state.transactions),
      items: state.cart.reduce((sum, item) => sum + item.quantity, 0),
      total,
      cashReceived,
      changeDue: change
    });

    paymentFeedback.textContent = 'Payment completed and transaction recorded.';
    paymentFeedback.dataset.state = 'success';
    changeDue.textContent = formatCurrency(change);
    renderChangeBreakdown(changeLines);
    state.cart = [];
    cashReceivedInput.value = '';
    renderCart();
  });

  renderCart();
}
