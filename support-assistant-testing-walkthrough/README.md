# Walkthrough: support-assistant-testing

Write the tests of a small Spring Boot ticket API, one kind of test at a time. The main code is given to you. You write only the tests: a plain JUnit test, a Mockito test, and a MockMvc test. No database is needed.

**What you are building:** the tests of [support-assistant-testing](../support-assistant-testing/), described in its [README](../support-assistant-testing/README.md).

## What you will build

Three test classes, ten tests:

| Test class | Tests | What it checks | Tools |
|---|---|---|---|
| `StatusRulesTest` | 3 | The order of ticket statuses | JUnit, AssertJ |
| `TicketServiceTest` | 5 | The service, with a fake repository | JUnit, Mockito, AssertJ |
| `TicketControllerTest` | 2 | The URL, the status code, and the JSON of the controller | MockMvc, Mockito |

- Each test follows three parts: arrange, act, assert.
- You break the code on purpose to see a test fail.
- All ten tests run in a few seconds, on your machine.

## Prerequisites

- Java 21 and Maven.
- The project `support-assistant-testing`, as it is given to you: `pom.xml`, everything under `src/main`, and three test classes under `src/test` that hold only their imports and annotations. This walkthrough does not build the main code. You write the tests.
- A Spring Boot application with a controller, a service, and a repository, as in [Day 2](../support-assistant/README.md).

Time: about 2 hours 20 minutes.

## Steps

| Step | File | What you add | Est. time |
|---|---|---|---|
| 1 | [01-concepts-and-overview.md](01-concepts-and-overview.md) | The words used in every step: unit test, mock, stub, MockMvc | 10 min |
| 2 | [02-the-starting-point.md](02-the-starting-point.md) | Check the given project, read the code under test, open the three test files | 10 min |
| 3 | [03-plain-junit.md](03-plain-junit.md) | `StatusRulesTest`: three tests with no mock. Break the code to see one fail | 15 min |
| 4 | [04-first-mockito-test.md](04-first-mockito-test.md) | `TicketServiceTest`: the `@Mock` and `@InjectMocks` fields, `verify`, `never()` | 20 min |
| 5 | [05-stubbing-with-when.md](05-stubbing-with-when.md) | `when(...).thenReturn(...)`: three tests for `changeStatus` | 25 min |
| 6 | [06-capturing-an-argument.md](06-capturing-an-argument.md) | `ArgumentCaptor`: check what the service gave to `save` | 15 min |
| 7 | [07-mockmvc-a-request.md](07-mockmvc-a-request.md) | `TicketControllerTest`: what `@WebMvcTest`, `MockMvc` and `@MockitoBean` do, and a `POST` test | 20 min |
| 8 | [08-mockmvc-an-error-body.md](08-mockmvc-an-error-body.md) | A `404` and its JSON error body | 10 min |
| 9 | [09-recap-and-exercises.md](09-recap-and-exercises.md) | Quick reference, gotchas, exercises | 15 min |

## How each step is built

- **Goal** and **Why this matters** say what the step adds, and why.
- The numbered parts are the build steps. Each one says which file to open, and where the new code goes. The imports are already in the files, so you type no `import` line. Most end with a **Check** line that you run before you go on.
- **Try it** runs the tests, with the lines to expect.
- **Checkpoint** gives the complete test file after the step. Compare yours with it before you go on.
- **Common mistakes** lists what goes wrong in this step, as the tools print it.

## The files you start with

The three test classes are in the project already. They hold the imports for the whole walkthrough, so you type no `import` line. IntelliJ shows an import in grey until the code that uses it is written.

| File | What it holds at the start |
|---|---|
| `src/test/java/com/bsp/supportassistant/model/StatusRulesTest.java` | The imports and an empty class |
| `src/test/java/com/bsp/supportassistant/service/TicketServiceTest.java` | The imports and `@ExtendWith(MockitoExtension.class)` on an empty class |
| `src/test/java/com/bsp/supportassistant/api/TicketControllerTest.java` | The imports, `@WebMvcTest`, and the two fields `mockMvc` and `ticketService` |

With these files, `mvn test` reports `Tests run: 0` and `BUILD SUCCESS`.

The **Checkpoint** of each step is the complete file as it should look after that step. The last checkpoint of each file is the finished test class:

| File | Finished in step |
|---|---|
| `StatusRulesTest.java` | 3 |
| `TicketServiceTest.java` | 6 |
| `TicketControllerTest.java` | 8 |

`TicketServiceTest.java` grows over steps 4 to 6, and `TicketControllerTest.java` over steps 7 and 8. Each step shows the whole file as it stands then, and says "Grows in a later step" until the last one.

The build was checked step by step: the checkpoint of each step was put in a copy of the project and `mvn test` was run. The number of tests at the end of each step (3, 4, 7, 8, 9, 10) and every failure message quoted in full in this walkthrough are the real output. It was built and tested on Spring Boot 4.1.1 only.

Start with **[Step 1 — Concepts and overview](01-concepts-and-overview.md)**.
