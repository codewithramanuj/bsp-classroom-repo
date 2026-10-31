# Step 2 — The starting point

> [Back to index](README.md) · Previous: [Concepts and overview](01-concepts-and-overview.md) · Next: [Plain JUnit](03-plain-junit.md)

## Goal

The given project compiles, you have read the two classes you will test, and the test folders exist.

## Why this matters

You cannot write a good test for code you have not read. A test says what the code **should** do, so you need to know the rules first. It also helps to call the application by hand once. Each call you make by hand is a test that you are about to write down, so that the computer can repeat it.

### 2.1 Check what you were given

Open the project folder. You should have:

```text
support-assistant-testing
├── pom.xml
└── src/main/java/com/bsp/supportassistant
    ├── SupportAssistantApplication.java
    ├── model/         Ticket, StatusRules
    ├── repository/    TicketRepository, InMemoryTicketRepository
    ├── service/       TicketService
    ├── dto/           CreateTicketRequest, StatusRequest, TicketResponse
    ├── exception/     ApiError, GlobalExceptionHandler, and two exceptions
    └── api/           TicketController
└── src/test/java/com/bsp/supportassistant
    ├── model/         StatusRulesTest
    ├── service/       TicketServiceTest
    └── api/           TicketControllerTest
```

The folder `src/test` is there too. You open its files in 2.5.

### 2.2 The test tools are already in `pom.xml`

Open `pom.xml`. Find this dependency:

```xml
        <!-- Test tools: JUnit 5, Mockito, AssertJ, MockMvc -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc-test</artifactId>
            <scope>test</scope>
        </dependency>
```

This one starter brings JUnit 5, Mockito, AssertJ, and MockMvc. `<scope>test</scope>` means these libraries are used when the tests run, and are not in the finished application. You add nothing to `pom.xml`.

**Check:** run `mvn -q compile`. It prints nothing.

### 2.3 Read the code under test

The first class, `StatusRules`. It knows the order of statuses:

```java
package com.bsp.supportassistant.model;

import java.util.List;

// The order of a ticket's life: OPEN, IN_PROGRESS, RESOLVED, CLOSED.
// A ticket moves one step forward at a time. This class uses no Spring and no database.
public final class StatusRules {

    private static final List<String> ORDER = List.of("OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED");

    private StatusRules() {
    }

    public static boolean isAllowed(String from, String to) {
        int position = ORDER.indexOf(from);
        return position >= 0 && position + 1 < ORDER.size() && ORDER.get(position + 1).equals(to);
    }
}
```

The second class, `TicketService`. It holds the rules of the application:

```java
package com.bsp.supportassistant.service;

import com.bsp.supportassistant.dto.CreateTicketRequest;
import com.bsp.supportassistant.dto.TicketResponse;
import com.bsp.supportassistant.exception.InvalidStatusChangeException;
import com.bsp.supportassistant.exception.TicketNotFoundException;
import com.bsp.supportassistant.model.StatusRules;
import com.bsp.supportassistant.model.Ticket;
import com.bsp.supportassistant.repository.TicketRepository;
import org.springframework.stereotype.Service;

// The business rules. This is the class that the Mockito tests look at.
@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    // A new ticket always starts as OPEN. The title is required.
    public TicketResponse create(CreateTicketRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        Ticket saved = ticketRepository.save(new Ticket(null, request.title(), request.priority(), "OPEN"));
        return toResponse(saved);
    }

    public TicketResponse get(long id) {
        return toResponse(find(id));
    }

    // A ticket moves one step forward at a time.
    public TicketResponse changeStatus(long id, String newStatus) {
        Ticket ticket = find(id);
        if (!StatusRules.isAllowed(ticket.status(), newStatus)) {
            throw new InvalidStatusChangeException(ticket.status(), newStatus);
        }
        return toResponse(ticketRepository.save(ticket.withStatus(newStatus)));
    }

    private Ticket find(long id) {
        return ticketRepository.findById(id).orElseThrow(() -> new TicketNotFoundException(id));
    }

    private TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(ticket.id(), ticket.title(), ticket.priority(), ticket.status());
    }
}
```

Read `TicketService` once and answer these in your head. You will write a test for each of them:

| Question | Answer in the code |
|---|---|
| What happens when the title is empty? | `IllegalArgumentException("Title is required")`, and nothing is saved |
| What status does a new ticket get? | `OPEN` |
| What happens when the ticket does not exist? | `TicketNotFoundException` |
| What happens when a step is skipped? | `InvalidStatusChangeException`, and nothing is saved |

### 2.4 Call the application by hand

Build and start it:

```bash
mvn -q clean package
java -jar target/support-assistant-testing-0.0.1-SNAPSHOT.jar
```

`mvn -q clean package` runs the tests, and there are none yet to run. In a second terminal, create a ticket, then try a step that skips two statuses, then ask for a ticket that does not exist:

```bash
curl -i -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{"title":"Printer is jammed","priority":"HIGH"}'

curl -i -X PATCH http://localhost:8080/api/tickets/1/status \
  -H "Content-Type: application/json" -d '{"status":"CLOSED"}'

curl -i http://localhost:8080/api/tickets/99
```

Expected, the first line of each answer and the body:

```text
HTTP/1.1 201
{"id":1,"title":"Printer is jammed","priority":"HIGH","status":"OPEN"}

HTTP/1.1 409
{"status":409,"error":"Conflict","message":"A ticket cannot move from OPEN to CLOSED","timestamp":"..."}

HTTP/1.1 404
{"status":404,"error":"Not Found","message":"Ticket 99 does not exist","timestamp":"..."}
```

Stop the application with `Ctrl+C`. You have just run three tests by hand. The next steps write them so that `mvn test` runs them in seconds.

### 2.5 Open the test files

The three test classes are given to you. Each one is in the **same package** as the class it tests: `StatusRulesTest` in `model`, `TicketServiceTest` in `service`, `TicketControllerTest` in `api`. Open them in the IDE:

| File | What is in it |
|---|---|
| `StatusRulesTest.java` | Two imports and an empty class |
| `TicketServiceTest.java` | The imports for the whole class, `@ExtendWith(MockitoExtension.class)`, and an empty class |
| `TicketControllerTest.java` | The imports, `@WebMvcTest`, and two fields. The class has no test yet |

All the `import` lines are there already. In the IDE, some are grey because nothing uses them yet. They turn normal as you write the tests.

**Check:** run `mvn test`. Among the last lines you should see:

```text
[INFO] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## Try it

You did it in 2.4 and 2.5: the application answers `201`, `409`, and `404`, and `mvn test` reports `Tests run: 0` before any test is written.

## Checkpoint

There is no file to compare in this step. Compare the output of `mvn test` with the lines in 2.5: `Tests run: 0` and `BUILD SUCCESS`.

## Common mistakes

| Symptom | Cause | Fix |
|---|---|---|
| `mvn -q compile` fails with `release version 21 not supported` | Maven runs on an older Java | Run `java -version`. Use Java 21, and in IntelliJ set the project SDK to 21 |
| `curl` says `Connection refused` | The application is not running, or runs on another port | Start the jar again. Check the log line `Tomcat started on port 8080` |
| `mvn test` still prints `Tests run: 0` after you added a test | The method has no `@Test`, or it is outside the braces of the class | Check the annotation and the braces |

Next: **[Plain JUnit](03-plain-junit.md)**.
