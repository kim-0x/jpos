# Web UI

This prototype uses plain HTML, CSS, and JavaScript so it can be deployed as static files.

## Run locally

Run the lightweight local server:

```bash
npm start
```

Then open `http://localhost:4200/`.

## Deploy

Upload these files to any static host:

- `index.html`
- `styles.css`
- `app.js`
- `favicon.ico`

Host the files from the `web-ui/` directory root. Navigation uses hash routes (for example `#dashboard`), so no server-side route rewrites are required.

No build step is required.
