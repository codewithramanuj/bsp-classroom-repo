# support-assistant-security-database

The same secured ticket API as [support-assistant-security](../support-assistant-security/), with **two changes**:

1. The users are no longer two fixed users in code. They are rows in an **Oracle table**, `DESK_USERS`.
2. The caller logs in **once** and gets a **JWT** (JSON Web Token). Every later request sends the token instead of the user name and password. A filter checks the token.

The roles `EMPLOYEE` and `AGENT`, the URL rules, and the BCrypt hashes stay the same. Read the first project's README first for those ideas.

**Time:** about 45 minutes.

## What is a JWT?

A JWT is a short text that the server hands out after a good login. It holds the user name and an expiry time, and it is **signed** with a secret key that only the server knows. If anyone changes a character, the signature no longer matches and the server refuses the token. It is like a wristband at an event: you show ID once at the gate, then you only show the wristband.

## How a request goes

```mermaid
sequenceDiagram
    participant C as Client
    participant L as LoginController
    participant F as JwtFilter
    participant T as TicketController
    C->>L: POST /api/public/login with user name and password
    L-->>C: 200 with a token
    C->>F: GET /api/tickets with Authorization Bearer token
    F->>F: check the signature and the expiry
    F->>T: user is logged in
    T-->>C: 200 with the tickets
```

**How to read it:** the login is the only time the password is sent. After that `JwtFilter` runs before every other URL rule, reads the token, and tells Spring Security who the user is.

## What changed from project 1

```mermaid
flowchart LR
    filter["Security filter"] --> uds["UserDetailsService"]
    uds -->|"project 1"| mem["InMemoryUserDetailsManager"]
    uds -->|"this project"| db["DatabaseUserDetailsService"]
    db --> repo["UserRepository (JPA)"]
    repo --> table[("DESK_USERS")]
```

**How to read it:** the security filter only knows the `UserDetailsService` interface. In project 1 an in-memory class answers it. Here `DatabaseUserDetailsService` answers it, by reading the table. `SecurityConfig` and the controllers did not change.

| Part | Project 1 | This project |
|---|---|---|
| How the caller proves who they are | `Authorization: Basic ...` on every request | `Authorization: Bearer TOKEN` on every request, after one login |
| Where the users are | `UsersConfig`, in memory | The table `DESK_USERS` |
| Who finds a user | `InMemoryUserDetailsManager` | `DatabaseUserDetailsService` + `UserRepository` |
| The stored password | Hashed at start-up with `encode(...)` | Already a BCrypt hash in the table |
| Added to `pom.xml` | none | `spring-boot-starter-data-jpa`, `ojdbc17`, `jjwt-api`, `jjwt-impl`, `jjwt-jackson` |

The users in the table are the same as before:

| User | Password | Role |
|---|---|---|
| `meera` | `meera123` | `EMPLOYEE` |
| `karan` | `karan123` | `AGENT` |

The URLs and who may call them are the same as in project 1, plus one new URL: `POST /api/public/login`.

## Versions

| Item | Version |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.1 (brings Spring Security 7) |
| Oracle JDBC driver | `ojdbc17` (comes with Spring Boot) |
| JWT library (jjwt) | 0.12.6 |
| Oracle Database | 19c, or 21c Express Edition |

## Before you build or run

1. Run the scripts in [db/oracle](db/oracle/), in order, as the user that your application connects as.

   | Script | What it does |
   |---|---|
   | `01-tables.sql` | Creates the sequence `DESK_USERS_SEQ` and the table `DESK_USERS` |
   | `02-sample-users.sql` | Inserts `meera` and `karan` with their BCrypt hashes |
   | `99-drop.sql` | Removes everything, so that you can start again |

2. Set the environment variables that the application reads:

   | Variable | Value |
   |---|---|
   | `DB_USERNAME` | Your Oracle user |
   | `DB_PASSWORD` | Your Oracle password |
   | `DB_URL` | Optional. Default: `jdbc:oracle:thin:@//localhost:1521/XEPDB1` |
   | `JWT_SECRET` | The key that signs the tokens. At least 32 characters. Choose your own, and do not put it in a file you commit |

   If `DB_PASSWORD` is not set, the application sends the text `${DB_PASSWORD}` to Oracle, and the first database call fails with `ORA-01017`. If `JWT_SECRET` is not set, the application does not start.

## Build, test, and run

```bash
mvn clean verify
java -jar target/support-assistant-security-database-0.0.1-SNAPSHOT.jar
```

`mvn clean verify` runs fourteen tests. Six need no database: two use a Mockito mock and four test the token code alone. Eight call Oracle and need the two scripts. You should see `Tests run: 14, Failures: 0, Errors: 0`.

Log in, and keep the token:

```bash
curl -s -X POST http://localhost:8080/api/public/login \
  -H "Content-Type: application/json" \
  -d '{"username":"meera","password":"meera123"}'
```

```text
{"token":"eyJ..."}
```

Send the token with each request:

```bash
curl -i http://localhost:8080/api/tickets -H "Authorization: Bearer PASTE_THE_TOKEN_HERE"
```

```text
HTTP/1.1 200
```

Without the header, or with a wrong token, the answer is `401`. A valid token of `meera` on `PATCH /api/tickets/1/status` gives `403`: the token proves who she is, and the table says she is an `EMPLOYEE`.

## The code

### 1. The table

[01-tables.sql](db/oracle/01-tables.sql)

```sql
CREATE TABLE DESK_USERS (
    ID             NUMBER        PRIMARY KEY,
    USERNAME       VARCHAR2(50)  NOT NULL,
    PASSWORD_HASH  VARCHAR2(100) NOT NULL,
    USER_ROLE      VARCHAR2(20)  NOT NULL,
    CONSTRAINT DESK_USERS_NAME_UK UNIQUE (USERNAME),
    CONSTRAINT DESK_USERS_ROLE_CK CHECK (USER_ROLE IN ('EMPLOYEE', 'AGENT'))
);
```

`PASSWORD_HASH` holds the BCrypt hash, 60 characters long. The password itself is never stored. `UNIQUE (USERNAME)` makes sure a name finds one user. `USER_ROLE` is `EMPLOYEE` or `AGENT`, **without** the `ROLE_` prefix, and the `CHECK` refuses anything else.

### 2. The entity and the repository

[AppUser](src/main/java/com/bsp/supportassistant/model/AppUser.java) is the JPA entity for one row. It is marked `@Immutable`: the application reads users and never changes them.

[UserRepository](src/main/java/com/bsp/supportassistant/repository/UserRepository.java) has one method. Spring Data writes the SQL from its name:

```java
public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);
}
```

### 3. The part that Spring Security calls

[DatabaseUserDetailsService](src/main/java/com/bsp/supportassistant/service/DatabaseUserDetailsService.java)

```java
@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public DatabaseUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        AppUser appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user named " + username));

        return User.withUsername(appUser.getUsername())
                .password(appUser.getPasswordHash())
                .roles(appUser.getUserRole())
                .build();
    }
}
```

Spring Security calls `loadUserByUsername` with the name from the `Authorization` header. The method returns the user with the **stored hash**. It does not compare passwords. Spring Security does that next, with the `PasswordEncoder`.

Spring Boot finds this `@Service` because it is a `UserDetailsService` bean, and uses it when a user logs in. There is nothing else to connect.

If the name is not in the table, the method throws `UsernameNotFoundException`, and the client gets `401`. It is the same answer as for a wrong password, so a caller cannot find out which names exist.

### 4. Making and reading the token

[JwtService](src/main/java/com/bsp/supportassistant/security/JwtService.java) has two methods. The secret and the expiry come from [application.yaml](src/main/resources/application.yaml).

```java
public String createToken(String username) {
    Date now = new Date();
    return Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + expiryMillis))
            .signWith(key)
            .compact();
}

public String readUsername(String token) {
    return Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).getPayload().getSubject();
}
```

`readUsername` throws a `JwtException` if the token was changed, was signed with another key, or has expired. The token holds only the user name. The role is **not** in the token.

### 5. The login URL

[LoginController](src/main/java/com/bsp/supportassistant/api/LoginController.java)

```java
@PostMapping("/login")
public LoginResponse login(@RequestBody LoginRequest request) {
    authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
    return new LoginResponse(jwtService.createToken(request.username()));
}
```

`authenticate` calls `DatabaseUserDetailsService` and compares the password with the BCrypt hash. For a wrong login it throws an exception, and the client gets `401`. For a good login the next line makes the token. The URL is under `/api/public`, so it is open to everyone.

### 6. The filter that checks the token

[JwtFilter](src/main/java/com/bsp/supportassistant/security/JwtFilter.java) runs once for every request.

```java
String header = request.getHeader("Authorization");

if (header != null && header.startsWith("Bearer ")) {
    try {
        String username = jwtService.readUsername(header.substring(7));
        UserDetails user = userDetailsService.loadUserByUsername(username);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
    } catch (JwtException | IllegalArgumentException | AuthenticationException e) {
        // a bad token: stay not logged in
    }
}
chain.doFilter(request, response);
```

If the token is good, the filter loads the user from the table and tells Spring Security who is calling. If the token is missing or bad, the filter does nothing, and the URL rules in `SecurityConfig` answer `401` for every URL except `/api/public/**`. The roles are read from the table on each request, so a role change works at once.

### 7. Connecting the filter

[SecurityConfig](src/main/java/com/bsp/supportassistant/config/SecurityConfig.java) has three changes. `httpBasic` is removed, and these are added:

```java
.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
.exceptionHandling(errors -> errors.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
```

- `addFilterBefore` puts `JwtFilter` ahead of the URL rules.
- `STATELESS` means the server keeps no login session. Every request brings its own token.
- The entry point makes "not logged in" answer `401`. Without it, Spring Security answers `403`.

There is also an `AuthenticationManager` bean, which the login URL uses.

### 8. Letting the React app call the API

A browser blocks a page on one address from calling an API on another. The React app runs on `http://localhost:5173` (the Vite default) and this API on `http://localhost:8080`, so the API must allow it. [CorsConfig](src/main/java/com/bsp/supportassistant/config/CorsConfig.java) does that for `/api/**`:

```java
registry.addMapping("/api/**")
        .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173")
        .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("Authorization", "Content-Type");
```

The token travels in the `Authorization` header, so `allowedHeaders` must list it. If your React app runs on another port, change the addresses here.

Before a `POST` or `PATCH` with a token, the browser first sends a question without any token: "may this page do that?" (an `OPTIONS` request). With Spring Security, that question would get `401`, and the browser would never send the real call. So `SecurityConfig` also has this line:

```java
.cors(Customizer.withDefaults())
```

It makes Spring Security answer the question using the rules in `CorsConfig`, before the login check.

In React, log in once, keep the token, and send it on each call:

```javascript
const login = await fetch('http://localhost:8080/api/public/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: 'meera', password: 'meera123' }),
})
const { token } = await login.json()

const response = await fetch('http://localhost:8080/api/tickets', {
  headers: { Authorization: `Bearer ${token}` },
})
```

## The tests

| Test class | Tests | Needs |
|---|---|---|
| [DatabaseUserDetailsServiceTest](src/test/java/com/bsp/supportassistant/service/DatabaseUserDetailsServiceTest.java) | 2: a known user gets the stored hash and `ROLE_AGENT`; an unknown user is refused | Nothing. The repository is a Mockito mock |
| [JwtServiceTest](src/test/java/com/bsp/supportassistant/security/JwtServiceTest.java) | 4: a token gives back the user name; a changed token, a token with another secret, and an expired token are refused | Nothing. Plain JUnit |
| [SecurityDatabaseTests](src/test/java/com/bsp/supportassistant/SecurityDatabaseTests.java) | 8: log in and use the token; a wrong password gives `401`; a name not in the table gives `401`; no token gives `401`; a wrong token gives `401`; the role from the table decides `403` or `200`; the React address may ask before a `PATCH`; another website is refused | Oracle, and `01-tables.sql` and `02-sample-users.sql` |

The eight database tests only read `DESK_USERS`, so they leave the table as it was. They set their own secret, so they do not need `JWT_SECRET`.

## Common mistakes

> **Mistake 1: the right password gives `401`, and the table holds plain text.** The log shows `WARN ... BCryptPasswordEncoder : Encoded password does not look like BCrypt`. The `PASSWORD_HASH` column must hold a BCrypt hash, which starts with `$2a$`. Insert the hash, not the password.

> **Mistake 2: the role is stored as `ROLE_AGENT`.** The table refuses it, because of the `CHECK`. Store `AGENT`. Spring Security adds the prefix in `roles(...)`.

> **Mistake 3: the application does not start, with `ORA-01017`.** Oracle refused the login. Check that `DB_USERNAME` and `DB_PASSWORD` are set in the terminal that runs the application.

> **Mistake 4: a new user cannot log in.** The row was inserted but not committed, and the application cannot see it. Run `COMMIT;` after the `INSERT`.

> **Mistake 5: the application does not start, with `Could not resolve placeholder 'JWT_SECRET'`.** Set `JWT_SECRET` in the terminal that runs the application. It must be at least 32 characters, or the first login fails with a `WeakKeyException`.

> **Mistake 6: the React page shows `Failed to fetch`, and the Console says `blocked by CORS policy`.** The page is not on an address listed in `CorsConfig`. Vite moves to port 5174 if 5173 is busy. Add the address to `allowedOrigins` and restart the API. A missing `Authorization` in `allowedHeaders` gives the same message.

> **Mistake 7: a good token gives `401`.** Check the header: it must be `Authorization: Bearer TOKEN`, with the word `Bearer` and one space. The token also expires after 30 minutes. Log in again.

## Practice

1. Log in as `meera`. Wait for the token to expire (set `expiry-minutes` to `1` to make it quick). Call `GET /api/tickets` again. What do you see?
2. Hash a password for a new user, `nisha`. Use `BCryptPasswordEncoder` in a small test or in `jshell`, then `INSERT` the row with the role `AGENT` and `COMMIT`. Log in as `nisha` and call `GET /api/me` with the token. You do not restart the application.
3. Change the role of `karan` to `EMPLOYEE` with an `UPDATE` and a `COMMIT`. Call `PATCH` with the token that `karan` got **before** the change. What do you see, and why?
4. Add a test to `DatabaseUserDetailsServiceTest`: a user with the role `EMPLOYEE` gets `ROLE_EMPLOYEE`.

## Recap

- The caller sends the password once, to `POST /api/public/login`. The answer is a signed token.
- `JwtFilter` checks the token on every other request and tells Spring Security who the user is. A missing or bad token gives `401`.
- The token holds the user name only. The role is read from the table on each request.
- The security filter asks a `UserDetailsService` for the user. Only that part changes when the users move from memory to a table.
- The table stores a BCrypt hash. Spring Security compares the typed password with it.
- The role is stored without `ROLE_`, and `roles(...)` adds the prefix.
- A name that is not in the table and a wrong password both give `401`.

## Layout

```text
src/main/java/com/bsp/supportassistant
├── config/       # SecurityConfig (rules, BCrypt, connects the filter), CorsConfig
├── security/     # JwtService, JwtFilter
├── model/        # AppUser
├── repository/   # UserRepository
├── service/      # DatabaseUserDetailsService
├── api/          # TicketController, AccountController, LoginController
└── dto/          # TicketResponse, CreateTicketRequest, StatusRequest, MeResponse, LoginRequest, LoginResponse
src/test/java/com/bsp/supportassistant
├── security/JwtServiceTest.java                   # plain JUnit
├── service/DatabaseUserDetailsServiceTest.java    # JUnit with Mockito
└── SecurityDatabaseTests.java                     # MockMvc, needs Oracle
db/oracle
├── 01-tables.sql
├── 02-sample-users.sql
└── 99-drop.sql
```

## Running on Spring Boot 3.5

| Item | Spring Boot 4.1.1 (this project) | Spring Boot 3.5.x |
|---|---|---|
| Parent version | `4.1.1` | `3.5.16`, or another 3.5.x |
| Web starter | `spring-boot-starter-webmvc` | `spring-boot-starter-web` |
| Test starter | `spring-boot-starter-webmvc-test` | `spring-boot-starter-test` |
| Security test starter | `spring-boot-starter-security-test` | `spring-security-test` (test scope) |
| jjwt (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`) | `0.12.6` | `0.12.6` (same) |
| `@AutoConfigureMockMvc` import | `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc` | `org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc` |
| Oracle driver | `ojdbc17` | `ojdbc11` |

This project was built on Spring Boot 4.1.1 only.

## What this project does not do

- It has not been run against Oracle. The six tests that need no database were run. The eight database tests were run on a temporary in-memory H2 copy of the same table, not on Oracle. A running server with `curl`, and a real browser with the React app, were not tried. The CORS rules were checked with `MockMvc` only.
- It does not add, change, or delete users. Users are added with SQL.
- It does not check that a password is strong, and it does not lock an account after wrong passwords.
- It reads the user from the table on every request, with one `SELECT` each. It does not cache users.
- It does not use HTTPS. Without HTTPS the password and the token travel as plain text.
- It has no logout and no refresh token. A token works until it expires, and it cannot be cancelled before that.
- The token is sent in the `Authorization` header only. It does not use a cookie.
