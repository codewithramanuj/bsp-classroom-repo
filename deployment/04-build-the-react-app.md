# Build the React App for the Server

**Time: about 25 minutes**

Back to the [Day 8 index](README.md) · Previous: [Run the API as a service](03-run-as-a-service.md) · Next: [nginx](05-nginx.md)

You need the React project from `frontend-and-react-js/` with the code from [Using .env Files](../frontend-and-react-js/13-environment-variables.md) and [Sending the Token](../frontend-and-react-js/16-sending-the-token.md). In `src/api.js`, the addresses are written like this:

```js
export const API_URL = import.meta.env.VITE_API_URL + '/api/tickets'
export const LOGIN_URL = import.meta.env.VITE_API_URL + '/api/public/login'
```

## What you will build

- A `dist/` folder of plain files (HTML, JavaScript, CSS) that any web server can serve.
- A build that calls the API at the **same address** as the page, so the browser needs no CORS permission.

---

## The ideas

### Why the API address is empty in production

On your machine, the page comes from `http://localhost:5173` and the API from `http://localhost:8080`. Two different addresses. The browser only allows this because `CorsConfig` in the API lists `localhost:5173`.

On the server, nginx serves the page **and** passes `/api/` to the API, so both have one address. The code can call `/api/tickets` with nothing in front of it, and the browser sends the call to the address the page came from. Set `VITE_API_URL` to nothing, and `VITE_API_URL + '/api/tickets'` becomes `/api/tickets`.

### The build writes the value in

As note 13 explained, `npm run build` writes the value into the files in `dist/`. Change `.env.production` and you build again.

---

## Build steps

### 1. Set the production address

In the React project, open `.env.production` and set:

```text
VITE_API_URL=
```

Nothing after the `=`. Keep `VITE_APP_TITLE` if you added it.

### 2. Build

```bash
npm run lint
npm run build
```

**Check:** the build ends with `✓ built in …`, and the folder `dist/` holds `index.html` and an `assets/` folder.

### 3. Look for the address

Run this in the project folder:

```bash
grep -o '.\{20\}/api/tickets.\{5\}' dist/assets/*.js
```

**Check:** you see a short piece of text with `` `/api/tickets` `` in it, and no `localhost` or `http://` in front of it.

### 4. Copy the files to the server

Make the folder on the server and copy the build into it:

```bash
ssh SERVER_USER@SERVER_HOST 'sudo mkdir -p /var/www/helpdesk && sudo chown SERVER_USER /var/www/helpdesk'
scp -r dist/* SERVER_USER@SERVER_HOST:/var/www/helpdesk/
```

**Check:** on the server, `ls /var/www/helpdesk` shows `index.html` and `assets`.

---

## Common mistakes

> **Warning: `VITE_API_URL` still has an address.** The pages work on your machine but fail on the server, and the Console shows ``blocked by CORS policy`` or a call to `localhost:8080`. The address was written into the build. Fix `.env.production` and run `npm run build` again.

> **Warning: copying `dist` itself.** `scp -r dist SERVER_USER@…` puts a folder `dist/` inside the target, and nginx then finds no `index.html`. Copy what is *inside*: `dist/*`.

> **Warning: old files left behind.** The file names in `assets/` change with every build. Old ones stay on the server and do no harm, but they pile up. Step 3 of [note 6](06-checkpoint.md) removes them when you update.

---

## Practice

1. Set `VITE_APP_TITLE=Support Assistant (server)` in `.env.production`, build, and search `dist/assets/*.js` for it. **Result:** the title is in the file.
2. Change `.env.production` back to a full address, build again, and run the `grep` from step 3. **Result:** the address is in the file, which shows why the empty value matters. Put the empty value back and build once more.

---

## Recap

- `npm run build` makes `dist/`: plain files, with no Node.js needed on the server.
- With nginx in front, the page and the API share one address. An empty `VITE_API_URL` makes the calls `/api/...`.
- The value is written in at build time. Change it, then build again.

> **Not verified:** the build was run on macOS with Node.js 22.19.0, Vite 8.3.5 and React 19.3.1, in a copy of `frontend-and-react-js/support-assistant`. In the copy, one line in `TicketsPage.jsx` was changed to `import.meta.env.VITE_API_URL + '/api/tickets'`, to stand in for notes 13 and 16, which the repository code does not contain yet. With `VITE_API_URL=` in `.env.production`, the build succeeded, and `dist/assets/index-….js` held `` `/api/tickets` `` with nothing in front of it. `npm run lint`, `ssh`, `scp`, and the CORS error in the first mistake were not run. The login code from note 16 was not part of the copy.
