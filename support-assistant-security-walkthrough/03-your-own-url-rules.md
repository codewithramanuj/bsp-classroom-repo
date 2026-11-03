# Step 3 — Your own URL rules

> [Back to index](README.md) · Previous: [Project and the default lock](02-project-and-the-default-lock.md) · Next: [In-memory users](04-in-memory-users.md)

## Goal

`/api/public/ping` answers without a login, and every other URL needs one.

## Why this matters

The default lock is the same for every URL. Real applications need a few open URLs (a health check, a welcome page) and many closed ones. You write the rules once, in one class, so a reader finds all of them in one place.

## 3.1 The `SecurityConfig` class

Create the package `config` and the class `SecurityConfig` in it:

```java
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(rules -> rules
                        .requestMatchers("/api/public/**").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
```

| Part | Meaning |
|---|---|
| `@Configuration` | This class holds settings for Spring |
| `@Bean` | Spring calls the method once and keeps the result. Spring Security finds the `SecurityFilterChain` bean and uses it **instead of** the default lock |
| `requestMatchers("/api/public/**")` | Every URL that starts with `/api/public/` |
| `permitAll()` | No login needed |
| `anyRequest().authenticated()` | Every other URL needs a login |
| `httpBasic(...)` | Log in with the Basic header. Without it, nobody can log in |

Spring Security reads the rules from the top, and the first rule that matches the request is the one that applies. Put `anyRequest()` **last**.

Pick these imports when IntelliJ asks:

| Name | Import |
|---|---|
| `Configuration`, `Bean` | `org.springframework.context.annotation` |
| `HttpSecurity` | `org.springframework.security.config.annotation.web.builders.HttpSecurity` |
| `Customizer` | `org.springframework.security.config.Customizer` |
| `SecurityFilterChain` | `org.springframework.security.web.SecurityFilterChain` |

The finished class is [SecurityConfig](../support-assistant-security/src/main/java/com/bsp/supportassistant/config/SecurityConfig.java). It has more rules, which you add in steps 4 and 5.

## 3.2 Run it and call it

Start the application with `mvn spring-boot:run`, and call the open URL with no login:

```bash
curl -i http://localhost:8080/api/public/ping
```

```text
HTTP/1.1 200
{"status":"up"}
```

Call a URL that has no controller yet:

```bash
curl -i http://localhost:8080/api/tickets
```

```text
HTTP/1.1 401
```

`/api/tickets` does not exist yet, and the answer is still `401`. The filter checks the login **before** it looks for a controller.

Now log in with the generated password from this run's log:

```bash
curl -i -u user:THE_PASSWORD_FROM_THE_LOG http://localhost:8080/api/tickets
```

```text
HTTP/1.1 404
```

`404` means the filter let you in, and then no controller was found. The generated user still exists, because you have not made any user of your own.

**Check:** `/api/public/ping` gives `200` with no login, `/api/tickets` gives `401` with no login, and `404` with the generated password. Stop the application.

## Common mistakes

| Symptom | Cause | Fix |
|---|---|---|
| `/api/tickets` answers `403`, not `401`, and `-u user:...` does not help | `.httpBasic(Customizer.withDefaults())` is missing. Your chain replaced the default one, and now has no way to log in | Add the `httpBasic` line |
| `/api/public/ping` answers `401` | The path in `requestMatchers` has a typo | Check the path. It must match the URL |
| `SecurityConfig` has red names | A missing import | `Alt+Enter`, and use the table in 3.1 |

Next: **[In-memory users](04-in-memory-users.md)**.
