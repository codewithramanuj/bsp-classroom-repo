# Step 4 — In-memory users

> [Back to index](README.md) · Previous: [Your own URL rules](03-your-own-url-rules.md) · Next: [Roles on URLs](05-roles-on-urls.md)

## Goal

`meera` and `karan` can log in, the generated `user` can not, and `GET /api/me` says who you are.

## Why this matters

Until now, the only user was the one Spring Security invented. Real people need their own names and passwords. The simplest place to keep them is in memory: two objects made when the application starts. This is not for production, but it shows the whole login path with no database.

## 4.1 The password encoder

Open `SecurityConfig` and add a second bean, below the first one:

```java
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
```

| Name | Import |
|---|---|
| `PasswordEncoder` | `org.springframework.security.crypto.password.PasswordEncoder` |
| `BCryptPasswordEncoder` | `org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder` |

This bean does two jobs. It **hashes** a password when you store it, and it **checks** a typed password against a stored hash when someone logs in. BCrypt gives a different hash each time for the same password, and it still knows how to check a match.

## 4.2 The users

Create the class `UsersConfig` in the package `config`:

```java
@Configuration
public class UsersConfig {

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
}
```

| Part | Meaning |
|---|---|
| `UserDetailsService` | The part from step 1 that finds a user by name |
| `InMemoryUserDetailsManager` | A `UserDetailsService` that keeps its users in a list in memory |
| `passwordEncoder.encode(...)` | Hashes the password **before** it is stored |
| `roles("EMPLOYEE")` | Stores the authority `ROLE_EMPLOYEE`. Spring adds the `ROLE_` prefix for you |

The method takes `PasswordEncoder` as a parameter, and Spring passes the bean from 4.1. These passwords are written in the code for the classroom only. A real application never has a password in its code.

| Name | Import |
|---|---|
| `User`, `UserDetails`, `UserDetailsService` | `org.springframework.security.core.userdetails` |
| `InMemoryUserDetailsManager` | `org.springframework.security.provisioning.InMemoryUserDetailsManager` |

`User` has more than one candidate in the list. Choose the one in `org.springframework.security.core.userdetails`.

The finished class is [UsersConfig](../support-assistant-security/src/main/java/com/bsp/supportassistant/config/UsersConfig.java).

## 4.3 Who am I: `GET /api/me`

Create the class `MeResponse` in a new package `dto`. A **record** is a short way to write a class that only holds values. This one has two fields, and Java makes the constructor and the getters `username()` and `roles()`:

```java
public record MeResponse(String username, List<String> roles) {
}
```

Open `AccountController` and add this method:

```java
    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        return new MeResponse(
                authentication.getName(),
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .filter(name -> name.startsWith("ROLE_"))
                        .toList());
    }
```

A controller method can take an `Authentication` parameter. Spring Security fills it with the user who logged in. `getAuthorities()` holds the roles, written as `ROLE_EMPLOYEE`. The `filter` keeps only the roles: Spring Security 7 also adds an authority named `FACTOR_PASSWORD` (how the user proved who they are), and it is not a role.

| Name | Import |
|---|---|
| `Authentication` | `org.springframework.security.core.Authentication` |
| `GrantedAuthority` | `org.springframework.security.core.GrantedAuthority` |
| `MeResponse` | `com.bsp.supportassistant.dto.MeResponse` |
| `List` | `java.util.List` |

## 4.4 Run it and log in

Start the application. This time the log has **no** `Using generated security password` line, because you made a `UserDetailsService`. Then:

```bash
curl -i -u meera:meera123 http://localhost:8080/api/me
```

```text
HTTP/1.1 200
{"username":"meera","roles":["ROLE_EMPLOYEE"]}
```

```bash
curl -u karan:karan123 http://localhost:8080/api/me
```

```text
{"username":"karan","roles":["ROLE_AGENT"]}
```

Three calls that must fail with `401`:

```bash
curl -i -u meera:wrong http://localhost:8080/api/me
curl -i http://localhost:8080/api/me
curl -i -u user:anything http://localhost:8080/api/me
```

```text
HTTP/1.1 401
```

The first has the wrong password, the second has no login, and the third uses the old generated user, which is gone.

**Check:** `meera` and `karan` get `200` with their roles, and the three calls above get `401`. Stop the application.

## Common mistakes

| Symptom | Cause | Fix |
|---|---|---|
| The application does not start, with `ROLE_EMPLOYEE cannot start with ROLE_ (it is automatically added)` | `roles("ROLE_EMPLOYEE")` was written | Write `roles("EMPLOYEE")` |
| The right password gives `401`, and the log has `Encoded password does not look like BCrypt` | The password was stored as plain text, without `passwordEncoder.encode(...)` | Hash it with `passwordEncoder.encode(...)` |

Next: **[Roles on URLs](05-roles-on-urls.md)**.
