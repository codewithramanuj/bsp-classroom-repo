# Walkthrough: support-assistant-security

Build a small Spring Boot ticket API that is locked with **Spring Security**. You start from an empty project and add one thing in each step: the default lock, your own URL rules, in-memory users, roles, and tests. No database is needed.

**What you are building:** [support-assistant-security](../support-assistant-security/), described in its [README](../support-assistant-security/README.md). The finished code is in that folder. Use it to compare your work, not to copy.

## What you will build

Two users, five URLs, and seven tests:

| User | Password | Role |
|---|---|---|
| `meera` | `meera123` | `EMPLOYEE` |
| `karan` | `karan123` | `AGENT` |

| Method | URL | Who may call it |
|---|---|---|
| `GET` | `/api/public/ping` | Everyone, no login |
| `GET` | `/api/me` | Any logged-in user |
| `GET` | `/api/tickets` | Any logged-in user |
| `POST` | `/api/tickets` | `EMPLOYEE` only |
| `PATCH` | `/api/tickets/1/status` | `AGENT` only |

- The users are fixed users in memory, made when the application starts.
- The client logs in with **HTTP Basic**: a user name and a password in the header of every request.
- You test every rule with MockMvc. The tests need no server and no database.

## Prerequisites

- Java 21 and Maven. In IntelliJ, set **Maven home path** to **Bundled (Maven 3)**.
- `curl`, to call the API from a terminal.
- A Spring Boot application with a controller, as in [Day 2](../support-assistant/README.md).

Time: about 2 hours.

## Steps

| Step | File | What you add | Est. time |
|---|---|---|---|
| 1 | [01-concepts-and-overview.md](01-concepts-and-overview.md) | The words used in every step: authentication, authorization, Basic, hash, role | 10 min |
| 2 | [02-project-and-the-default-lock.md](02-project-and-the-default-lock.md) | The project, one public URL, and the lock that Spring Security adds by itself | 15 min |
| 3 | [03-your-own-url-rules.md](03-your-own-url-rules.md) | `SecurityConfig`: which URL is open, and which needs a login | 15 min |
| 4 | [04-in-memory-users.md](04-in-memory-users.md) | `UsersConfig`: `meera` and `karan`, BCrypt hashes, and `GET /api/me` | 20 min |
| 5 | [05-roles-on-urls.md](05-roles-on-urls.md) | The ticket URLs, a rule per role, `403`, and the CSRF switch | 25 min |
| 6 | [06-testing-the-rules.md](06-testing-the-rules.md) | `SecurityTests`: seven MockMvc tests. Break a rule to see one fail | 25 min |
| 7 | [07-recap-and-exercises.md](07-recap-and-exercises.md) | Quick reference, gotchas, exercises | 10 min |

## How each step is built

- **Goal** and **Why this matters** say what the step adds, and why.
- The numbered parts are the build steps. Each one names the file and shows the code to type. Most end with a **Check** line that you run before you go on.
- **Common mistakes** lists what goes wrong in this step, as the tools print it.
- Snippets are short. The complete classes are in [support-assistant-security](../support-assistant-security/). Each snippet says which finished file it belongs to.
- The comments in the finished files are for the reader. You do not have to type them.
- IntelliJ adds `import` lines when you press `Alt+Enter` on a red name. Where a name has more than one candidate, the step lists the right one.

## How it was checked

The project was built step by step in a clean copy, using the code in these steps. Each **Check** was run with `curl` against the running application, and the output shown is what the tools printed. The test counts (4, then 7) and the failure messages in the steps are the real output. The final files are identical to the finished project.

Not checked: creating the project with Spring Initializr (the steps give the `pom.xml` to use instead), Spring Boot 3.5, and a browser login box. It was built and tested on Spring Boot 4.1.1 only.

Start with **[Step 1 — Concepts and overview](01-concepts-and-overview.md)**.
