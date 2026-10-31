# Step 6 — Capturing an argument

> [Back to index](README.md) · Previous: [Stubbing with when](05-stubbing-with-when.md) · Next: [MockMvc: a request](07-mockmvc-a-request.md)

## Goal

`TicketServiceTest` has its fifth test: a new ticket is saved with the status `OPEN`. You check **what the service gave to `save`**.

## Why this matters

In `create`, the service builds a `Ticket` with the status `OPEN` and hands it to `save`. The mock then answers with whatever you stubbed. If you only look at the answer, you see your own stub, not the service's work. The thing worth checking is the ticket **that went in**.

An `ArgumentCaptor` catches the argument that the service gave to the mock, so the test can look at it.

### 6.1 The test

Add this test as the **first** test in the class, above `createWithoutATitleSavesNothing`:

```java
    @Test
    void createSavesANewTicketAsOpen() {
        // Arrange: tell the mock what to answer when save is called.
        when(ticketRepository.save(any(Ticket.class)))
                .thenReturn(new Ticket(1L, "Printer is jammed", "HIGH", "OPEN"));

        // Act
        TicketResponse response = ticketService.create(new CreateTicketRequest("Printer is jammed", "HIGH"));

        // Assert: the answer, and what the service gave to save.
        assertThat(response.id()).isEqualTo(1L);
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo("OPEN");
    }
```

| Line | What it does |
|---|---|
| `when(...save...).thenReturn(...)` | The mock answers with a ticket that has an id. The real repository would give it one |
| `response.id()` is `1` | The service passes the saved ticket back |
| `ArgumentCaptor.forClass(Ticket.class)` | Makes a catcher for `Ticket` arguments |
| `verify(ticketRepository).save(captor.capture())` | Checks that `save` was called once, and catches the argument |
| `captor.getValue().status()` | The ticket the service **built**. Its status must be `OPEN` |

**Check:** run `mvn test`. You should see `Tests run: 8` (3 + 5) and `BUILD SUCCESS`.

### 6.2 See it fail

Open `src/main/java/com/bsp/supportassistant/service/TicketService.java`. In `create`, change `"OPEN"` to `"NEW"`:

```java
        Ticket saved = ticketRepository.save(new Ticket(null, request.title(), request.priority(), "NEW"));
```

Run `mvn test`. You should see:

```text
[ERROR] Tests run: 5, Failures: 1, Errors: 0, Skipped: 0, ... <<< FAILURE! -- in com.bsp.supportassistant.service.TicketServiceTest
[ERROR]   TicketServiceTest.createSavesANewTicketAsOpen:49 
expected: "OPEN"
 but was: "NEW"
```

Only the captor test notices. The stubbed answer says `OPEN` whatever the service does, so without the captor this mistake would pass. **Put `"OPEN"` back** and run `mvn test` again.

## Try it

```bash
mvn test
```

Expected, among the last lines:

```text
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, ... -- in com.bsp.supportassistant.model.StatusRulesTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, ... -- in com.bsp.supportassistant.service.TicketServiceTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
```

You may also see a line that starts with `Mockito is currently self-attaching`. It is a warning from Mockito and the Java version. It does not fail the build.

## Checkpoint

`src/test/java/com/bsp/supportassistant/service/TicketServiceTest.java` - This is the finished file.

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
    void createSavesANewTicketAsOpen() {
        // Arrange: tell the mock what to answer when save is called.
        when(ticketRepository.save(any(Ticket.class)))
                .thenReturn(new Ticket(1L, "Printer is jammed", "HIGH", "OPEN"));

        // Act
        TicketResponse response = ticketService.create(new CreateTicketRequest("Printer is jammed", "HIGH"));

        // Assert: the answer, and what the service gave to save.
        assertThat(response.id()).isEqualTo(1L);
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo("OPEN");
    }

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
| `TooManyActualInvocations` | `save` was called more times than `verify` expects. `verify(mock)` means exactly once | Read the lines that list the calls. `create` calls `save` once |
| `Wanted but not invoked ... Actually, there were zero interactions with this mock` | The `verify` line is before the call to the service, so nothing has happened yet | Put `verify(...)` after the act |
| The test passes when you change `"OPEN"` in the service | The test checks the stub's answer, not the captured ticket | Assert on `captor.getValue()` |

Next: **[MockMvc: a request](07-mockmvc-a-request.md)**.
