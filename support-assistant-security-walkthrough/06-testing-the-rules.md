# Step 6 — Testing the rules

> [Back to index](README.md) · Previous: [Roles on URLs](05-roles-on-urls.md) · Next: [Recap and exercises](07-recap-and-exercises.md)

## Goal

`SecurityTests` has seven tests, and they pass. You also break a rule once, and see a test fail.

## Why this matters

In steps 3 to 5 you checked the rules with `curl`, one call at a time. Security rules break quietly: a change in one line can open a URL that should be closed, and nothing in the application complains. Tests run all the calls every time, in a few seconds, and they fail loudly.

## 6.1 The test class

Create the class `SecurityTests` in `src/test/java/com/bsp/supportassistant`, the same package as `SupportAssistantApplication`:

```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityTests {

    @Autowired
    private MockMvc mockMvc;
}
```

| Part | Meaning |
|---|---|
| `@SpringBootTest` | Starts the whole application for the test, with your real rules and your real users |
| `@AutoConfigureMockMvc` | Makes a `MockMvc` for the test |
| `MockMvc` | Sends a pretend HTTP request through the whole security filter and the controller. There is no real server and no network |

| Name | Import |
|---|---|
| `SpringBootTest` | `org.springframework.boot.test.context.SpringBootTest` |
| `AutoConfigureMockMvc` | `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc` |
| `Autowired` | `org.springframework.beans.factory.annotation.Autowired` |
| `MockMvc` | `org.springframework.test.web.servlet.MockMvc` |
| `Test` | `org.junit.jupiter.api.Test` |

Add these static imports at the top of the file. They let you write `get(...)` and `status()` without a class name:

```java
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
```

Also import `org.springframework.http.MediaType`, for the tests in 6.3.

`httpBasic("meera", "meera123")` adds the `Authorization` header, as `curl -u` does.

## 6.2 Four tests for the login

Add these four methods inside the class. Each one is a call from step 3 or 4:

```java
    @Test
    void publicUrlNeedsNoLogin() throws Exception {
        mockMvc.perform(get("/api/public/ping"))
                .andExpect(status().isOk());
    }

    @Test
    void noLoginIs401AndAsksForBasicAuth() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("WWW-Authenticate"));
    }
```

```java
    @Test
    void wrongPasswordIs401() throws Exception {
        mockMvc.perform(get("/api/tickets").with(httpBasic("meera", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rightPasswordIsLetIn() throws Exception {
        mockMvc.perform(get("/api/me").with(httpBasic("meera", "meera123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("meera"))
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_EMPLOYEE"));
    }
```

Each test has three parts: send a request (`perform`), then check the status, then check the body. `jsonPath("$.username")` reads the field `username` of the JSON answer. `$.roles.length()` is the size of the list.

**Check:** run `mvn test`. You should see `Tests run: 4, Failures: 0, Errors: 0, Skipped: 0` and `BUILD SUCCESS`.

## 6.3 Three tests for the roles

Add these inside the class, after the first four:

```java
    @Test
    void anEmployeeCanRaiseATicket() throws Exception {
        mockMvc.perform(post("/api/tickets").with(httpBasic("meera", "meera123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Mouse does not work\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void anAgentCannotRaiseATicketIs403() throws Exception {
        mockMvc.perform(post("/api/tickets").with(httpBasic("karan", "karan123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Mouse does not work\"}"))
                .andExpect(status().isForbidden());
    }
```

```java
    @Test
    void onlyAnAgentCanChangeAStatus() throws Exception {
        String body = "{\"status\":\"IN_PROGRESS\"}";

        mockMvc.perform(patch("/api/tickets/1/status").with(httpBasic("meera", "meera123"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/tickets/1/status").with(httpBasic("karan", "karan123"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }
```

`contentType(...)` and `content(...)` set the type and the text of the request body, as `-H "Content-Type: ..."` and `-d` do in `curl`.

**Check:** run `mvn test`. You should see `Tests run: 7, Failures: 0, Errors: 0, Skipped: 0` and `BUILD SUCCESS`. The finished class is [SecurityTests](../support-assistant-security/src/test/java/com/bsp/supportassistant/SecurityTests.java).

## 6.4 Break a rule on purpose

A test that has never failed has not proved anything. Open `SecurityConfig` and change the `PATCH` rule from `hasRole("AGENT")` to `hasRole("EMPLOYEE")`. Now Meera may change statuses, and Karan may not.

Run `mvn test`. You should see one failure:

```text
[ERROR]   SecurityTests.onlyAnAgentCanChangeAStatus:77 Status expected:<403> but was:<200>
[ERROR] Tests run: 7, Failures: 1, Errors: 0, Skipped: 0
```

Meera's `PATCH` was expected to answer `403`, and it answered `200`. The name of the test tells you which rule broke. (The line number in your file may differ.)

Put `hasRole("AGENT")` back, and run `mvn test`. It shows `Tests run: 7, Failures: 0`.

## Try it

```bash
mvn clean verify
```

Expected, among the last lines:

```text
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, ... -- in com.bsp.supportassistant.SecurityTests
[INFO] BUILD SUCCESS
```

## Common mistakes

| Symptom | Cause | Fix |
|---|---|---|
| `Status expected:<200> but was:<401>` | The request has no `.with(httpBasic(...))` | Add `.with(httpBasic("user", "password"))` |
| `Status expected:<201> but was:<403>` in a `POST` test of a right user | CSRF is on in `SecurityConfig` | Add `.csrf(csrf -> csrf.disable())` (step 5.4) |
| `Tests run: 0` | The method has no `@Test` above it | Add `@Test` |
| `package org.springframework.boot.test.autoconfigure.web.servlet does not exist` | The import of Spring Boot 3.5 in a 4.1.1 project | Import `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc` |

Next: **[Recap and exercises](07-recap-and-exercises.md)**.
