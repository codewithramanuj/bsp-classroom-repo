# Using .env Files: Reading the API Address

**Time: about 30 minutes**

You need the project from [Changing Data with a PATCH Request](12-changing-data-with-patch.md). `TicketsPage` has the line `const API_URL = 'http://localhost:8080/api/v1/tickets'`. The address is written in the code. On a server it will be a different address, and you do not want to edit code each time.

The Support Assistant API must be running, as in step 1 of note 09.

## What you will build

- A `.env` file that holds the API address and the app title.
- Code that reads these values, with no address written in it.
- A second file, `.env.production`, with different values for the finished build.

---

## The ideas

### A `.env` file

A `.env` file is a plain text file with one `NAME=value` on each line. It sits in the project folder, next to `package.json`. Your code asks for a value by its name, and the value comes from the file. To change the value, you edit the file, and the code stays the same.

### Reading a value

Vite reads the file. Your code gets the values from `import.meta.env`:

```jsx
const address = import.meta.env.VITE_API_URL
```

Two rules:

- The name must **start with `VITE_`**. Vite hides every other name from your code.
- Write the line with no quotes, no spaces around `=`, and no `/` at the end of an address: `VITE_API_URL=http://localhost:8080`.

> **Warning: nothing in a `VITE_` variable is secret.** The value ends up in the JavaScript that every visitor downloads. Use these variables for things like the API address. Never put a password or a key in one.

### Which file is used when

| Command | Files it reads |
|---|---|
| `npm run dev` | `.env`, then `.env.development` |
| `npm run build` | `.env`, then `.env.production` |

If a name is in both files, the second file wins. In this note you use `.env` while you work, and `.env.production` for the build.

---

## Build steps

Work in `hello-react` with the dev server running (`npm run dev`) and the API running.

### 1. Create the `.env` file

Create `.env` in the `hello-react` folder, next to `package.json`:

```text
VITE_API_URL=http://localhost:8080
```

### 2. Read the address in `TicketsPage`

In `src/pages/TicketsPage.jsx`, replace the line `const API_URL = 'http://localhost:8080/api/v1/tickets'` with:

```jsx
const API_URL = import.meta.env.VITE_API_URL + '/api/v1/tickets'
```

Reload the page.

**Check:** the page shows the tickets, as before. The address now comes from `.env`. To see it, change the file to `VITE_API_URL=http://localhost:8090` (nothing runs there), save, and reload: the page shows `Could not load the tickets. Failed to fetch`. Vite restarts itself when you save a `.env` file, so you only reload the page. Change the value back and reload: the tickets return.

### 3. Add a second value

Add a line to `.env`:

```text
VITE_APP_TITLE=Support Assistant (dev)
```

In `src/components/Navbar.jsx`, replace `<strong>Support Assistant</strong>` with:

```jsx
<strong>{import.meta.env.VITE_APP_TITLE}</strong>
```

**Check:** the navbar shows `Support Assistant (dev)`.

### 4. Different values for the build

Create `.env.production` in the same folder. The address is made up, to show where it ends up:

```text
VITE_API_URL=https://support.example.com
VITE_APP_TITLE=Support Assistant
```

Stop the dev server, or open a second terminal, and run:

```bash
npm run build
```

Open the folder `dist/assets/`. It holds a `.js` file with a name like `index-AbC123.js`. Open it in VS Code, press `Ctrl+F` (`Cmd+F` on a Mac), and search for `api/v1`.

**Check:** you find `https://support.example.com/api/v1/tickets`. The address from `.env` is not there, because the build reads `.env.production` after `.env`.

The value is **written into the file** when you build. If you change `.env.production` now, the file in `dist/` does not change. Run `npm run build` again to get the new value.

### 5. Put the real address in `.env.production`

Change `.env.production` to the address you want for the build. For a try on your own machine, use `VITE_API_URL=http://localhost:8080`. Then run:

```bash
npm run lint
npm run build
```

**Check:** `npm run lint` prints nothing but its own header, and `npm run build` ends with `✓ built in …`.

---

## Common mistakes

> **Warning: a name without `VITE_`.** With `API_URL=http://localhost:8080` in the file, the code reads `undefined`. The address becomes `undefined/api/v1/tickets`, and the page shows ``Could not load the tickets. Unexpected token '<', "<!doctype "... is not valid JSON``. Rename the variable to `VITE_API_URL`.

> **Warning: a `/` at the end of the address.** With `VITE_API_URL=http://localhost:8080/`, the page shows `Failed to fetch`, and the Console shows `blocked by CORS policy`. The address has a double slash, `//api/v1/tickets`, and the API answers `404`. Remove the last `/`.

> **Warning: changing `.env.production` and expecting a built site to change.** The value is inside the `dist/` files. Run `npm run build` again.

---

## Practice

1. Add `VITE_SUPPORT_EMAIL=help@example.com` to `.env`, and show it in `Footer.jsx` with `{import.meta.env.VITE_SUPPORT_EMAIL}`. **Result:** the footer shows the email address.
2. Add the same name to `.env.production` with a different email, run `npm run build`, and search the `.js` file in `dist/assets/` for it. **Result:** the file contains the production email, and not the one from `.env`.

---

## Recap

- A `.env` file holds `NAME=value` lines. Use it for things that change between machines, such as the API address.
- Start each name with `VITE_`, and read it with `import.meta.env.VITE_NAME`.
- `npm run dev` reads `.env` and `.env.development`. `npm run build` reads `.env` and `.env.production`.
- The build writes the value into the `dist/` files. To change it, edit the file and build again.
- Never put a secret in a `VITE_` variable.

> **Not verified:** the steps were run on macOS with Node.js 22.19.0, Vite 8.3.5 and React 19.3.1, next to the code from note 12, with the API from `support-assistant-jpa-h2` on port 8080 and headless Chrome. With only a `.env` file, the page showed the six tickets and the navbar title `Support Assistant (dev)`. The build with `.env.production` put `https://support.example.com/api/v1/tickets` and the title `Support Assistant` in `dist/`, and the address from `.env` was not there. I searched `dist/` with `grep`, not with `Ctrl+F` in VS Code. The three mistakes were each tried, and the quoted messages are what appeared. The restart message and the reload were seen with `.env.development`, and the restart is the same for `.env`. Practice task 1 was not run (the same step as the navbar title), and task 2 was run in a form close to the text. Windows was not tried. `support-assistant-plsql` was not run, because it needs Oracle. Only Chrome was used.
