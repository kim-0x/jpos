export function renderUsers(container, demoUsers) {
  container.innerHTML = `
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
