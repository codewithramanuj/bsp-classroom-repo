# support-assistant-jpa-h2

The ticket API of [support-assistant-plsql](../support-assistant-plsql/), built on **Spring Data JPA** and an **in-memory H2 database**. It needs no Oracle and no installation, so you can run it on your own machine and build the React app against it.

The address, the request bodies, the answers, and the error bodies are the same as in the Oracle project. Switch your React app between the two by changing only the server address. The controller, the records, and the error handler are the same files (only the package name differs).

> **Note:** this project is a stand-in for local testing. The course database is Oracle, and the rules of the Oracle version are in the PL/SQL package. Here the same rules are written in `TicketService`, because there is no package.

## Versions

| Item | Version |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.1 |
| H2 | 2.4.240 (comes with Spring Boot) |
| Swagger UI (springdoc) | 3.1.1 |

## Build and run

```bash
mvn clean verify
mvn spring-boot:run
```

The API is at `http://localhost:8080/api/v1/tickets`. Swagger UI is at `http://localhost:8080/swagger-ui.html`. To use another port, change `server.port` in `application.yaml`.

The database is created at every start from [schema.sql](src/main/resources/schema.sql), and filled with six sample tickets from [data.sql](src/main/resources/data.sql). When you stop the application, the data is gone.

## Endpoints

| Method and path | What it does | Success |
|---|---|---|
| `GET /api/v1/tickets/open-count?priority=HIGH` | Number of unfinished tickets with this priority | `200`, a number |
| `GET /api/v1/tickets` | All tickets. Optional `status` and `priority` filters | `200`, a list of tickets |
| `POST /api/v1/tickets` | Creates a ticket. It starts as `OPEN` | `201`, `{ "id": 7 }` |
| `PATCH /api/v1/tickets/{id}/status` | Moves a ticket one step forward | `204`, no body |
| `POST /api/v1/tickets/bulk-status` | Moves several tickets. All change, or none | `204`, no body |

The request bodies, the ticket JSON, and the error table are described in the [README of the Oracle project](../support-assistant-plsql/README.md#endpoints). They are the same.

## The rules (in `TicketService`)

| Rule | Where |
|---|---|
| A new ticket starts `OPEN`. The title is required. The priority is `HIGH`, `MEDIUM` or `LOW` | `createTicket` |
| Due date: `HIGH` 1 day, `MEDIUM` 3 days, `LOW` 7 days after the creation time | `createTicket` |
| A status moves one step: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`. A `CLOSED` ticket cannot change | `changeStatus` |
| A bulk change is all or nothing | `changeStatus(List, String)` with `@Transactional` |

## Layout

```text
src/main/java/com/bsp/supportassistant
├── SupportAssistantApplication.java
├── api/          TicketController
├── service/      TicketService (the rules)
├── repository/   TicketRepository (extends JpaRepository)
├── model/        Ticket (the entity)
├── mapper/       TicketMapper (entity to TicketResponse, by hand)
├── dto/          CreateTicketRequest, TicketCreated, TicketResponse, StatusRequest, BulkStatusRequest
├── exception/    ApiError, GlobalExceptionHandler, and one exception for each error
└── config/       CorsConfig
```

## Tests

`mvn clean verify` runs 12 tests on H2: 10 call the endpoints, and 2 check the rules. One test is not `@Transactional` on purpose. It shows that a failed bulk change really undoes the tickets before it.

## Running on Spring Boot 3.5

| Item in `pom.xml` | Spring Boot 4.1.1 (this project) | Spring Boot 3.5.x |
|---|---|---|
| Parent version | `4.1.1` | `3.5.16`, or another 3.5.x |
| Web starter | `spring-boot-starter-webmvc` | `spring-boot-starter-web` |
| Test starter | `spring-boot-starter-webmvc-test` | `spring-boot-starter-test` |

This was built and tested on Spring Boot 4.1.1 only.

## What this project does not do

- It does not use Oracle, so nothing here tests the PL/SQL package. A rule changed in `TicketService` is not changed in the package, and the other way round.
- It does not lock a row before a status change. Two callers who change the same ticket at the same moment can both succeed.
- It does not keep data between runs, and it has no log of status changes.
- It has no login. Any caller can change any ticket.
