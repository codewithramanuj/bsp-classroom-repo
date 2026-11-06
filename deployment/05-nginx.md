# nginx: One Address for Pages and API

**Time: about 50 minutes**

Back to the [Day 8 index](README.md) · Previous: [Build the React app](04-build-the-react-app.md) · Next: [Health check, updates and the checkpoint](06-checkpoint.md)

You need the API running as a service (step 3) and the React files in `/var/www/helpdesk/` (step 4).

## What you will build

- An nginx **server block** that serves the React files and passes `/api/` to the API.
- A page reload that works on every address, such as `/tickets` or `/login`.
- A check that port 8080 is closed to everyone except the server itself.

---

## The ideas

### A reverse proxy

nginx is a web server. It can also act as a **reverse proxy**: it receives a request, passes it to another program, and sends the answer back. The browser only ever talks to nginx. It is like a receptionist who answers the door, and sends a visitor to the right office.

### Two kinds of address

| Request path | What nginx does |
|---|---|
| `/api/tickets`, `/api/public/login` | Passes it to the API on `127.0.0.1:8080` |
| `/assets/index-….js`, `/favicon.svg` | Sends the file from `/var/www/helpdesk/` |
| `/tickets`, `/login` (pages of the React app) | Sends `index.html`, and React Router shows the right page |

The last row matters. `/tickets` is not a file. It is an address that only exists inside the React app. If nginx looked for a file called `tickets`, it would answer `404` on every reload. The line `try_files $uri /index.html` says: send the file if it exists, otherwise send `index.html`.

---

## Build steps

### 1. Check that nginx is installed

On the server:

```bash
nginx -v
```

**Check:** it prints `nginx version: nginx/…`. If not, install it with your server's package manager. If the server cannot reach the internet, point the package manager at `MIRROR_URL`.

### 2. Write the server block

Create `/etc/nginx/conf.d/helpdesk.conf` with `sudo`:

```nginx
server {
    listen 80;
    server_name SERVER_HOST;

    root /var/www/helpdesk;
    index index.html;

    # Calls to the API
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Everything else: a file, or the React app
    location / {
        try_files $uri /index.html;
    }
}
```

`proxy_pass` has no path after `:8080`, so nginx sends `/api/tickets` to the API unchanged.

If the file `/etc/nginx/conf.d/default.conf` or a `default` site already uses port 80 and the same name, disable it, or nginx shows its welcome page.

### 3. Test the file, then load it

```bash
sudo nginx -t
sudo systemctl reload nginx
```

**Check:** `nginx -t` prints `syntax is ok` and `test is successful`. Run it every time before `reload`. A mistake in the file stops nginx from loading it, and `-t` shows the line.

### 4. Open the firewall for port 80

Allow port 80 from your network. The command depends on the firewall on your server (for example `firewall-cmd` or `ufw`). Do **not** open port 8080.

**Check:** from your own machine, `curl -I http://SERVER_HOST/` prints `HTTP/1.1 200 OK`.

### 5. Try the pages and the API

From your machine:

```bash
curl -i http://SERVER_HOST/api/tickets
curl -i http://SERVER_HOST/actuator/health
curl -s -o /dev/null -w '%{http_code}\n' http://SERVER_HOST/tickets
```

**Check:**

- The first answers `401`. The request reached the API, which wants a token.
- The second answers `200` with the React page (`<!doctype html>`), not `{"status":"UP"}`. nginx does not pass `/actuator/` to the API, so the health address is private. You use it on the server in step 1 of the next note.
- The third prints `200`. That is `index.html`, sent for a React route.

### 6. Log in from the browser

Open `http://SERVER_HOST/` in a browser. Log in as `meera` or `karan`.

**Check:** the login works and the tickets load. In the browser's **Network** tab, the calls go to `http://SERVER_HOST/api/…`, with no `localhost`. Reload the page on `/tickets`: it stays on the page and does not show `404`.

### 7. Check that port 8080 is closed from outside

From your machine:

```bash
curl -m 5 http://SERVER_HOST:8080/actuator/health
```

**Check:** it fails with `Connection refused` or a timeout. The API is reachable only through nginx.

---

## HTTPS

Without HTTPS, the password and the token travel as readable text on the network. For a real deployment, ask your security team for a certificate and put it on the server. Then change the server block:

```nginx
server {
    listen 443 ssl;
    server_name SERVER_HOST;
    ssl_certificate CERT_FILE;
    ssl_certificate_key KEY_FILE;
    # ... the same root, index and location blocks as above
}
```

Keep a second `server` block on port 80 that answers `return 301 https://$host$request_uri;`. This step needs a certificate from your organization, so it is not part of the checks.

---

## Common mistakes

> **Warning: `502 Bad Gateway`.** nginx cannot reach the API. Run `sudo systemctl status helpdesk-api` and `curl http://127.0.0.1:8080/actuator/health` on the server. On Red Hat family servers with SELinux, nginx is not allowed to call another program by default: run `sudo setsebool -P httpd_can_network_connect 1`.

> **Warning: `403 Forbidden` on the main page.** nginx cannot read `/var/www/helpdesk/`. Check that `index.html` is there and that the folders can be read by the user nginx runs as (`ls -ld /var/www /var/www/helpdesk`). On SELinux servers, run `sudo restorecon -R /var/www/helpdesk`.

> **Warning: the nginx welcome page.** Another file in `/etc/nginx/` answers for the same name. Remove or rename it, then `nginx -t` and `reload`.

> **Warning: `404` when you reload `/tickets`.** The `try_files $uri /index.html;` line is missing in `location /`.

| Message | Cause | Fix |
|---|---|---|
| `nginx: [emerg] unknown directive` | A typo or a missing `;` | The message names the line. Fix it and run `nginx -t` |
| `bind() to 0.0.0.0:80 failed (98: Address already in use)` | Another program uses port 80 | Stop it, or change `listen` |
| `connect() failed (111: Connection refused) while connecting to upstream` | The API is stopped | Start the service |

**Check yourself:**

- `sudo nginx -t` prints `test is successful`.
- A browser at `http://SERVER_HOST/` shows the login page, and login works.
- A reload on `/tickets` shows the page.
- `curl http://SERVER_HOST:8080/...` from your machine fails.

---

## Practice

1. Stop the API with `sudo systemctl stop helpdesk-api` and open the site. **Result:** the login page still loads, and the login shows an error. Start the API again.
2. Take `try_files` out of the file, reload nginx, and reload the browser on `/tickets`. **Result:** `404`. Put the line back.
3. Add `add_header X-Served-By helpdesk always;` inside `location /api/` and check with `curl -i`. **Result:** the header appears in the answer.

---

## Recap

- nginx serves the React files and passes `/api/` to the API. The browser sees one address, so no CORS is needed.
- `try_files $uri /index.html` makes React routes work after a reload.
- The API listens on `127.0.0.1`, so it is reachable only through nginx, and `/actuator/` is not passed on.
- Always run `nginx -t` before `reload`. Use HTTPS for a real deployment.

> **Not verified:** this note was **not run**. nginx is not installed on the machine used to write it (macOS), and no Linux server was available. The server block, the commands, the expected answers (`401`, `200`, `502`, `403`, and the three messages in the table) and the SELinux advice follow standard nginx behaviour. The `/actuator/health` answer in step 5 (`200` with the React page) is reasoned from the config: that path matches `location /`, and `try_files` sends `index.html` for it. The HTTPS block was not tried, and no certificate was used.
