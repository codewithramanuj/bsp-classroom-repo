# support-assistant-security

A small ticket API that shows **Spring Security** in its simplest form:

- **In-memory users**: two users made in code when the application starts.
- **HTTP Basic authentication**: the client sends a user name and a password with every request.
- **Roles**: `EMPLOYEE` and `AGENT`, and which URL each role may call.

It needs no database. The users stay in memory, and the tests run on your machine in a few seconds.

The next project, [support-assistant-security-database](../support-assistant-security-database/), keeps everything here and moves the users into an Oracle table.

**Time:** about 40 minutes.

## The ideas

**Authentication** answers "who are you?". **Authorization** answers "may you do this?". Spring Security does both, in a filter that runs before your controller. A request that fails never reaches the controller.

**HTTP Basic** is the simplest way for a client to prove who it is. The client puts `user:password` in the `Authorization` header, written in Base64, on **every** request:

```text
Authorization: Basic bWVlcmE6bWVlcmExMjM=
```

`bWVlcmE6bWVlcmExMjM=` is `meera:meera123` in Base64. Base64 is a way to write text, not a lock: anyone can read it. Outside the classroom, Basic authentication must go over HTTPS.

Three Spring Security parts work together:

| Part | Job | In this project |
|---|---|---|
| `SecurityFilterChain` | The rules: which URL needs which login or role | `SecurityConfig` |
| `UserDetailsService` | Finds a user by name and returns the stored password and roles | `UsersConfig` (in memory) |
| `PasswordEncoder` | Hashes a password, and checks that a typed password matches a stored hash | `SecurityConfig` (BCrypt) |

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

**How to read it:** the filter takes the user name and password from the header. It asks the `UserDetailsService` for the user, and asks the `PasswordEncoder` to compare. If both pass and the role is allowed, the controller runs. If not, the filter answers and the controller is never called.

A **password hash** is a one-way scramble of the password. The application stores the hash, not the password. **BCrypt** is a hash made for passwords. It gives a different hash each time you hash the same password, and the encoder still knows how to check a match.

## The users and the rules

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

The tickets in the answers are fixed examples. This project is about **who may call** each URL, not about storing tickets.

| Answer | Meaning |
|---|---|
| `401 Unauthorized` | The application does not know who you are: no login, or a wrong password |
| `403 Forbidden` | The application knows who you are, and your role is not allowed here |

## Versions

| Item | Version |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.1 (brings Spring Security 7) |
| Test tools | JUnit 5, MockMvc, `spring-boot-starter-security-test` |

## Build, test, and run

```bash
mvn clean verify
java -jar target/support-assistant-security-0.0.1-SNAPSHOT.jar
```

`mvn clean verify` runs seven tests. You should see `Tests run: 7, Failures: 0, Errors: 0`.

## The code

### 1. The rules: `SecurityConfig`

[SecurityConfig](src/main/java/com/bsp/supportassistant/config/SecurityConfig.java)

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .authorizeHttpRequests(rules -> rules
                    .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/tickets").hasRole("EMPLOYEE")
                    .requestMatchers(HttpMethod.PATCH, "/api/tickets/*/status").hasRole("AGENT")
                    .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable());
    return http.build();
}
```

| Line | What it does |
|---|---|
| `permitAll()` | No login needed |
| `hasRole("EMPLOYEE")` | Login needed, and the user must have the role `EMPLOYEE` |
| `anyRequest().authenticated()` | Every other URL needs a login. Keep it **last** |
| `httpBasic(...)` | Turns on HTTP Basic |
| `csrf.disable()` | CSRF protection guards pages that log in with a cookie. The clients of this API send no cookie, and with it on every `POST` and `PATCH` is refused |

Spring Security reads the rules from the top. The first rule that matches the request is the one that applies.

### 2. The users: `UsersConfig`

[UsersConfig](src/main/java/com/bsp/supportassistant/config/UsersConfig.java)

```java
@Bean
UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    UserDetails meera = User.withUsername("meera")
            .password(passwordEncoder.encode("meera123"))
            .roles("EMPLOYEE")
            .build();
    UserDetails karan = User.withUsername("karan")
            .password(passwordEncoder.encode("karan123"))
            .roles("AGENT")
            .build();
    return new InMemoryUserDetailsManager(meera, karan);
}
```

`roles("EMPLOYEE")` stores the authority `ROLE_EMPLOYEE`. Spring adds the prefix `ROLE_` for you, and `hasRole("EMPLOYEE")` adds it when it checks. `encode(...)` hashes the password before it is stored. These passwords are in the code for the classroom only. A real application never has a password in its code.

### 3. Who is calling: `AccountController`

[AccountController](src/main/java/com/bsp/supportassistant/api/AccountController.java). A controller method can take an `Authentication` parameter. Spring Security fills it with the user who logged in. `GET /api/me` uses it to answer with the user name and the roles.

## Try it

Start the application, then use `curl`. `-u user:password` makes `curl` add the Basic header. `-i` shows the status line.

```bash
curl -i http://localhost:8080/api/public/ping
```

```text
HTTP/1.1 200
{"status":"up"}
```

No login, on a URL that needs one:

```bash
curl -i http://localhost:8080/api/tickets
```

```text
HTTP/1.1 401
WWW-Authenticate: Basic realm="Realm", charset="UTF-8"
```

`WWW-Authenticate` tells the client to try again with Basic authentication. A browser shows a login box when it sees it.

Meera logs in:

```bash
curl -i -u meera:meera123 http://localhost:8080/api/tickets
```

```text
HTTP/1.1 200
[{"id":1,"title":"VPN drops every hour","status":"OPEN"},{"id":2,"title":"Printer is jammed","status":"IN_PROGRESS"}]
```

A wrong password gives `401` again. Now the roles. Karan is an agent, and an agent may not raise a ticket:

```bash
curl -i -u karan:karan123 -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" -d '{"title":"Mouse does not work"}'
```

```text
HTTP/1.1 403
{"timestamp":"...","status":403,"error":"Forbidden","path":"/api/tickets"}
```

Meera may raise it. She gets `201`, and the body `{"id":3,"title":"Mouse does not work","status":"OPEN"}`. Karan may change a status:

```bash
curl -i -u karan:karan123 -X PATCH http://localhost:8080/api/tickets/1/status \
  -H "Content-Type: application/json" -d '{"status":"IN_PROGRESS"}'
```

```text
HTTP/1.1 200
{"id":1,"title":"VPN drops every hour","status":"IN_PROGRESS"}
```

And `GET /api/me` shows who the application thinks you are:

```bash
curl -u karan:karan123 http://localhost:8080/api/me
```

```text
{"username":"karan","roles":["ROLE_AGENT"]}
```

## The tests

[SecurityTests](src/test/java/com/bsp/supportassistant/SecurityTests.java) has seven tests. MockMvc sends pretend requests through the whole security filter, and `httpBasic("meera", "meera123")` adds the header, as `curl -u` does. The tests need no server and no database.

| Test | What it checks |
|---|---|
| `publicUrlNeedsNoLogin` | `/api/public/ping` answers `200` with no login |
| `noLoginIs401AndAsksForBasicAuth` | `/api/tickets` answers `401` and sends `WWW-Authenticate` |
| `wrongPasswordIs401` | A wrong password answers `401` |
| `rightPasswordIsLetIn` | `/api/me` answers with the user and the one role |
| `anEmployeeCanRaiseATicket` | Meera: `POST` answers `201` |
| `anAgentCannotRaiseATicketIs403` | Karan: `POST` answers `403` |
| `onlyAnAgentCanChangeAStatus` | Meera: `PATCH` answers `403`. Karan: `PATCH` answers `200` |

## Common mistakes

> **Mistake 1: every `POST` and `PATCH` is refused, even for the right user.** CSRF protection is still on. `GET` works, so the password looks fine. With `curl` the answer is `401`. In a MockMvc test it is `403`: the test log shows `Status expected:<201> but was:<403>`. Add `.csrf(csrf -> csrf.disable())` for an API that sends no cookies.

> **Mistake 2: the right password gives `401`.** The password was stored as plain text, without `passwordEncoder.encode(...)`. The log shows `WARN ... BCryptPasswordEncoder : Encoded password does not look like BCrypt`. Hash the password before you store it.

> **Mistake 3: the application does not start, with `ROLE_EMPLOYEE cannot start with ROLE_ (it is automatically added)`.** `roles("ROLE_EMPLOYEE")` was written. Write `roles("EMPLOYEE")`. (`authorities("ROLE_EMPLOYEE")` is the form that keeps the prefix.)

> **Mistake 4: the application does not start, with `Can't configure requestMatchers after anyRequest`.** `anyRequest()` was written before another rule. Move `anyRequest().authenticated()` to the end.

> **Mistake 5: `401` and `403` are mixed up.** `401`: who are you? Check the user name and password. `403`: you may not do this. Check the role in `UsersConfig` and the rule in `SecurityConfig`.

## Practice

1. Add a third user, `nisha`, with the role `AGENT`. Log in as `nisha` with `curl -u` and call `PATCH`.
2. Make `GET /api/tickets` open to everyone (move it under `permitAll()`). Which test fails? Change the test so it describes the new rule.
3. Add a test: Karan calls `GET /api/me` and gets the role `ROLE_AGENT`.
4. Write down `Authorization: Basic ...` for `karan:karan123`. Use `echo -n karan:karan123 | base64`, then call the API with `curl -H "Authorization: Basic <your value>"`.

## Recap

- Authentication is "who are you?", and authorization is "may you do this?". The answers are `401` and `403`.
- HTTP Basic sends `user:password` in Base64 in every request. Use HTTPS outside the classroom.
- `SecurityFilterChain` holds the URL rules, `UserDetailsService` finds the users, and `PasswordEncoder` checks the passwords.
- Store a password hash, never the password.

## Layout

```text
src/main/java/com/bsp/supportassistant
├── config/    # SecurityConfig (rules, BCrypt), UsersConfig (in-memory users)
├── api/       # TicketController, AccountController
└── dto/       # TicketResponse, CreateTicketRequest, StatusRequest, MeResponse
src/test/java/com/bsp/supportassistant
└── SecurityTests.java
```

## Running on Spring Boot 3.5

| Item | Spring Boot 4.1.1 (this project) | Spring Boot 3.5.x |
|---|---|---|
| Parent version | `4.1.1` | `3.5.16`, or another 3.5.x |
| Web starter | `spring-boot-starter-webmvc` | `spring-boot-starter-web` |
| Test starter | `spring-boot-starter-webmvc-test` | `spring-boot-starter-test` |
| Security test starter | `spring-boot-starter-security-test` | `spring-security-test` (test scope) |
| `@AutoConfigureMockMvc` import | `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc` | `org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc` |

This project was built and tested on Spring Boot 4.1.1 only. Spring Security 7 adds an extra authority named `FACTOR_PASSWORD` to a user who logged in with a password. `AccountController` leaves it out of the answer.

## What this project does not do

- It does not keep users after the application stops. The two users are made again at each start. See [support-assistant-security-database](../support-assistant-security-database/) for users in a table.
- It does not use HTTPS. Basic authentication without HTTPS shows the password to anyone on the network.
- It does not lock an account after wrong passwords, and it has no sign-up or password change.
- It does not use a session or a token. The client sends the password with every request. Login with JWT is a later topic.
- It does not store tickets. The tickets in the answers are fixed.
