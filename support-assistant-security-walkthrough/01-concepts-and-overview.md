# Step 1 — Concepts and overview

> [Back to index](README.md) · Previous: none · Next: [Project and the default lock](02-project-and-the-default-lock.md)

## Goal

Know the words that every later step uses, and see the three Spring Security parts that you will write.

## Why this matters

Spring Security is large, and most of it is not needed on the first day. If you know which three parts do the work, the code in the next steps is short and has a clear place for each line.

## 1.1 Two questions

| Word | The question | Answered with |
|---|---|---|
| **Authentication** | Who are you? | A user name and a password |
| **Authorization** | May you do this? | A role, such as `EMPLOYEE` or `AGENT` |

When the answer to the first question is "I do not know", the application replies **`401 Unauthorized`**. When it knows who you are, and you may not do this, it replies **`403 Forbidden`**.

Think of a building. At the gate, the guard checks who you are (authentication). At the door of the server room, the guard checks that your badge opens it (authorization).

## 1.2 HTTP Basic

**HTTP Basic** is the simplest way to log in to an API. The client writes `user:password` in the `Authorization` header of **every** request, in Base64:

```text
Authorization: Basic bWVlcmE6bWVlcmExMjM=
```

`bWVlcmE6bWVlcmExMjM=` is `meera:meera123` in Base64. Base64 is a way to write text, not a lock. Anyone who sees the header can read the password. Outside the classroom, Basic must go over HTTPS.

`curl -u meera:meera123` writes this header for you.

## 1.3 The three parts

| Part | Its job | You write it in |
|---|---|---|
| `SecurityFilterChain` | The rules: which URL is open, which needs a login, which needs a role | Step 3 and step 5 |
| `UserDetailsService` | Finds a user by name, and returns the stored password and roles | Step 4 |
| `PasswordEncoder` | Hashes a password, and checks that a typed password matches a stored hash | Step 4 |

A **hash** is a one-way scramble of the password. The application stores the hash, never the password. **BCrypt** is a hash made for passwords.

```mermaid
sequenceDiagram
    participant C as Client
    participant F as Security filter
    participant U as UserDetailsService
    participant P as PasswordEncoder
    participant K as TicketController
    C->>F: GET /api/tickets with the Basic header
    F->>U: find the user meera
    U-->>F: the user, the stored hash, the role
    F->>P: does meera123 match the hash?
    P-->>F: yes
    F->>K: allowed, call the controller
    K-->>C: 200 and the tickets
```

**How to read it:** the filter runs **before** the controller. It takes the user name and password from the header, asks the `UserDetailsService` for the user, and asks the `PasswordEncoder` to compare. If the answer is yes and the role fits the URL, the controller runs. If not, the filter answers, and the controller is never called.

## 1.4 The plan

| Step | What the application can do after it |
|---|---|
| 2 | Spring Security locks every URL by itself, with a password it prints in the log |
| 3 | Your rules: `/api/public/ping` is open, everything else needs a login |
| 4 | Your own users, `meera` and `karan`, replace the generated one |
| 5 | Roles decide who may `POST` and who may `PATCH` |
| 6 | Seven tests prove every rule |

The tickets in this project are fixed examples. The lesson is **who may call** each URL, not how tickets are stored.

## Check yourself

- Which answer means "I do not know who you are": `401` or `403`?
- Where does the password travel in HTTP Basic, and is it hidden?
- Which of the three parts compares a typed password with a stored one?

Next: **[Project and the default lock](02-project-and-the-default-lock.md)**.
