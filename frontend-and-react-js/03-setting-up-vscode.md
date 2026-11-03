# Setting Up VS Code for React

**Time: about 30 minutes**

You need the `hello-react` project from [Setting Up Node.js and React](02-setting-up-react.md).

## What you will build

- VS Code set up to edit a React project, with a terminal inside it.
- The **ES7+ React/Redux/React-Native snippets** extension, so that typing `rfce` writes a whole component.
- Emmet working in `.jsx` files, so that typing `ul>li*3` writes three list items.

---

## The ideas

### Emmet and snippets are two different things

Both save typing. You type a short word, press `Tab`, and VS Code writes more text.

| | Emmet | Snippets |
|---|---|---|
| What it is | A short way to write HTML | Ready-made blocks of code |
| Where it comes from | Built into VS Code | An extension you install |
| Example | `ul>li*3` | `rfce` |
| What it writes | Tags | A whole component file |

`rfce` and `rafce` are **snippets**, not Emmet. If they do nothing, the extension is not installed (step 2).

### The snippets you will use

| Type | It writes |
|---|---|
| `rfce` | A component written as a **function**, with `export default` at the end |
| `rafce` | The same component written as an **arrow function** (`const Name = () => { … }`), with `export default` at the end |

Both give the same result in the browser. The notes in this folder write components as `function TicketCard() { … }`, so `rfce` matches them. `rafce` is equally valid.

The snippets also write the name of the file as the name of the component. A file called `TicketCard.jsx` gives a component called `TicketCard`.

---

## Build steps

### 1. Install VS Code and open the project

1. Download VS Code from the Visual Studio Code website and run the installer.
2. Start VS Code. Choose **File → Open Folder…** and select the **`hello-react`** folder itself (the one that contains `package.json`), not the folder above it.
3. Open a terminal inside VS Code: **Terminal → New Terminal** (shortcut `` Ctrl+` ``).

**Check:** the terminal prompt shows `hello-react`. Run `node -v` in it and see a version number.

### 2. Install the snippets extension

1. Open the Extensions panel: click the square icon on the left bar, or press `Ctrl+Shift+X` (`Cmd+Shift+X` on Mac).
2. Search for `ES7+ React/Redux/React-Native snippets`. The publisher is **dsznajder**.
3. Click **Install**.

**Check:** the extension shows a **Disable** button instead of **Install**.

If the Extensions list stays empty, or shows an error, go to [Troubleshooting the Extensions panel](#troubleshooting-the-extensions-panel).

### 3. Use a snippet

1. In the Explorer panel, right-click the `src/components` folder and choose **New File**. Name it `Navbar.jsx`. (Create the `components` folder first if it does not exist.)
2. The new file is empty. Type `rfce` and wait for a small list to appear.
3. Choose the entry named `rfce` and press `Tab` (or `Enter`).

**Check:** the file now holds a complete component, similar to this:

```jsx
import React from 'react'

function Navbar() {
  return (
    <div>Navbar</div>
  )
}

export default Navbar
```

The first line, `import React from 'react'`, is not needed in this project. You can delete it, and the project still builds. Edit the text between the tags to change what the component shows.

Now delete the whole file content and try `rafce` the same way.

**Check:** you get the same component, written as `const Navbar = () => { … }`.

### 4. Use Emmet

Inside the `return ( … )` of a `.jsx` file, replace `<div>Navbar</div>` with the text `ul>li*3`, then press `Tab`.

**Check:** the text becomes a list with three items:

```jsx
<ul>
  <li></li>
  <li></li>
  <li></li>
</ul>
```

Learn four patterns first:

| Type | It writes |
|---|---|
| `h1` | `<h1></h1>` |
| `div>h2+p` | a `div` that contains an `h2` and a `p` |
| `li*3` | three `li` tags |
| `.card` | `<div className="card"></div>` |

`>` means "inside", `+` means "next to", and `*3` means "three times". In a `.jsx` file the last one writes `className`, not `class`, because `class` is a reserved word in JavaScript.

### 5. Change two settings

Open the settings file: press `Ctrl+Shift+P` (`Cmd+Shift+P` on Mac), type `Open User Settings (JSON)`, and press `Enter`. Add these lines inside the braces:

```json
"emmet.triggerExpansionOnTab": true,
"files.autoSave": "afterDelay"
```

The first makes `Tab` expand Emmet text. The second saves a file a moment after you stop typing, so the browser updates without you pressing `Ctrl+S`.

**Check:** type `div>p` in a `.jsx` file and press `Tab`. It expands. Type a letter in any file, wait a second, and the dot next to the file name disappears.

> **Note:** if Emmet does nothing in files that end in `.js`, add `"emmet.includeLanguages": { "javascript": "javascriptreact" }` to the same file. Files that end in `.jsx` do not need it.

### 6. Know five shortcuts

| Job | Windows and Linux | Mac |
|---|---|---|
| Open a file by name | `Ctrl+P` | `Cmd+P` |
| Open the command list | `Ctrl+Shift+P` | `Cmd+Shift+P` |
| Open the Extensions panel | `Ctrl+Shift+X` | `Cmd+Shift+X` |
| Show or hide the terminal | `` Ctrl+` `` | `` Ctrl+` `` |
| Show or hide the side panel | `Ctrl+B` | `Cmd+B` |

**Check:** you open `App.jsx` with `Ctrl+P` without touching the mouse.

---

## Troubleshooting the Extensions panel

**Symptom:** the Extensions panel shows no results when you search, or an error such as `Error while fetching extensions`.

1. Check that the machine reaches the internet: open any public website in a browser. **Check:** it loads.
2. Tell VS Code about the proxy. Open the settings file (step 5) and add:

   ```json
   "http.proxy": "http://YOUR_USERNAME:YOUR_PASSWORD@PROXY_HOST:PROXY_PORT"
   ```

   Use the values from the Day 1 proxy notes. If the proxy needs no login, use `http://PROXY_HOST:PROXY_PORT`. Replace special characters in the user name or password with their `%` codes (for example `@` becomes `%40`), as in the npm note. The password is stored as plain text in this file, so do not copy the file into a project.
   **Check:** close VS Code, open it again, and search again. Results appear.
3. If the Marketplace is still blocked, install from a file. Get the `.vsix` file of the extension (from the extension's page on the Marketplace website, on a machine that can reach it, or from a shared folder your organization provides). In VS Code, press `Ctrl+Shift+P`, run **Extensions: Install from VSIX…**, and choose the file. **Check:** the extension shows **Disable** in the Extensions panel.

**Check yourself:**

- [ ] The Extensions panel finds `ES7+ React/Redux/React-Native snippets`, or you installed it from a `.vsix` file.
- [ ] No password is stored in any file inside your project folder.

---

## Common mistakes

> **Warning: the snippet list does not appear.** Look at the bottom-right corner of VS Code. It must say **JavaScript React**. If it says **Plain Text**, the file name has no `.jsx` ending. Rename the file.

> **Warning: `Tab` writes spaces instead of expanding.** Add `"emmet.triggerExpansionOnTab": true` (step 5), or press `Enter` on the entry in the small list.

> **Warning: a snippet typed into a file that already has code.** The snippet is inserted at the cursor, so you end up with two components in one file. Use the snippet in an empty file.

> **Warning: the wrong folder is open.** `npm run dev` prints `Could not read package.json`. You opened the folder above `hello-react`. Use **File → Open Folder…** and open `hello-react` itself.

> **Warning: `class` instead of `className`.** In a `.jsx` file, write `className="card"`. Emmet's `.card` does it for you.

---

## Practice

1. Create `src/components/TicketList.jsx` with `rfce`. **Result:** a component named `TicketList` is in the file.
2. Inside it, use one Emmet line to write a heading and a list of four items. **Result:** the browser shows them after you use `<TicketList />` in `App.jsx`.
3. Create `src/components/TicketForm.jsx` with `rafce`. Change the text it shows. **Result:** the browser shows your text.

---

## Recap

- **Emmet** is built in and writes tags. **Snippets** come from an extension and write blocks of code.
- `rfce` writes a function component. `rafce` writes an arrow-function component. Both end with `export default`.
- Open the `hello-react` folder itself in VS Code, so the terminal and the file list line up.
- If the Extensions panel does not work, set `http.proxy` or install from a `.vsix` file.

> **Not verified:** the generated code for `rfce` and `rafce`, the Emmet output in `.jsx` files, the settings names, and the Extensions panel error text are written from the documented behaviour of VS Code and of the snippets extension. They were not run for this note. Run each Check once and correct the note where the output differs. The proxy and `.vsix` steps were not tried.
