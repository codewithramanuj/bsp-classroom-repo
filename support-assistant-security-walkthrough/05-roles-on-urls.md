# Step 5 — Roles on URLs

> [Back to index](README.md) · Previous: [In-memory users](04-in-memory-users.md) · Next: [Testing the rules](06-testing-the-rules.md)

## Goal

Only an `EMPLOYEE` can raise a ticket, only an `AGENT` can change a status, and any logged-in user can list the tickets.

## Why this matters

A login says who you are. A role says what you may do. Without roles, an employee could close tickets that only an agent should close. The rule sits in `SecurityConfig`, beside the other rules, not inside each controller.

## 5.1 The ticket URLs

Create three records in the package `dto`, each in its own file:

```java
public record TicketResponse(long id, String title, String status) {
}
```

```java
public record CreateTicketRequest(String title) {
}
```

```java
public record StatusRequest(String status) {
}
```

Create the class `TicketController` in the package `api`:

```java
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @GetMapping
    public List<TicketResponse> list() {
        return List.of(
                new TicketResponse(1, "VPN drops every hour", "OPEN"),
                new TicketResponse(2, "Printer is jammed", "IN_PROGRESS"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse create(@RequestBody CreateTicketRequest request) {
        return new TicketResponse(3, request.title(), "OPEN");
    }
```

Add the last method inside the same class:

```java
    @PatchMapping("/{id}/status")
    public TicketResponse changeStatus(@PathVariable long id, @RequestBody StatusRequest request) {
        return new TicketResponse(id, "VPN drops every hour", request.status());
    }
```

The tickets are fixed examples. The controller has no security code. The rules are in `SecurityConfig`.

| Name | Import |
|---|---|
| `GetMapping`, `PostMapping`, `PatchMapping`, `PathVariable`, `RequestBody`, `RequestMapping`, `ResponseStatus`, `RestController` | `org.springframework.web.bind.annotation` |
| `HttpStatus` | `org.springframework.http.HttpStatus` |

The finished class is [TicketController](../support-assistant-security/src/main/java/com/bsp/supportassistant/api/TicketController.java).

## 5.2 A rule for each role

Open `SecurityConfig`. Add two lines **between** `permitAll()` and `anyRequest()`:

```java
.authorizeHttpRequests(rules -> rules
        .requestMatchers("/api/public/**").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/tickets").hasRole("EMPLOYEE")
        .requestMatchers(HttpMethod.PATCH, "/api/tickets/*/status").hasRole("AGENT")
        .anyRequest().authenticated())
```

| Part | Meaning |
|---|---|
| `HttpMethod.POST, "/api/tickets"` | Only a `POST` to this URL. A `GET` to the same URL falls through to `anyRequest()` |
| `/api/tickets/*/status` | The `*` stands for any id: `/api/tickets/1/status`, `/api/tickets/7/status` |
| `hasRole("EMPLOYEE")` | A login is needed, and the user must have the role `EMPLOYEE`. Write it without `ROLE_` |

`HttpMethod` is `org.springframework.http.HttpMethod`.

## 5.3 Try a `POST`

Start the application. Listing works for both users:

```bash
curl -i -u karan:karan123 http://localhost:8080/api/tickets
```

```text
HTTP/1.1 200
[{"id":1,"title":"VPN drops every hour","status":"OPEN"},{"id":2,"title":"Printer is jammed","status":"IN_PROGRESS"}]
```

Now Meera raises a ticket. Meera is an `EMPLOYEE`, so this should work:

```bash
curl -i -u meera:meera123 -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" -d '{"title":"Mouse does not work"}'
```

```text
HTTP/1.1 401
WWW-Authenticate: Basic realm="Realm", charset="UTF-8"
```

It fails, and the password is right. `GET` worked a moment ago. A `PATCH` fails the same way.

## 5.4 Switch off CSRF

**CSRF** protection guards web pages that log in with a **cookie**: it refuses a `POST` or `PATCH` that does not carry a secret token. This API logs in with a header on every request and sends no cookie, so there is nothing to guard, and the protection refuses every `POST` and `PATCH`.

Add one line at the end of the chain in `SecurityConfig`:

```java
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable());
        return http.build();
```

Remove the semicolon after `httpBasic(...)`, so that the `.csrf(...)` line continues the same statement.

Run the application again, and try the calls. Meera raises a ticket:

```bash
curl -i -u meera:meera123 -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" -d '{"title":"Mouse does not work"}'
```

```text
HTTP/1.1 201
{"id":3,"title":"Mouse does not work","status":"OPEN"}
```

Karan is an agent, and an agent may not raise a ticket:

```bash
curl -i -u karan:karan123 -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" -d '{"title":"x"}'
```

```text
HTTP/1.1 403
{"timestamp":"...","status":403,"error":"Forbidden","path":"/api/tickets"}
```

`403`: the application knows Karan, and Karan's role is not allowed here. Now the status change:

```bash
curl -i -u meera:meera123 -X PATCH http://localhost:8080/api/tickets/1/status \
  -H "Content-Type: application/json" -d '{"status":"IN_PROGRESS"}'
```

```text
HTTP/1.1 403
```

```bash
curl -i -u karan:karan123 -X PATCH http://localhost:8080/api/tickets/1/status \
  -H "Content-Type: application/json" -d '{"status":"IN_PROGRESS"}'
```

```text
HTTP/1.1 200
{"id":1,"title":"VPN drops every hour","status":"IN_PROGRESS"}
```

**Check:** Meera's `POST` gives `201`, Karan's `POST` gives `403`, Meera's `PATCH` gives `403`, Karan's `PATCH` gives `200`. Your `SecurityConfig` should now match the finished [SecurityConfig](../support-assistant-security/src/main/java/com/bsp/supportassistant/config/SecurityConfig.java). Stop the application.

| Answer | Meaning | Where it came from here |
|---|---|---|
| `401` | The application does not know who you are | No login, a wrong password, or CSRF refusing the request |
| `403` | It knows who you are, and your role is not allowed | Karan on `POST`, Meera on `PATCH` |

## Common mistakes

| Symptom | Cause | Fix |
|---|---|---|
| Every `POST` and `PATCH` gives `401` with the right password, and `GET` works | CSRF is still on | Add `.csrf(csrf -> csrf.disable())` (5.4) |
| The application does not start, with `Can't configure requestMatchers after anyRequest` | `anyRequest()` was written before another rule | Move `anyRequest().authenticated()` to the end |
| The application does not start, with `ROLE_AGENT should not start with ROLE_ since ROLE_ is automatically prepended` | `hasRole("ROLE_AGENT")` was written | Write `hasRole("AGENT")` |
| `PATCH` gives `403` for Karan | The pattern does not match the URL, or `hasRole` names the wrong role | Compare `/api/tickets/*/status` with the URL you called |

Next: **[Testing the rules](06-testing-the-rules.md)**.
