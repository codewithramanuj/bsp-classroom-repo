# Setting Up Node.js and React

**Time: about 40 minutes** (the first `npm install` can take a few minutes on a slow network)

## What you will build

- A working Node.js and npm installation on your machine.
- A React project called `hello-react`, created with Vite, that shows a heading in the browser.
- A production build of that project, so you know where the output goes.

---

## The ideas

### Three tools, three jobs

| Tool | What it is | Comparison with the Java side |
|---|---|---|
| **Node.js** | Lets JavaScript run on your computer, outside the browser. The React tools are written in JavaScript, so they need it | Like the JDK |
| **npm** | Installs JavaScript libraries and runs project commands. It comes with Node.js | Like Maven |
| **Vite** | Creates the project, serves it while you work, and builds it for release | Like Spring Initializr plus `mvn spring-boot:run` |

React itself is a library. Vite and npm bring it into your project.

### `package.json` is the project's `pom.xml`

Every npm project has a `package.json` file. It lists the libraries the project needs and the commands you can run (`npm run dev`, `npm run build`). `npm install` reads it and downloads the libraries into a folder called `node_modules`.

```mermaid
flowchart LR
    pkg["package.json"] -->|"npm install"| modules["node_modules"]
    modules --> dev["npm run dev"]
    modules --> build["npm run build"]
    dev --> browser["Browser at localhost:5173"]
    build --> dist["dist folder"]
```

**How to read it:** `npm install` fills `node_modules`. After that, `npm run dev` shows the app in the browser while you work, and `npm run build` produces the `dist` folder that you release.

---

## Build steps

### 1. Install Node.js

1. Open the Node.js website and download the installer marked **LTS** (long-term support).
2. Run the installer and accept the defaults.
3. Close any open terminal and open a new one, so the terminal sees the new program.

Check the install:

```bash
node -v
npm -v
```

**Check:** both commands print a version number, for example `v22.19.0` and `11.11.0`. The Node.js version must be **20.19 or higher** (or 22.12 or higher). If it is lower, install a newer LTS version.

Run one line of JavaScript with Node:

```bash
node -e "console.log('Hello from Node')"
```

**Check:** you see `Hello from Node`.

### 2. Make npm reach the internet

`npm install` downloads libraries from the npm registry, so it needs the same network access as Maven. If your network uses a proxy, set it once. Use the host, port, user name and password from the Day 1 proxy notes.

**A proxy that asks for a login:**

```bash
npm config set proxy http://YOUR_USERNAME:YOUR_PASSWORD@PROXY_HOST:PROXY_PORT
npm config set https-proxy http://YOUR_USERNAME:YOUR_PASSWORD@PROXY_HOST:PROXY_PORT
```

**A proxy with no login:**

```bash
npm config set proxy http://PROXY_HOST:PROXY_PORT
npm config set https-proxy http://PROXY_HOST:PROXY_PORT
```

Both lines start with `http://`, even `https-proxy`. That is the normal form.

**Special characters.** If your user name or password contains a character from this table, write its code instead. Otherwise the address is read wrongly.

| Character | Write | Example |
|---|---|---|
| `@` | `%40` | `p@ss` becomes `p%40ss` |
| `#` | `%23` | `ab#1` becomes `ab%231` |
| `:` | `%3A` | `a:b` becomes `a%3Ab` |
| `/` | `%2F` | `a/b` becomes `a%2Fb` |
| `\` | `%5C` | `DOMAIN\user` becomes `DOMAIN%5Cuser` |

**A mirror of the registry.** If your organization gives you one, set that instead of a proxy:

```bash
npm config set registry MIRROR_URL
```

**Where it is saved.** npm writes these settings, password included, in plain text to a file called `.npmrc` in your home folder. Do not copy that file into any project or commit it.

To see what is set, run:

```bash
npm config list
```

**Check:** the output has lines like `proxy = "http://YOUR_USERNAME:***@PROXY_HOST:PROXY_PORT"`. npm hides the password as `***`. Then run `npm view react version`. You see a version number such as `19.3.1`. If you see an error, go to [Troubleshooting npm](#troubleshooting-npm).

> **Note:** `npm config get proxy` prints `npm error The proxy option is protected` when the address contains a password. Use `npm config list` instead.

**To remove the settings:**

```bash
npm config delete proxy
npm config delete https-proxy
```

**Check:** `npm config get proxy` prints `null`.

### 3. Create the React project

Go to the folder where you keep your front-end work, then run:

```bash
npm create vite@latest hello-react -- --template react
```

If npm asks `Ok to proceed? (y)`, type `y`. If Vite asks further questions, choose **React** and **JavaScript**.

**Check:** Vite prints `Done. Now run:` and a folder `hello-react/` exists.

### 4. Install the libraries

```bash
cd hello-react
npm install
```

**Check:** the last lines say `added … packages` and `found 0 vulnerabilities`. A `node_modules` folder now exists.

### 5. Run the project

```bash
npm run dev
```

**Check:** the terminal prints

```text
VITE v8.3.5  ready in 140 ms

➜  Local:   http://localhost:5173/
```

Open `http://localhost:5173/` in your browser. You see the Vite and React starter page.

Leave the server running. Stop it later with `Ctrl+C`.

### 6. Change the page

Open `src/App.jsx` and replace everything in it with:

```jsx
function App() {
  return <h1>Support Assistant</h1>
}

export default App
```

This is a **component**: a JavaScript function that returns what to show. The HTML-like text inside it is called **JSX**. A file that contains JSX ends in `.jsx`.

Save the file. Do not refresh the browser.

**Check:** the browser updates by itself and shows only the heading `Support Assistant`.

### 7. Look at the project

```text
hello-react/
├── index.html          # the one page; has <div id="root"></div>
├── package.json        # libraries and commands
├── vite.config.js      # Vite settings (leave as it is)
├── public/             # files copied as they are (the icon)
├── src/
│   ├── main.jsx        # puts <App /> into the "root" div
│   ├── App.jsx         # your component
│   ├── App.css
│   └── index.css
└── node_modules/       # downloaded libraries (never edit, never commit)
```

`index.html` has an empty `<div id="root">`. `main.jsx` tells React to fill that div with `App`. Everything you build in the next days goes inside `src/`.

**Check:** you can say which file holds the `root` div, and which file fills it.

### 8. Build for release

Stop the dev server with `Ctrl+C`, then run:

```bash
npm run build
```

**Check:** the output ends with `✓ built in …`, and a `dist/` folder appears with `index.html` and an `assets/` folder inside. This is the folder you will serve on Day 12.

`node_modules/` and `dist/` are already listed in the `.gitignore` that Vite created. Do not commit them.

---

## Troubleshooting npm

**Symptom:** `npm install` or `npm create vite@latest` stops with a message that starts with `npm error`, for example `npm error network request to https://registry.npmjs.org/… failed`.

1. Check the version of Node: `node -v`. It must be 20.19 or higher (or 22.12 or higher). **Check:** the number is high enough.
2. Check that npm can reach the registry: `npm view react version`. **Check:** you see a version number.
3. Check the proxy settings: `npm config list`. **Check:** you see `proxy` and `https-proxy` lines with your `PROXY_HOST` and `PROXY_PORT`. If the lines are missing, go back to step 2.
4. If the proxy needs a login and the user name or password has special characters (such as `@` or `#`), replace each one with its `%` code from the table in step 2. **Check:** `npm view react version` works again.
5. To remove a wrong setting: `npm config delete proxy` and `npm config delete https-proxy`, then set them again. **Check:** `npm config get proxy` shows `null` before you set them again.

| Message | Cause | Fix |
|---|---|---|
| `ETIMEDOUT` or `ECONNREFUSED` | npm cannot reach the registry | Set the proxy (step 2) or the mirror |
| `ENOTFOUND` | The registry name cannot be found | Check the proxy or mirror address for typing mistakes |
| `407 Proxy Authentication Required` | The proxy needs a login | Add `YOUR_USERNAME:YOUR_PASSWORD` to the proxy address |
| `SELF_SIGNED_CERT_IN_CHAIN` or `UNABLE_TO_GET_ISSUER_CERT_LOCALLY` | The network re-signs secure connections | Ask for the organization's certificate file and set it with `npm config set cafile PATH_TO_CERTIFICATE` |
| `'npm' is not recognized` or `command not found: npm` | The terminal was opened before the install | Close it and open a new one |

**Check yourself:**

- [ ] `node -v` shows 20.19 or higher (or 22.12 or higher).
- [ ] `npm view react version` prints a version number.
- [ ] No password is stored in any file inside your project folder.

> **Not verified:** the steps and output above were run on macOS with Node.js 22.19.0, npm 11.11.0, Vite 8.3.5 and React 19.3.1, on a network without a proxy. The `npm config set`, `list`, `get` and `delete` commands for the proxy were run against a temporary settings file, with the address and the `%` codes shown in step 2. The messages in the table come from npm's usual behaviour. Connecting through a real proxy, a mirror, or a certificate file was not tried.

---

## Common mistakes

> **Warning: running a command in the wrong folder.** `npm run dev` prints `npm error enoent Could not read package.json`. You are not inside `hello-react`. Run `cd hello-react` first.

> **Warning: skipping `npm install`.** `npm run dev` prints `vite: command not found` (or `'vite' is not recognized`). The libraries are not downloaded yet. Run `npm install`.

> **Warning: the port is already taken.** If another program uses 5173, Vite starts on 5174 and prints that address. Open the address that the terminal shows, not the one you remember.

> **Warning: JSX in a file that ends in `.js`.** Keep the `.jsx` ending on files that return HTML-like text.

---

## Practice

1. Change the heading in `App.jsx` to `Support Assistant: My Tickets`. **Result:** the browser shows the new text without a refresh.
2. Change `<h1>` to `<h2>` in `App.jsx`. **Result:** the heading becomes smaller.
3. Stop the server, run `npm run build`, and open the `dist/index.html` file. **Result:** you find the `assets/` folder next to it and can say why the page is blank when opened by double-click (the script is requested as `/assets/…`, an address that exists only on a web server; serving `dist/` is Day 12).

---

## Recap

- **Node.js** runs JavaScript outside the browser. **npm** installs libraries and runs commands. **Vite** creates, serves and builds the React project.
- `package.json` lists the libraries and commands. `npm install` downloads them into `node_modules`.
- `npm run dev` serves the app at `http://localhost:5173/` while you work. `npm run build` creates the `dist/` folder.
- A React component is a function in a `.jsx` file that returns what to show. `main.jsx` places it in the `root` div of `index.html`.
