# Step 4 — Project and connection

> [Back to index](README.md) · Previous: [The package](03-the-package.md) · Next: [Ticket calls](05-ticket-calls.md)

## Goal

A Spring Boot application that starts, with the Oracle connection settings read from environment variables.

## Why this matters

Nothing here is new. The project is set up as in the first walkthrough, so only the names change. Spring Initializr generates a few files and names that this project does not use, so this step also tidies them. If you skip that, the class names and the test count in later steps do not match what you see.

### 4.1 Generate the project

Open Spring Initializr and fill in:

| Field | Value |
|---|---|
| Project | Maven |
| Language | Java |
| Spring Boot | 4.1.1 |
| Group | `com.bsp` |
| Artifact | `support-assistant-plsql-relations` |
| Name | `support-assistant-plsql-relations` |
| Package name | `com.bsp.supportassistant` |
| Packaging | Jar |
| Java | 21 |
| Dependencies | Spring Web, JDBC API, Oracle Driver |

Do **not** add Spring Data JPA. Change the **Package name** by hand: Initializr suggests `com.bsp.support_assistant_plsql_relations`, and every file in this walkthrough uses `com.bsp.supportassistant`. The reason for each dependency is in [step 3 of the first walkthrough](../support-assistant-plsql-walkthrough/03-project-and-connection.md).

Generate the project and put its files in the `support-assistant-plsql-relations` folder from step 2, next to `db`. Open that folder in IntelliJ. In **Settings | Build, Execution, Deployment | Build Tools | Maven**, set **Maven home path** to **Bundled (Maven 3)**.

**Check:** the folder holds `db`, `src`, and `pom.xml` side by side.

Initializr also adds `HELP.md`, `mvnw`, `mvnw.cmd`, and a `.mvn` folder. This walkthrough runs Maven with `mvn` and does not use them. Delete them.

### 4.2 Tidy `pom.xml`

Initializr writes a `pom.xml` that works, with a few extra lines. Change it so that it matches the checkpoint below:

1. Write a description: `<description>Support assistant: tickets and their comments, through an Oracle PL/SQL package. No JPA entities.</description>`.
2. Delete the empty elements `<url/>`, `<licenses>`, `<developers>`, and `<scm>`.
3. Delete the dependency `spring-boot-starter-jdbc-test`. The tests in step 11 use the starter `spring-boot-starter-webmvc-test`, which is already there.
4. Put the dependencies in the order of the checkpoint, and add the comment above each one.

The three dependencies that matter:

| Dependency | Why |
|---|---|
| `spring-boot-starter-webmvc` | Controllers and JSON |
| `spring-boot-starter-jdbc` | The connection pool, `JdbcTemplate`, `SimpleJdbcCall`, and `@Transactional` |
| `ojdbc17` (runtime) | Lets Java talk to Oracle |

The relationship adds no dependency. A foreign key is a rule inside Oracle, and Java does not need to know it exists.

**Check:** in IntelliJ, open the **Maven** tool window and click **Reload All Maven Projects**. No error appears.

### 4.3 Rename the main class

Initializr names the main class after the artifact: `SupportAssistantPlsqlRelationsApplication`. Rename it to `SupportAssistantApplication`, as in the first project. In IntelliJ, put the cursor on the class name and use **Refactor | Rename** (**Shift+F6**). IntelliJ renames the file too.

**Check:** the file is `src/main/java/com/bsp/supportassistant/SupportAssistantApplication.java`, and it matches the checkpoint.

### 4.4 Delete the generated test

Initializr adds `src/test/java/com/bsp/supportassistant/SupportAssistantPlsqlRelationsApplicationTests.java`, with one empty test called `contextLoads`. Delete the file. Step 11 writes the tests of this project in the same folder.

**Check:** `src/test/java/com/bsp/supportassistant` is empty.

### 4.5 The connection

Initializr writes the settings in `src/main/resources/application.properties`. This project uses the YAML form. Delete `application.properties`, and create `src/main/resources/application.yaml`:

```yaml
spring:
  application:
    name: support-assistant-plsql-relations

  datasource:
    # Oracle XE on your own machine. For another database, set DB_URL:
    # jdbc:oracle:thin:@//ORACLE_HOST:1521/ORACLE_SERVICE
    url: ${DB_URL:jdbc:oracle:thin:@//localhost:1521/XEPDB1}
    # Set DB_USERNAME and DB_PASSWORD as environment variables. Do not write a password in this file.
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

`${DB_USERNAME}` reads an environment variable. `${DB_URL:...}` has a default after the colon. Indentation is part of YAML: use two spaces for each level, and no tabs.

**Check:** `src/main/resources` holds `application.yaml` and no `application.properties`.

### 4.6 Create the packages

The classes of the next steps go into five packages. Create them under `src/main/java/com/bsp/supportassistant` (in IntelliJ: right-click the package, **New | Package**):

| Package | What goes in it | Step |
|---|---|---|
| `dto` | The records for requests and answers | 5 to 7, 10 |
| `repository` | `TicketRepository`, which calls the package | 5 to 8 |
| `service` | `TicketService` | 5 to 8, 10 |
| `api` | `TicketController` | 5 to 8, 10 |
| `exception` | `ApiError` and `GlobalExceptionHandler` | 9 |

**Check:** IntelliJ shows the five packages next to `SupportAssistantApplication`. An empty package may be hidden until a class is in it. That is fine.

### 4.7 Set the variables

| Where you run | How |
|---|---|
| IntelliJ | **Run | Edit Configurations | Environment variables**: `DB_USERNAME=YOUR_USERNAME;DB_PASSWORD=YOUR_PASSWORD` |
| Terminal | `export DB_USERNAME=YOUR_USERNAME` and `export DB_PASSWORD=YOUR_PASSWORD` |

If your database is not at `localhost:1521/XEPDB1`, set `DB_URL` the same way: `DB_URL=jdbc:oracle:thin:@//ORACLE_HOST:1521/ORACLE_SERVICE`.

## Try it

Compile the project from a terminal in the project folder:

```bash
mvn -q clean compile
```

**Check:** the command prints nothing and ends without `ERROR`.

Run `SupportAssistantApplication` in IntelliJ, or with Maven:

```bash
mvn spring-boot:run
```

Expected, near the end of the log:

```text
Started SupportAssistantApplication in 1.4 seconds
```

Stop the application (the red square in IntelliJ, or **Ctrl+C** in the terminal) before the next step.

> **Note:** the application starts even when the password is wrong. Nothing connects to Oracle until the first call. A wrong password shows up as `ORA-01017` in step 5.

## Checkpoint

`pom.xml` - Matches [../support-assistant-plsql-relations/pom.xml](../support-assistant-plsql-relations/pom.xml) exactly.

<details>
<summary>Full <code>pom.xml</code></summary>

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.1.1</version>
        <relativePath/>
    </parent>

    <groupId>com.bsp</groupId>
    <artifactId>support-assistant-plsql-relations</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>support-assistant-plsql-relations</name>
    <description>Support assistant: tickets and their comments, through an Oracle PL/SQL package. No JPA entities.</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <!-- Web: REST controllers, JSON, embedded Tomcat -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc</artifactId>
        </dependency>

        <!-- JDBC: the connection pool, JdbcTemplate, SimpleJdbcCall and @Transactional -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>

        <!-- Oracle JDBC driver. Needed when the application runs, not when you compile -->
        <dependency>
            <groupId>com.oracle.database.jdbc</groupId>
            <artifactId>ojdbc17</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

</details>

`src/main/resources/application.yaml` - Matches [../support-assistant-plsql-relations/src/main/resources/application.yaml](../support-assistant-plsql-relations/src/main/resources/application.yaml) exactly.

<details>
<summary>Full <code>src/main/resources/application.yaml</code></summary>

```yaml
spring:
  application:
    name: support-assistant-plsql-relations

  datasource:
    # Oracle XE on your own machine. For another database, set DB_URL:
    # jdbc:oracle:thin:@//ORACLE_HOST:1521/ORACLE_SERVICE
    url: ${DB_URL:jdbc:oracle:thin:@//localhost:1521/XEPDB1}
    # Set DB_USERNAME and DB_PASSWORD as environment variables. Do not write a password in this file.
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

</details>

`src/main/java/com/bsp/supportassistant/SupportAssistantApplication.java` - Matches [../support-assistant-plsql-relations/src/main/java/com/bsp/supportassistant/SupportAssistantApplication.java](../support-assistant-plsql-relations/src/main/java/com/bsp/supportassistant/SupportAssistantApplication.java) exactly.

<details>
<summary>Full <code>src/main/java/com/bsp/supportassistant/SupportAssistantApplication.java</code></summary>

```java
package com.bsp.supportassistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SupportAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupportAssistantApplication.class, args);
    }
}
```

</details>

## Common mistakes

| Symptom | Cause | Fix |
|---|---|---|
| `package org.springframework.jdbc.core does not exist` in the next step | The JDBC starter is missing | Add `spring-boot-starter-jdbc` |
| The application does not start: `Failed to load driver class oracle.jdbc.OracleDriver` | The Oracle driver is missing | Add `ojdbc17` |
| `package com.bsp.supportassistant` is red in a class you copied from a later step | The project was generated with the package that Initializr suggested | Move the classes to `com.bsp.supportassistant` with **Refactor | Move**, or generate the project again with the right **Package name** |
| The application starts, then the first call fails with `ORA-01017` | `DB_USERNAME` or `DB_PASSWORD` is not set in the place where you run, or the password is wrong | Set them in the run configuration, or `export` them in the terminal that starts Maven |

Next: **[Ticket calls](05-ticket-calls.md)**.
