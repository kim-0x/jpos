const http = require('http');
const fs = require('fs');
const path = require('path');

const port = 4200;
const root = __dirname;

const contentTypes = {
  '.css': 'text/css; charset=utf-8',
  '.html': 'text/html; charset=utf-8',
  '.ico': 'image/x-icon',
  '.js': 'application/javascript; charset=utf-8'
};

http
  .createServer((request, response) => {
    const rawPath = (request.url || '/').split('?')[0];
    const relativePath = rawPath === '/' ? 'index.html' : rawPath.replace(/^\/+/, '');
    const filePath = path.resolve(root, relativePath);
    const resolvedRelativePath = path.relative(root, filePath);

    if (resolvedRelativePath.startsWith('..') || path.isAbsolute(resolvedRelativePath)) {
      response.writeHead(403);
      response.end('Forbidden');
      return;
    }

    fs.readFile(filePath, (error, data) => {
      if (error) {
        response.writeHead(404);
        response.end('Not found');
        return;
      }

      response.writeHead(200, {
        'Content-Type': contentTypes[path.extname(filePath)] || 'application/octet-stream'
      });
      response.end(data);
    });
  })
  .listen(port, () => {
    console.log(`Static server running at http://localhost:${port}/`);
  });
