# Step 4 — A first Mockito test

> [Back to index](README.md) · Previous: [Plain JUnit](03-plain-junit.md) · Next: [Stubbing with when](05-stubbing-with-when.md)

## Goal

`TicketServiceTest` has a fake `TicketRepository` and a real `TicketService`, and one test passes: a ticket without a title is refused and nothing is saved.

## Why this matters

`TicketService` calls `TicketRepository`. Today the real repository keeps tickets in memory. Tomorrow it may talk to a database. A test of the **service** should not care. If it used the real repository, a fault in the repository could make a service test fail, and you would look in the wrong place.

A **mock** removes that problem. It stands in for the repository, so the test looks at the service alone. A mock also remembers how it was called. That lets a test check the things that leave no trace in the answer, such as "nothing was saved".

### 4.1 The mock and the real class

Open `src/test/java/com/bsp/supportassistant/service/TicketServiceTest.java`. The file has the imports and the line above the class that makes Mockito work:

```java
// Test 2: JUnit with Mockito. The service is real. The repository is a mock,
// so the test needs no Spring and no database.
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

}
```

Inside the class, add the fake repository and the real service:

```java
    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketService ticketService;
```

| Part | What it does |
|---|---|
| `@ExtendWith(MockitoExtension.class)` | Already in the file. It tells JUnit to let Mockito create the mocks before each test |
| `@Mock` | Makes a fake `TicketRepository`. The fake is new for each test |
| `@InjectMocks` | Makes a **real** `TicketService` and gives it the fake through its constructor |

No Spring starts. The test creates the service itself, so it is fast.

### 4.2 The first test

Add this test inside the class:

```java
    @Test
    void createWithoutATitleSavesNothing() {
        assertThatThrownBy(() -> ticketService.create(new CreateTicketRequest(" ", "HIGH")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Title is required");

        verify(ticketRepository, never()).save(any());
    }
```

| Part | What it does |
|---|---|
| `assertThatThrownBy(() -> ...)` | Runs the code in the brackets and expects it to throw |
| `.isInstanceOf(...)`, `.hasMessage(...)` | Checks which exception, and its message |
| `verify(ticketRepository, never()).save(any())` | Asks the mock: "was `save` called with anything?" The answer must be no |

The title is a blank space, so the service must refuse it. The last line checks the part that the answer cannot show: the refused ticket was **not saved**.

**Check:** run `mvn test`. You should see `Tests run: 4` (3 from step 3, 1 new) and `BUILD SUCCESS`.

## Try it

Run the new class alone:

```bash
mvn test -Dtest=TicketServiceTest
```

Expected, among the last lines:

```text
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, ... -- in com.bsp.supportassistant.service.TicketServiceTest
[INFO] BUILD SUCCESS
```

Then run all of them with `mvn test`:

```text
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, ... -- in com.bsp.supportassistant.model.StatusRulesTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, ... -- in com.bsp.supportassistant.service.TicketServiceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

## Checkpoint

`src/test/java/com/bsp/supportassistant/service/TicketServiceTest.java` - Grows in a later step.

<details>
<summary>Full <code>src/test/java/com/bsp/supportassistant/service/TicketServiceTest.java</code></summary>

```java
package com.bsp.supportassistant.service;

import com.bsp.supportassistant.dto.CreateTicketRequest;
import com.bsp.supportassistant.dto.TicketResponse;
import com.bsp.supportassistant.exception.InvalidStatusChangeException;
import com.bsp.supportassistant.exception.TicketNotFoundException;
import com.bsp.supportassistant.model.Ticket;
import com.bsp.supportassistant.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Test 2: JUnit with Mockito. The service is real. The repository is a mock,
// so the test needs no Spring and no database.
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void createWithoutATitleSavesNothing() {
        assertThatThrownBy(() -> ticketService.create(new CreateTicketRequest(" ", "HIGH")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Title is required");

        verify(ticketRepository, never()).save(any());
    }
}
```

</details>

## Common mistakes

| Symptom | Cause | Fix |
|---|---|---|
| `NullPointerException: ... because "this.ticketService" is null` | `@ExtendWith(MockitoExtension.class)` is missing, so `@Mock` and `@InjectMocks` never ran | Add it above the class |
| You are not sure the test can fail | A test that has never failed has proved nothing | In `TicketService.create`, comment out the `throw`. Run the test and see it fail. Put the code back |
| You mocked `TicketService` | `@Mock` is for what the class **uses**. The class under test is real | Use `@InjectMocks` for the class under test |

Next: **[Stubbing with when](05-stubbing-with-when.md)**.
