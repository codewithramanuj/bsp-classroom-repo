# Step 9 — Error handling

> [Back to index](README.md) · Previous: [Many tickets with comments](08-many-tickets-with-comments.md) · Next: [Transactions](10-transactions.md)

## Goal

Turn the errors that the package raises into clear answers: `400`, `404`, and `409`. Add the two errors that come from the relationship.

## Why this matters

Since step 6, a refused call has been a plain `500`. The client cannot tell a missing ticket from a bug in the server. The package already knows the reason and has put it in the error. One class reads the error number and chooses the status, for every route.

Two of the numbers belong to the relationship:

| Number | Meaning | Answer | Why |
|---|---|---|---|
| `20003` | The ticket does not exist | `404 Not Found` | A comment for a missing parent, or a read of a missing parent |
| `20005` | The author or the text is missing | `400 Bad Request` | The comment is incomplete |
| `20006` | The ticket is `CLOSED` | `409 Conflict` | The request is fine, but the **state of the parent** forbids it |

`409` is the interesting one. The comment is complete and the ticket exists, so `400` and `404` would both be wrong. What is wrong is the state of the parent at this moment.

### 9.1 The error body

In the package `exception`, create `ApiError.java`. Every error has this body:

```java
package com.bsp.supportassistant.exception;

import java.time.LocalDateTime;

// The body of every error response.
public record ApiError(
        int status,
        String error,
        String message,
        LocalDateTime timestamp) {
}
```

### 9.2 The handler class

In the same package, create `GlobalExceptionHandler.java`. A Spring class marked `@RestControllerAdvice` catches exceptions from every controller, like a help desk that every department sends its problems to:

```java
// One place that turns exceptions from every controller into an ApiError.
@RestControllerAdvice
public class GlobalExceptionHandler {
```

The class must be inside `com.bsp.supportassistant` or one of its packages. Spring looks for classes only there.

### 9.3 Catch the database error

A call that `RAISE_APPLICATION_ERROR` refuses reaches Java as an `UncategorizedSQLException`. Inside it is the `SQLException` from the Oracle driver, and `getErrorCode()` gives the number without the minus sign: `20003`. Oracle numbers these errors from 20000 to 20999. Anything else is a fault of the server, so the client gets a general message. Add the method:

```java
    // A PL/SQL program called RAISE_APPLICATION_ERROR. Oracle numbers these from 20000 to 20999.
    // Any other database error is a fault of the server, so the client gets a general message.
    @ExceptionHandler(UncategorizedSQLException.class)
    public ResponseEntity<ApiError> handleDatabase(UncategorizedSQLException ex) {
        SQLException sqlException = ex.getSQLException();
        int code = sqlException.getErrorCode();
        if (code >= 20000 && code <= 20999) {
            return build(statusFor(code), messageOf(sqlException));
        }
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "The database call failed");
    }
```

`@ExceptionHandler` says which exception this method handles. `ResponseEntity` is an answer with a status code and a body.

### 9.4 Choose the status

The table above, as code. Every other number of the package is a problem with the request, so it is `400`:

```java
    // The error numbers are the ones listed at the top of the package specification.
    private HttpStatus statusFor(int oracleCode) {
        if (oracleCode == 20003) {
            return HttpStatus.NOT_FOUND;
        }
        if (oracleCode == 20004 || oracleCode == 20006) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.BAD_REQUEST;
    }
```

### 9.5 Keep the message, drop the rest

Oracle's message has the number at the start, and `ORA-06512` lines after it:

```text
ORA-20003: Ticket 999 does not exist
ORA-06512: at "YOUR_USERNAME.DESK_TICKET_PKG", line 13
```

The client needs only `Ticket 999 does not exist`. Take the first line, and keep the text after the first `": "`:

```java
    // Oracle writes "ORA-20001: Title is required", then lines about where it happened.
    // Keep the text of the first line, after the colon.
    private String messageOf(SQLException sqlException) {
        String firstLine = sqlException.getMessage().lines().findFirst().orElse("");
        return firstLine.substring(firstLine.indexOf(": ") + 2);
    }
```

### 9.6 Build the answer

The last method puts the parts together, and closes the class. `getReasonPhrase()` gives the name of the status, such as `Not Found`:

```java
    private ResponseEntity<ApiError> build(HttpStatus status, String message) {
        ApiError body = new ApiError(status.value(), status.getReasonPhrase(), message, LocalDateTime.now());
        return ResponseEntity.status(status).body(body);
    }
}
```

Add the imports, and compare both files with the checkpoint. No other class changes: the handler works for every route of the controller.

**Check:** run `mvn -q compile`. It prints nothing.

## Try it

Restart the application. A comment for a ticket that does not exist:

```bash
curl -X POST http://localhost:8080/api/tickets/999/comments -H "Content-Type: application/json" \
  -d '{"author":"Meera","body":"Hello"}'
```

Expected, status `404`:

```json
{"status":404,"error":"Not Found","message":"Ticket 999 does not exist","timestamp":"2026-10-17T02:09:45.082018"}
```

A comment with no text, status `400`:

```bash
curl -X POST http://localhost:8080/api/tickets/1/comments -H "Content-Type: application/json" \
  -d '{"author":"Meera"}'
```

```json
{"status":400,"error":"Bad Request","message":"Author and text are required","timestamp":"2026-10-17T02:09:45.101414"}
```

`GET /api/tickets/999` now answers `404` with the same message.

A ticket with no title, status `400`. This one comes from `create_ticket`:

```bash
curl -X POST http://localhost:8080/api/tickets -H "Content-Type: application/json" -d '{"priority":"HIGH"}'
```

```json
{"status":400,"error":"Bad Request","message":"Title is required","timestamp":"2026-10-17T02:09:45.121408"}
```

The package checks the title before it takes a number from the sequence, so this refused call uses no id.

Now close ticket 3. Move it to `RESOLVED`, then to `CLOSED`, then add a comment:

```bash
curl -X PATCH http://localhost:8080/api/tickets/3/status -H "Content-Type: application/json" -d '{"status":"RESOLVED"}'
curl -X PATCH http://localhost:8080/api/tickets/3/status -H "Content-Type: application/json" -d '{"status":"CLOSED"}'
curl -X POST http://localhost:8080/api/tickets/3/comments -H "Content-Type: application/json" \
  -d '{"author":"Meera","body":"Thanks"}'
```

Ticket 3 was moved to `IN_PROGRESS` in step 5, so the two `PATCH` calls take it to `CLOSED`. Both print nothing. The comment is refused, status `409`:

```json
{"status":409,"error":"Conflict","message":"Ticket 3 is CLOSED and takes no comments","timestamp":"2026-10-17T02:09:45.775799"}
```

A step that skips ahead gets the same status. Ticket 2 is `OPEN`, so `CLOSED` is not its next status:

```bash
curl -X PATCH http://localhost:8080/api/tickets/2/status -H "Content-Type: application/json" -d '{"status":"CLOSED"}'
```

```json
{"status":409,"error":"Conflict","message":"Ticket 2 is OPEN. The next status is IN_PROGRESS","timestamp":"2026-10-17T02:09:45.793014"}
```

## Checkpoint

`src/main/java/com/bsp/supportassistant/exception/ApiError.java` - Matches [../support-assistant-plsql-relations/src/main/java/com/bsp/supportassistant/exception/ApiError.java](../support-assistant-plsql-relations/src/main/java/com/bsp/supportassistant/exception/ApiError.java) exactly.

<details>
<summary>Full <code>src/main/java/com/bsp/supportassistant/exception/ApiError.java</code></summary>

```java
package com.bsp.supportassistant.exception;

import java.time.LocalDateTime;

// The body of every error response.
public record ApiError(
        int status,
        String error,
        String message,
        LocalDateTime timestamp) {
}
```

</details>

`src/main/java/com/bsp/supportassistant/exception/GlobalExceptionHandler.java` - Matches [../support-assistant-plsql-relations/src/main/java/com/bsp/supportassistant/exception/GlobalExceptionHandler.java](../support-assistant-plsql-relations/src/main/java/com/bsp/supportassistant/exception/GlobalExceptionHandler.java) exactly.

<details>
<summary>Full <code>src/main/java/com/bsp/supportassistant/exception/GlobalExceptionHandler.java</code></summary>

```java
package com.bsp.supportassistant.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.time.LocalDateTime;

// One place that turns exceptions from every controller into an ApiError.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // A PL/SQL program called RAISE_APPLICATION_ERROR. Oracle numbers these from 20000 to 20999.
    // Any other database error is a fault of the server, so the client gets a general message.
    @ExceptionHandler(UncategorizedSQLException.class)
    public ResponseEntity<ApiError> handleDatabase(UncategorizedSQLException ex) {
        SQLException sqlException = ex.getSQLException();
        int code = sqlException.getErrorCode();
        if (code >= 20000 && code <= 20999) {
            return build(statusFor(code), messageOf(sqlException));
        }
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "The database call failed");
    }

    // The error numbers are the ones listed at the top of the package specification.
    private HttpStatus statusFor(int oracleCode) {
        if (oracleCode == 20003) {
            return HttpStatus.NOT_FOUND;
        }
        if (oracleCode == 20004 || oracleCode == 20006) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.BAD_REQUEST;
    }

    // Oracle writes "ORA-20001: Title is required", then lines about where it happened.
    // Keep the text of the first line, after the colon.
    private String messageOf(SQLException sqlException) {
        String firstLine = sqlException.getMessage().lines().findFirst().orElse("");
        return firstLine.substring(firstLine.indexOf(": ") + 2);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message) {
        ApiError body = new ApiError(status.value(), status.getReasonPhrase(), message, LocalDateTime.now());
        return ResponseEntity.status(status).body(body);
    }
}
```

</details>

## Common mistakes

| Symptom | Cause | Fix |
|---|---|---|
| Still `500` for a package error | `@RestControllerAdvice` is missing, or the class is outside the application's package | Put the class under `com.bsp.supportassistant` |
| A new rule answers `400`, and you wanted `409` | The new number is not in `statusFor` | Add it. Keep the list in step with the comment at the top of the package |
| The message reads `ORA-20003: Ticket 999 does not exist` | `messageOf` was not used, so the whole Oracle text is shown | Cut the text after the first `: ` |
| The client sees SQL text for an error such as `ORA-02291` | Every database error was passed on | Pass on only 20000 to 20999. Give the rest a general message |

Next: **[Transactions](10-transactions.md)**.
