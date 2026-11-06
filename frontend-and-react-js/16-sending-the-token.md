# Sending the Token with Every Request

**Time: about 45 minutes**

You need the project from [Login: Getting a Token and Keeping It in Session Storage](15-login-and-session-storage.md): you can log in, and the token is in `sessionStorage`. The login API from that note, [support-assistant-security-database](../support-assistant-security-database/README.md), must be running.

At the end of note 15, the tickets page showed `Could not load the tickets. The server answered 401`. The API refuses every request that does not show the token. In this note, every request shows it.

## What you will build

- A function `apiFetch` that you use in place of `fetch`. It adds the token to every request.
- The same function checks every answer: a `401` sends you back to the login page, and a `403` gives a clear message.
- A tickets page and a form page that work for a logged-in user.

---

## The ideas

### The `Authorization` header

A request can carry **headers**: extra lines of information, besides the address and the body. You already send one, `Content-Type`. The token travels in another one:

```text
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

The word `Bearer`, **one space**, and then the token. In `fetch`, headers go in the `headers` option:

```javascript
fetch(url, { headers: { Authorization: 'Bearer ' + token } })
```

### One place for it: a function that every call goes through

Your page makes several calls: the list, the create, the status change. If each one adds the header and checks the answer, the same lines are repeated, and one call gets forgotten. Write them once, in a function. Every call goes through it.

Libraries such as Axios call this idea an **interceptor**: code that runs on every request on its way out, and on every answer on its way back, so that the rest of your code does not repeat it. With `fetch`, you write the same thing by hand, as a function that wraps `fetch`:

```mermaid
sequenceDiagram
    participant T as TicketsPage
    participant W as apiFetch
    participant A as API
    T->>W: apiFetch with the address
    W->>W: add Authorization Bearer and the token
    W->>A: request with the header
    A-->>W: answer
    W->>W: 401 go to login, 403 an error
    W-->>T: the answer
```

**How to read it:** the page calls `apiFetch`, as it called `fetch`. `apiFetch` adds the header before the request leaves, and looks at the status when the answer arrives.

### What the API answers

| Answer | When | Body |
|---|---|---|
| `401` | No token, a wrong token, or a token that has expired | None |
| `403` | A good token, but the user's role may not do this | A short JSON text |
| `200` or `201` | The call is allowed | The ticket |

The roles decide who may do what:

| Call | Who may |
|---|---|
| Read the tickets (`GET /api/tickets`) | Any logged-in user |
| Create a ticket (`POST /api/tickets`) | `EMPLOYEE`, such as `meera` |
| Change a status (`PATCH /api/tickets/1/status`) | `AGENT`, such as `karan` |

> **Note:** this API does not store tickets. The list is always the same two examples (`VPN drops every hour` and `Printer is jammed`), and they have no priority, so the `Priority:` line on each card is empty. A create or a status change is answered, but does not change the list. The point of this note is the token and the roles.

### Why the browser asks first

A page on `http://localhost:5173` sends a request to `http://localhost:8080`. When the request has an `Authorization` header, the browser first sends a question without the token: "may this page send that header?" (an `OPTIONS` request, as in note 11). The API answers from its `CorsConfig`, which already lists `Authorization`. You do not write code for this.

---

## Build steps

Work in `hello-react` with `npm run dev` running and the login API running. Log in first, as in note 15, so that the token is in `sessionStorage`.

### 1. Send the header by hand

Open the Console (`F12`) on your React page and paste:

```javascript
const token = sessionStorage.getItem('token')
const without = await fetch('http://localhost:8080/api/tickets')
without.status
```

**Check:** the Console shows `401`. Now with the token:

```javascript
const withToken = await fetch('http://localhost:8080/api/tickets', {
  headers: { Authorization: 'Bearer ' + token },
})
withToken.status
await withToken.json()
```

**Check:** the Console shows `200`, and then a list of two objects, with the titles `VPN drops every hour` and `Printer is jammed`.

### 2. Write `apiFetch` and use it

Replace `src/api.js` with:

```javascript
import { getToken } from './session.js'

export const API_URL = import.meta.env.VITE_API_URL + '/api/tickets'
export const LOGIN_URL = import.meta.env.VITE_API_URL + '/api/public/login'

// Use this in place of fetch for every call that needs a login.
export async function apiFetch(url, options = {}) {
  const headers = options.headers || {}
  const token = getToken()
  if (token) {
    headers.Authorization = 'Bearer ' + token
  }
  options.headers = headers

  return fetch(url, options)
}
```

Three things to notice:

- `options = {}` means: if the caller passes no options, use an empty object. A plain `GET` call passes none.
- The function takes the headers the caller gave (such as `Content-Type`), adds `Authorization` to them, and calls `fetch`.
- `API_URL` now ends in `/api/tickets`. This API has no `/api/v1/tickets`.

Now use it. In `src/pages/TicketsPage.jsx`:

1. Change the import to `import { API_URL, apiFetch } from '../api.js'`.
2. Replace each `await fetch(` with `await apiFetch(`. There are three: in `fetchTickets`, in `handleCreate` and in `handleStatusChange`.

Leave `LoginPage.jsx` as it is. The login request is the one request that has no token yet.

**Check:** log in as `meera`. The tickets page shows `2 tickets` with the cards `VPN drops every hour` and `Printer is jammed`. In Chrome, open the **Network** tab, reload, and click the request `tickets`: under **Request Headers** you see `Authorization: Bearer eyJ…`. Before it, you see a request of the same name with the method `OPTIONS`: that is the browser's question.

### 3. Check the answers

Add the answer checks to `apiFetch`. Replace the last line, `return fetch(url, options)`, and the import, so that `src/api.js` becomes:

```javascript
import { getToken, clearSession } from './session.js'

export const API_URL = import.meta.env.VITE_API_URL + '/api/tickets'
export const LOGIN_URL = import.meta.env.VITE_API_URL + '/api/public/login'

// Use this in place of fetch for every call that needs a login.
export async function apiFetch(url, options = {}) {
  const headers = options.headers || {}
  const token = getToken()
  if (token) {
    headers.Authorization = 'Bearer ' + token
  }
  options.headers = headers

  const response = await fetch(url, options)

  if (response.status === 401) {
    clearSession()
    window.location.assign('/login')
    throw new Error('Your session has ended. Please log in again.')
  }
  if (response.status === 403) {
    throw new Error('Your role is not allowed to do this.')
  }
  return response
}
```

- On `401`, the session is cleared, and `window.location.assign('/login')` opens the login page. This loads the page again on purpose: React starts clean, and the navigation bar shows `Login`. The `throw` stops the code that called `apiFetch`, so that it does not go on with a failed answer.
- On `403`, the function throws an error with a clear message. The pages already catch errors and show them, so they need no change.
- Every other answer goes back to the caller as before.

### 4. Try each user, and a bad token

**As `meera` (an `EMPLOYEE`).**

- Open **New Ticket**, type a title such as `Token test`, and click **Create ticket**. **Check:** you return to the tickets page, with no error.
- On the tickets page, choose `IN_PROGRESS` in the status list of a card. **Check:** the page shows `Could not change the status. Your role is not allowed to do this.`, and the list goes back to `OPEN`.

**As `karan` (an `AGENT`).** Click **Logout**, then log in as `karan` with the password `karan123`.

- Choose `IN_PROGRESS` in the status list. **Check:** no error appears. The list goes back to `OPEN`, because this API does not store the change.
- Open **New Ticket**, type a title, and click **Create ticket**. **Check:** the form shows `Could not create the ticket. Your role is not allowed to do this.`

**A bad token.** In the Console, type `sessionStorage.setItem('token', 'abc')` and press Enter. Then reload the page.

**Check:** the browser moves to the login page, the navigation bar shows `Login`, and **Session Storage** (the **Application** tab) is empty. The same happens when a token has expired: the API answers `401`.

### 5. Run the checks

```bash
npm run lint
npm run build
```

**Check:** `npm run lint` prints nothing but its own header, and `npm run build` ends with `✓ built in …`.

---

## Common mistakes

> **Warning: `'Bearer' + token` with no space.** You log in, and at once you are on the login page again, with an empty session. The API does not accept a header that does not start with `Bearer ` (with the space), so the tickets request gets `401`. Write `'Bearer ' + token`.

> **Warning: one `fetch` left in place of `apiFetch`.** That call has no header, so the API answers `401`. In the form page it shows `Could not create the ticket. Failed to execute 'json' on 'Response': Unexpected end of JSON input`. Search your files for `fetch(` and check that each one that needs a login is `apiFetch(`.

> **Warning: `apiFetch` in `LoginPage.jsx`.** A wrong password gets `401`, so `apiFetch` loads the login page again, and the message `Wrong user name or password.` never appears. The login request must use plain `fetch`.

> **Warning: `API_URL` still ends in `/api/v1/tickets`.** The page shows `Could not load the tickets. The server answered 404`. This API serves `/api/tickets`.

> **Warning: `Authorization` missing from `allowedHeaders` in `CorsConfig`.** The tickets page shows `Could not load the tickets. Failed to fetch`, and the Console shows ``Access to fetch at 'http://localhost:8080/api/tickets' from origin 'http://localhost:5173' has been blocked by CORS policy: Response to preflight request doesn't pass access control check…``. Add `"Authorization"` to `allowedHeaders` in the API, and restart it.

> **Warning: a wrong role sends you to the login page.** As `meera`, you choose a status, and you land on the login page with an empty session, in place of seeing `Your role is not allowed to do this.` The API answered `401` where it should answer `403`. On a real server the `403` is sent through the framework's internal error page, and that page needs a login itself. In `SecurityConfig` of the API, add `.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()` as the first line inside `.authorizeHttpRequests(rules -> rules`, and the import `jakarta.servlet.DispatcherType`. Then restart the API.

---

## Practice

1. Add `console.log('Calling', url)` as the first line of `apiFetch`. **Result:** the Console shows one line for each call the page makes.
2. In `Navbar.jsx`, show the `New Ticket` link only when someone is logged in: `{username && <NavLink to="/tickets/new">New Ticket</NavLink>}`. **Result:** after you log out, the link is gone.
3. Log in as each user and try the list, a create, and a status change. **Result:** what works matches the table in this note.

---

## Recap

- The token travels in the header `Authorization: Bearer TOKEN`: the word `Bearer`, one space, then the token.
- Write the header once, in `apiFetch`, and use it in place of `fetch` for every call that needs a login. This is the idea of an interceptor, written by hand.
- `apiFetch` also checks the answer. A `401` clears the session and opens the login page. A `403` becomes an error with a clear message, which the pages already show.
- The login request uses plain `fetch`, because it has no token yet.
- The API decides who may do what, by role. The page shows the API's answer.

**Not covered here:** hiding buttons that the role may not use. The role is not in the token. It is available from `GET /api/me`, which the login API offers.

> **Not verified:** `support-assistant-security-database` was not run on Oracle. A copy in a temporary folder ran on an in-memory H2 database in Oracle mode, with the two SQL scripts, a made-up `JWT_SECRET`, and Spring Boot 4.1.1. **That copy answered `401` for a wrong role, not the `403` that its README describes, until the `DispatcherType.ERROR` line from the last common mistake was added to its `SecurityConfig`.** With the line, it answered `403` with a JSON body and the CORS header. The repository's own `SecurityConfig` does not have the line. The React steps were run on macOS with Node.js 22.19.0, Vite 8.3.5, React 19.3.1 and `react-router` 7.18.5, in headless Chrome, next to the code from note 15, with the line added. A script logged in, and the requests were recorded by a small forwarding server: the preflight `OPTIONS` asked for `authorization`, and every call after the login carried `Authorization: Bearer eyJ…`. As `meera`, the list loaded, a create returned `201`, and a status change gave `Your role is not allowed to do this.`. As `karan`, a status change gave `200`, and a create gave `Could not create the ticket. Your role is not allowed to do this.`. A bad token in the storage ended on the login page with an empty session. The six common mistakes were each tried, and the quoted messages are what appeared. The Console lines in step 1 were not typed by hand: the same calls were made by `curl` and by script. The Network tab and the Application tab were not looked at by eye. The practice tasks were not run. A real token that expires (after 30 minutes) was not waited for. A bad token was used in its place. `support-assistant-plsql` was not run, because it needs Oracle. The note was first run with the routes `/` and `/new` and a separate `NewTicketPage`. After note 14 changed to child routes in `TicketsPage`, the final files were run again with `fetch` replaced in the three places of `TicketsPage`: `meera` got `Your role is not allowed to do this.` for a status change, and created a ticket and returned to `/tickets`. `karan` got a status change without a message, and `Could not create the ticket. Your role is not allowed to do this.` for a create. A bad token again ended on `/login` with an empty session. The mistake with one `fetch` left (in `handleCreate`) gave `Could not create the ticket. Failed to execute 'json' on 'Response': Unexpected end of JSON input`. The other five mistakes were not tried again after the change, and neither was the Network tab. Only Chrome was used.
