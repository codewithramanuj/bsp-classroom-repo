# Step 7 — MockMvc: a request

> [Back to index](README.md) · Previous: [Capturing an argument](06-capturing-an-argument.md) · Next: [MockMvc: an error body](08-mockmvc-an-error-body.md)

## Goal

`TicketControllerTest` sends a pretend `POST /api/tickets` to the controller, and checks the status code `201` and the JSON of the answer.

## Why this matters

The tests so far look at rules. They do not look at the **web part**: is the URL right, is the body read, is the status code `201`, is the JSON written with the right field names? A mistake there makes a client fail, even when every rule is correct.

To check the web part you would normally start the whole application and call it with `curl`. **MockMvc** gives the same check without a server: it sends a pretend request straight into the controller and gives back the answer. `@WebMvcTest` starts only the web layer (controllers, JSON conversion, error handlers), so the test starts in about two seconds. The service is replaced by a mock, because the web test is not about the rules.

### 7.1 The class that is given to you

Open `src/test/java/com/bsp/supportassistant/api/TicketControllerTest.java`. It already holds the class and two fields:

```java
// Test 3: MockMvc. It sends a pretend HTTP request to the controller, with no real server.
// @WebMvcTest starts only the web layer. The service is a mock.
@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

}
```

| Part | What it does |
|---|---|
| `@WebMvcTest(TicketController.class)` | Starts Spring with the web layer only, for this controller. No service and no repository bean is created |
| `@Autowired MockMvc` | The tool that sends the pretend requests. Spring creates it |
| `@MockitoBean` | Puts a Mockito mock of `TicketService` in the Spring context, so that the controller can be created. Use it in a Spring test where `@Mock` is not enough |

> **Note:** `@Mock` is for a plain Mockito test (steps 4 to 6). `@MockitoBean` is for a test where Spring creates the objects (this step).

### 7.2 The test

Add this test inside the class, below the two fields:

```java
    @Test
    void postCreatesATicketAndAnswers201() throws Exception {
        when(ticketService.create(any()))
                .thenReturn(new TicketResponse(1L, "Printer is jammed", "HIGH", "OPEN"));

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Printer is jammed","priority":"HIGH"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }
```

| Part | What it does |
|---|---|
| `when(ticketService.create(any())).thenReturn(...)` | Stub, as in step 5: the mock service answers with a ticket |
| `post("/api/tickets")` | The pretend request: method and URL |
| `.contentType(MediaType.APPLICATION_JSON)` | Says that the body is JSON. The controller refuses a body without it |
| `.content("""...""")` | The body. A Java text block keeps the JSON readable |
| `status().isCreated()` | The status code must be `201` |
| `jsonPath("$.id").value(1)` | `$` is the whole answer. The field `id` must be `1` |

`perform` can throw a checked exception, so the method says `throws Exception`.

**Check:** run `mvn test`. You should see `Tests run: 9` (3 + 5 + 1) and `BUILD SUCCESS`.

## Try it

```bash
mvn test -Dtest=TicketControllerTest
```

Expected, among the last lines:

```text
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, ... -- in com.bsp.supportassistant.api.TicketControllerTest
[INFO] BUILD SUCCESS
```

This run is slower than the Mockito tests, because Spring starts. Then run all with `mvn test`: you should see `Tests run: 9`.

## Checkpoint

`src/test/java/com/bsp/supportassistant/api/TicketControllerTest.java` - Grows in a later step.

<details>
<summary>Full <code>src/test/java/com/bsp/supportassistant/api/TicketControllerTest.java</code></summary>

```java
package com.bsp.supportassistant.api;

import com.bsp.supportassistant.dto.TicketResponse;
import com.bsp.supportassistant.exception.TicketNotFoundException;
import com.bsp.supportassistant.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Test 3: MockMvc. It sends a pretend HTTP request to the controller, with no real server.
// @WebMvcTest starts only the web layer. The service is a mock.
@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @Test
    void postCreatesATicketAndAnswers201() throws Exception {
        when(ticketService.create(any()))
                .thenReturn(new TicketResponse(1L, "Printer is jammed", "HIGH", "OPEN"));

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Printer is jammed","priority":"HIGH"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }
}
```

</details>

## Common mistakes

| Symptom | Cause | Fix |
|---|---|---|
| `Status expected:<201> but was:<415>` | `.contentType(MediaType.APPLICATION_JSON)` is missing. The controller does not accept a body of unknown type | Add the content type to the request |
| `UnsatisfiedDependencyException ... No qualifying bean of type 'com.bsp.supportassistant.service.TicketService' available` | `@MockitoBean` is missing, so Spring cannot create the controller | Add the mocked service field |
| `No value at JSON path "$.id"` | The `when(...)` stub is missing, so the mock service answers `null` and the body is empty | Add the stub |

Next: **[MockMvc: an error body](08-mockmvc-an-error-body.md)**.
