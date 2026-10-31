# Step 5 — Stubbing with when

> [Back to index](README.md) · Previous: [A first Mockito test](04-first-mockito-test.md) · Next: [Capturing an argument](06-capturing-an-argument.md)

## Goal

`TicketServiceTest` has three more tests for `changeStatus`: a good step, a skipped step, and a ticket that does not exist. In each, you tell the mock what to answer.

## Why this matters

`changeStatus` first asks the repository for the ticket. Without help, a fresh mock answers with nothing useful. To test the service, **you** decide what the repository answers: "ticket 7 exists and is `OPEN`", or "there is no ticket 99". Telling a mock what to answer is called **stubbing**. It lets you create any situation in one line, including ones that are hard to make with a real database.

### 5.1 A good step

Add this test at the end of the class:

```java
    @Test
    void changeStatusMovesATicketOneStepForward() {
        Ticket open = new Ticket(7L, "VPN drops", "MEDIUM", "OPEN");
        when(ticketRepository.findById(7L)).thenReturn(Optional.of(open));
        when(ticketRepository.save(any(Ticket.class)))
                .thenReturn(new Ticket(7L, "VPN drops", "MEDIUM", "IN_PROGRESS"));

        TicketResponse response = ticketService.changeStatus(7L, "IN_PROGRESS");

        assertThat(response.status()).isEqualTo("IN_PROGRESS");
    }
```

| Line | Part |
|---|---|
| `Ticket open = ...` | **Arrange**: a ticket that is `OPEN` |
| `when(...findById(7L)).thenReturn(Optional.of(open))` | Stub: "when the service asks for ticket 7, answer this ticket" |
| `when(...save(any(Ticket.class))).thenReturn(...)` | Stub: "when the service saves any ticket, answer this one, now `IN_PROGRESS`" |
| `ticketService.changeStatus(7L, "IN_PROGRESS")` | **Act** |
| `assertThat(response.status())...` | **Assert** |

`findById` returns an `Optional`, so the stub answers `Optional.of(open)`. `any(Ticket.class)` means "whatever ticket is given".

### 5.2 A skipped step

Add this test at the end of the class:

```java
    @Test
    void changeStatusRefusesToSkipAStep() {
        when(ticketRepository.findById(7L)).thenReturn(Optional.of(new Ticket(7L, "VPN drops", "MEDIUM", "OPEN")));

        assertThatThrownBy(() -> ticketService.changeStatus(7L, "CLOSED"))
                .isInstanceOf(InvalidStatusChangeException.class);

        verify(ticketRepository, never()).save(any());
    }
```

The ticket is `OPEN`, and the request asks for `CLOSED`. The service must refuse and save nothing. There is no stub for `save`, because it must not be called.

### 5.3 A ticket that does not exist

Add this test at the end of the class:

```java
    @Test
    void changeStatusOfAMissingTicketIsNotFound() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.changeStatus(99L, "IN_PROGRESS"))
                .isInstanceOf(TicketNotFoundException.class);
    }
```

`Optional.empty()` is the repository's way to say "not found".

**Check:** run `mvn test`. You should see `Tests run: 7` (3 + 4) and `BUILD SUCCESS`.

## Try it

```bash
mvn test
```

Expected, among the last lines:

```text
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, ... -- in com.bsp.supportassistant.model.StatusRulesTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, ... -- in com.bsp.supportassistant.service.TicketServiceTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
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

    @Test
    void changeStatusMovesATicketOneStepForward() {
        Ticket open = new Ticket(7L, "VPN drops", "MEDIUM", "OPEN");
        when(ticketRepository.findById(7L)).thenReturn(Optional.of(open));
        when(ticketRepository.save(any(Ticket.class)))
                .thenReturn(new Ticket(7L, "VPN drops", "MEDIUM", "IN_PROGRESS"));

        TicketResponse response = ticketService.changeStatus(7L, "IN_PROGRESS");

        assertThat(response.status()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void changeStatusRefusesToSkipAStep() {
        when(ticketRepository.findById(7L)).thenReturn(Optional.of(new Ticket(7L, "VPN drops", "MEDIUM", "OPEN")));

        assertThatThrownBy(() -> ticketService.changeStatus(7L, "CLOSED"))
                .isInstanceOf(InvalidStatusChangeException.class);

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void changeStatusOfAMissingTicketIsNotFound() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.changeStatus(99L, "IN_PROGRESS"))
                .isInstanceOf(TicketNotFoundException.class);
    }
}
```

</details>

## Common mistakes

| Symptom | Cause | Fix |
|---|---|---|
| `UnnecessaryStubbingException: Unnecessary stubbings detected` | A `when(...)` that the code never called. For example, you added `when(ticketRepository.save(...))` to the "missing ticket" test | Remove the stub. A test must stub only what the code uses |
| `incompatible types` on `thenReturn(open)` | `findById` returns an `Optional`, not a `Ticket` | Use `thenReturn(Optional.of(open))` |
| `PotentialStubbingProblem: Strict stubbing argument mismatch` | The mock was stubbed with one argument (for example `findById(8L)`) and the service called it with another (`7L`) | Use the same value in the stub and in the call |
| `Wanted but not invoked` after `verify(ticketRepository).save(...)` | The service did not call `save` | Read the service. In the skipped-step test it must not, so use `never()` there |

The first row is the output you see when you add `when(ticketRepository.save(any(Ticket.class))).thenReturn(null);` to the last test:

```text
org.mockito.exceptions.misusing.UnnecessaryStubbingException: 
Unnecessary stubbings detected.
Following stubbings are unnecessary (click to navigate to relevant line of code):
  1. -> at com.bsp.supportassistant.service.TicketServiceTest.changeStatusOfAMissingTicketIsNotFound(TicketServiceTest.java:69)
```

Mockito fails the test on purpose. An unused stub often means the test does not check what you think it checks.

Next: **[Capturing an argument](06-capturing-an-argument.md)**.
