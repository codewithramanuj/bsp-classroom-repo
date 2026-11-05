# support-assistant-plsql

A ticket API for a support desk. Spring Boot receives the request and calls an Oracle PL/SQL package, `SUPPORT_TICKET_PKG`. The rules (allowed status steps, the due date from the priority) are in the package. Java does not repeat them. There is no entity class and no JPA.

The build guide is the [support-assistant-plsql walkthrough](../oracle-pl-sql-spring-boot/support-assistant-plsql-walkthrough/README.md).

## Versions

| Item | Version |
|---|---|
| Java | 17 (set in `pom.xml`) |
| Spring Boot | 4.1.1 |
| Oracle JDBC driver | `ojdbc17` (comes with Spring Boot) |
| Swagger UI (springdoc) | 3.1.1 |
| Oracle Database | 19c, or 21c Express Edition |

## Before you build or run

1. Run the scripts in [db/oracle](db/oracle/), in order: `01-tables.sql`, then `02-package.sql`. `99-drop.sql` removes everything so you can start again. If you created the package before `DELETE_TICKET` was added, run `02-package.sql` again. It uses `CREATE OR REPLACE`, so your tickets stay.
2. Choose the settings file with the `PROFILE` environment variable: `dev` (port 8100) or `qa` (port 8000, the default). The database connection is in `application-dev.yaml` and `application-qa.yaml`.

> **Note:** the package must be created before the first call. If it is missing, the call fails with `PLS-00201`.
## Build and run

```bash
mvn clean verify
mvn spring-boot:run
```

`mvn clean verify` runs the tests in `TicketPackageTests`. They call the package in your database, and each test rolls back.

## Endpoints

The base path is `/api/v1/tickets`. Swagger UI is at `/swagger-ui.html`.

| Method and path | What it does | Calls | Success |
|---|---|---|---|
| `GET /open-count?priority=HIGH` | Number of unfinished tickets with this priority | `OPEN_COUNT` | `200`, a number |
| `GET /` | All tickets. Optional `status` and `priority` filters | `FIND_TICKETS` | `200`, a list of tickets |
| `POST /` | Creates a ticket. It starts as `OPEN` | `CREATE_TICKET` | `201`, `{ "id": 7 }` |
| `PATCH /{id}/status` | Moves a ticket one step forward | `CHANGE_STATUS` | `204`, no body |
| `DELETE /{id}` | Deletes a ticket and its log lines | `DELETE_TICKET` | `204`, no body |
| `POST /bulk-status` | Moves several tickets. All change, or none | `CHANGE_STATUS` for each id | `204`, no body |

Values: `status` is `OPEN`, `IN_PROGRESS`, `RESOLVED` or `CLOSED`. `priority` is `HIGH`, `MEDIUM` or `LOW`. A status in lower case is accepted for `status` filters and status changes.

Request bodies:

```json
// POST /api/v1/tickets
{ "title": "Printer is jammed", "description": "Second floor", "priority": "HIGH" }

// PATCH /api/v1/tickets/7/status
{ "status": "IN_PROGRESS" }

// POST /api/v1/tickets/bulk-status
{ "ticketIds": [7, 8, 9], "status": "IN_PROGRESS" }
```

One ticket in a list:

```json
{ "id": 7, "title": "Printer is jammed", "description": "Second floor", "status": "OPEN",
  "priority": "HIGH", "createdAt": "2026-11-04T10:30:00", "dueBy": "2026-11-05T10:30:00" }
```

### Errors

Every error has the same body:

```json
{ "status": 409, "error": "Conflict", "message": "Ticket 7 is OPEN. The next status is IN_PROGRESS", "timestamp": "2026-11-04T10:31:00" }
```

| Status | When | Oracle error |
|---|---|---|
| `400` | Title missing, priority not `HIGH`, `MEDIUM` or `LOW`, empty `ticketIds`, a missing parameter, or a body that is not valid JSON | `-20001`, `-20002` |
| `404` | The ticket does not exist (for `PATCH` and `DELETE`) | `-20003` |
| `409` | The status change is not allowed (a step skipped, or the ticket is `CLOSED`) | `-20004` |
| `500` | Any other database error | |

### Calling it from React

A browser blocks a page on one address from calling an API on another. `CorsConfig` allows the React app on `http://localhost:5173` (the Vite default) to call `/api/**` with `GET`, `POST`, `PATCH` and `DELETE`. If your React app runs on another port, change the address in `config/CorsConfig.java`.

## Layout

```text
src/main/java/com/bsp/supportassistantplsql
├── SupportAssistantPlsqlApplication.java
├── api/          TicketController
├── service/      TicketService
├── repository/   TicketRepository (SimpleJdbcCall)
├── dto/          CreateTicketRequest, TicketCreated, TicketResponse, StatusRequest, BulkStatusRequest
├── exception/    ApiError, GlobalExceptionHandler, InvalidRequestException
└── config/       AppConfig, CorsConfig
```

## Running on Spring Boot 3.5

Change these four lines in `pom.xml`:

| Item in `pom.xml` | Spring Boot 4.1.1 (this project) | Spring Boot 3.5.x |
|---|---|---|
| Parent version | `4.1.1` | `3.5.16`, or another 3.5.x |
| Web starter | `spring-boot-starter-webmvc` | `spring-boot-starter-web` |
| Test starter | `spring-boot-starter-webmvc-test` | `spring-boot-starter-test` |
| Oracle driver | `ojdbc17` | `ojdbc11` |

The `spring-boot-starter-jdbc-test` dependency has no 3.5 name. Remove it, because `spring-boot-starter-test` already covers it.

## What this project does not do

- It does not check the request fields in Java. The package checks them.
- It has no login. Any caller can change any ticket.
- It has no endpoint to read one ticket by id, and no comments. `FIND_TICKETS` returns a list, so filter that list by id on the client. Comments are in `support-assistant-with-relations`, and pages are in `support-assistant-plsql-pagination`.
- The `priority` filter on `GET /` is applied in Java, after `FIND_TICKETS` returns. The package filters only by status.
- It was compiled, and the web layer was checked with a mocked repository. The tests that call the package were not run, because they need the Oracle database.
