# Step 9 — Recap and exercises

> [Back to index](README.md) · Previous: [MockMvc: an error body](08-mockmvc-an-error-body.md) · Next: none

## Quick reference

| Concept | Where it lives |
|---|---|
| A plain test, no mock | `StatusRulesTest`, `@Test`, `assertThat(...)` |
| A fake for a dependency | `TicketServiceTest`, `@Mock TicketRepository` |
| The real class that gets the fakes | `@InjectMocks TicketService` |
| Let Mockito create the mocks | `@ExtendWith(MockitoExtension.class)` |
| Tell a mock what to answer | `when(...).thenReturn(...)`, `when(...).thenThrow(...)` |
| Check a mock was called, or not | `verify(mock)`, `verify(mock, never())` |
| Look at what the service gave to a mock | `ArgumentCaptor`, `captor.getValue()` |
| Check that code throws | `assertThatThrownBy(() -> ...).isInstanceOf(...)` |
| Test only the web layer | `TicketControllerTest`, `@WebMvcTest(TicketController.class)` |
| Send a pretend request | `mockMvc.perform(post(...))`, `get(...)` |
| A mock inside a Spring test | `@MockitoBean` |
| Check the status code and the JSON | `status().isCreated()`, `jsonPath("$.id").value(1)` |
| Run the tests | `mvn test`, or `mvn test -Dtest=TicketServiceTest` for one class |

## Which test for what

| You want to check | Use |
|---|---|
| A rule with no dependencies | Plain JUnit (step 3) |
| A service that calls a repository | JUnit with Mockito (steps 4 to 6) |
| A URL, a status code, or the JSON | MockMvc (steps 7 and 8) |
| That a query or a database program works against the real tables | A test that uses the database. See [support-assistant-hybrid](../support-assistant-hybrid/) |

## Gotchas

| Symptom | Cause | Fix |
|---|---|---|
| `UnnecessaryStubbingException` | A `when(...)` that the code never called | Remove the stub (step 5) |
| `PotentialStubbingProblem` | A stub was set for one argument, and the code used another | Use the same value (step 5) |
| `Wanted but not invoked ... zero interactions` | `verify` was written before the act | Put `verify` after the call (step 6) |
| `Status expected:<201> but was:<415>` | The request has no content type | Add `.contentType(MediaType.APPLICATION_JSON)` (step 7) |
| `package org.springframework.boot.test.autoconfigure.web.servlet does not exist` | The import of Spring Boot 3.5 in a 4.1.1 project | Import `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest` |
| A line `Mockito is currently self-attaching to enable the inline-mock-maker` | A warning from Mockito and the Java version | None needed. It does not fail the build |

## Discussion questions

1. `TicketServiceTest` never starts Spring, and `TicketControllerTest` does. What does each choice cost, and what does each give?
2. In step 6 the stub answers `OPEN` whatever the service does. Why would the test pass without the captor if the service had a fault?
3. The controller test mocks the service, and the service test mocks the repository. What does nobody test in this project?
4. `StatusRulesTest` has no mock. What makes a class easy to test without one?
5. A test passes on the first run. What do you do before you trust it?

## Exercises

Ordered from easiest to hardest. No answers are given here.

1. Add a test to `StatusRulesTest`: `IN_PROGRESS → RESOLVED` is allowed.
2. Add a test to `TicketServiceTest`: `get` of an existing ticket answers its title. You need a `when(ticketRepository.findById(...))`.
3. Add a MockMvc test: `PATCH /api/tickets/1/status` answers `409` when the service throws `InvalidStatusChangeException`.
4. Break `StatusRules` in a different way: make `isAllowed` answer `false` for everything. Which tests fail? Which do not? Put it back.
5. Add a MockMvc test for `GET /api/tickets/1` that answers `200`. Check the `title` in the JSON.
6. Remove everything inside the class `TicketServiceTest`, and write the tests again from memory. Compare with the checkpoint of step 6. Which tests did you forget?

## What's next

The tests in this walkthrough need no database. The next step is a test that uses the real tables and a PL/SQL package, and rolls back after each test. See [support-assistant-hybrid](../support-assistant-hybrid/) and its `TicketHybridTests`.
