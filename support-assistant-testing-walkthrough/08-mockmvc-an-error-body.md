# Step 8 — MockMvc: an error body

> [Back to index](README.md) · Previous: [MockMvc: a request](07-mockmvc-a-request.md) · Next: [Recap and exercises](09-recap-and-exercises.md)

## Goal

`TicketControllerTest` has a second test: asking for a ticket that does not exist answers `404` with a JSON error body.

## Why this matters

When something goes wrong, the client sees only the status code and the body. A good test checks both. The exception is thrown in the service, but it is the web layer that turns it into `404` and into the `ApiError` JSON. That is the part this test looks at.

You do not need the real service for this. The mock service **throws** the exception, the way the real one would. `GlobalExceptionHandler` is part of the web layer, so `@WebMvcTest` loads it, and the test sees the real `404` and the real body. The rule "a missing ticket is an error" is tested in step 5. It is not tested again here.

### 8.1 The test

Add this test at the end of the class:

```java
    @Test
    void getOfAMissingTicketAnswers404WithAnErrorBody() throws Exception {
        when(ticketService.get(99L)).thenThrow(new TicketNotFoundException(99L));

        mockMvc.perform(get("/api/tickets/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ticket 99 does not exist"));
    }
```

| Part | What it does |
|---|---|
| `when(ticketService.get(99L)).thenThrow(...)` | Stub: "when the controller asks for ticket 99, throw this exception" |
| `get("/api/tickets/99")` | A pretend `GET` request |
| `status().isNotFound()` | The status code must be `404` |
| `jsonPath("$.message")` | Reads the field `message` of the `ApiError` body |

**Check:** run `mvn test`. You should see `Tests run: 10` (3 + 5 + 2) and `BUILD SUCCESS`.

### 8.2 See it fail

Change the text in the test to `"Ticket 99 is gone"` and run `mvn test`. You should see:

```text
[ERROR]   TicketControllerTest.getOfAMissingTicketAnswers404WithAnErrorBody:52 JSON path "$.message" expected:<Ticket 99 is gone> but was:<Ticket 99 does not exist>
```

The message shows the field that was read, what you expected, and what the answer held. **Put the text back** and run `mvn test` again.

## Try it

```bash
mvn test
```

Expected, among the last lines:

```text
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, ... -- in com.bsp.supportassistant.model.StatusRulesTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, ... -- in com.bsp.supportassistant.api.TicketControllerTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, ... -- in com.bsp.supportassistant.service.TicketServiceTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## Checkpoint

`src/test/java/com/bsp/supportassistant/api/TicketControllerTest.java` - This is the finished file.

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

    @Test
    void getOfAMissingTicketAnswers404WithAnErrorBody() throws Exception {
        when(ticketService.get(99L)).thenThrow(new TicketNotFoundException(99L));

        mockMvc.perform(get("/api/tickets/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ticket 99 does not exist"));
    }
}
```

</details>

## Common mistakes

| Symptom | Cause | Fix |
|---|---|---|
| `ServletException: Request processing failed: java.lang.RuntimeException` | The mock threw an exception that `GlobalExceptionHandler` does not handle, so nothing turns it into an answer | Throw `TicketNotFoundException`, not `RuntimeException` |
| `JSON path "$.message" expected:<...> but was:<...>` | The text differs from the exception message | Compare the two texts. The message comes from the constructor of the exception |
| `No value at JSON path "$.message"` | The body has no field with that name | `ApiError` has `status`, `error`, `message`, and `timestamp`. Use one of them |

Next: **[Recap and exercises](09-recap-and-exercises.md)**.
