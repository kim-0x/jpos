export function renderReports(container, reportItems, generatedAt) {
  container.innerHTML = `
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
