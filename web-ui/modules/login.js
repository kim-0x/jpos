export function renderLogin(container, appName) {
  container.innerHTML = `
    <div class="login-layout">
      <article class="card">
        <h2>Sign in to ${appName}</h2>
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
        <p class="helper-text">Use one of the configured prototype usernames and passwords.</p>
        <p id="login-feedback" class="login-feedback" aria-live="polite"></p>
        <div class="login-actions">
          <button class="button button-secondary" type="reset" form="login-form">Reset</button>
          <button class="button button-primary" type="submit" form="login-form">Sign in</button>
        </div>
      </article>
    </div>
  `;
}

export function bindLoginInteractions(container, { demoUsers, onLogin }) {
  const loginForm = container.querySelector('#login-form');
  const usernameField = container.querySelector('#username');
  const passwordField = container.querySelector('#password');
  const feedback = container.querySelector('#login-feedback');

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

    feedback.textContent = '';
    delete feedback.dataset.state;
    onLogin(matchedUser);
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
