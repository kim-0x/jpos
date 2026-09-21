export function renderDashboard(container, dashboardMetrics) {
  container.innerHTML = `
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
