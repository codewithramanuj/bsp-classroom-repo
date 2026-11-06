# Build the JAR and Run It with Settings from Outside

**Time: about 45 minutes**

Back to the [Day 8 index](README.md) · Previous: [Choose how to run the API](01-how-to-run-spring-boot.md) · Next: [Run the API as a service](03-run-as-a-service.md)

You need `support-assistant-security-database` from the security day, and an Oracle user with the two scripts in `db/oracle/` already run.

## What you will build

- One JAR file that holds the whole API.
- A health address, `/actuator/health`, that tells you the API is running.
- A way to give the database login and the JWT secret from outside the JAR, so the same JAR works on any machine.

---

## The ideas

### An executable JAR

`mvn package` runs the `spring-boot-maven-plugin` that is already in your `pom.xml`. The plugin puts your classes, every library, and Tomcat into one file in `target/`. You start it with `java -jar`.

### Settings from outside

Right now the database login is written in `application.yaml`, inside the JAR. A server needs its own login, and a password must not travel inside a file that people copy around.

Spring Boot reads **environment variables** as well as `application.yaml`, and an environment variable **wins**. The name is the setting name in capitals, with `_` in place of `.` and `-`:

| Setting in `application.yaml` | Environment variable |
|---|---|
| `spring.datasource.url` | `SPRING_DATASOURCE_URL` |
| `spring.datasource.username` | `SPRING_DATASOURCE_USERNAME` |
| `spring.datasource.password` | `SPRING_DATASOURCE_PASSWORD` |
| `server.port` | `SERVER_PORT` |
| `server.address` | `SERVER_ADDRESS` |
| `app.jwt.secret` is `${JWT_SECRET}` | `JWT_SECRET` |

You do not edit the code. The values in `application.yaml` stay as the defaults for your machine, and the server's environment replaces them.

### A health address

Spring Boot Actuator adds ready-made addresses for running applications. You use one of them, `/actuator/health`. It answers `{"status":"UP"}` when the API can reach its database. Only `health` is open to the web by default.

---

## Build steps

### 1. Add Actuator

In `pom.xml`, add this inside `<dependencies>`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### 2. Let the health address through the login rules

Without a rule, every address except `/api/public/**` needs a token, including the health check. In `SecurityConfig`, add one line at the top of the rules:

```java
.authorizeHttpRequests(rules -> rules
        .requestMatchers("/actuator/health").permitAll()
        .requestMatchers("/api/public/**").permitAll()
        // ... the other rules stay as they are
```

The line must come before `.anyRequest().authenticated()`. The first rule that matches wins.

### 3. Build the JAR

```bash
mvn -q clean package -DskipTests
```

The tests call Oracle. You ran them on Day 7. If your machine can reach Oracle, leave out `-DskipTests` and run `mvn -q clean verify`.

**Check:** `ls target/*.jar` shows `support-assistant-security-database-0.0.1-SNAPSHOT.jar`. It is about 65 MB, because it carries Tomcat and all the libraries.

### 4. Make a JWT secret

The secret signs every token. It must be at least 32 characters. Make a long random one:

```bash
openssl rand -base64 48
```

**Check:** the command prints one line of 64 characters. Keep it for the next step. Do not put it in a file in the repository.

### 5. Run the JAR with settings from outside

Set the variables in the terminal, then start the JAR. Use your own values for the capitals:

```bash
export SPRING_DATASOURCE_URL='jdbc:oracle:thin:@//ORACLE_HOST:ORACLE_PORT/ORACLE_SERVICE'
export SPRING_DATASOURCE_USERNAME='DB_USERNAME'
export SPRING_DATASOURCE_PASSWORD='DB_PASSWORD'
export JWT_SECRET='the line from step 4'
java -jar target/support-assistant-security-database-0.0.1-SNAPSHOT.jar
```

**Check:** the console ends with a line like `Started SupportAssistantApplication in 6.2 seconds`.

### 6. Call the health address

In a second terminal:

```bash
curl http://localhost:8080/actuator/health
```

**Check:** you see `{"status":"UP"}`. Stop the API with `Ctrl+C`.

### 7. See the settings win over the file

Start the JAR again with a port that is not in `application.yaml`:

```bash
SERVER_PORT=8090 java -jar target/support-assistant-security-database-0.0.1-SNAPSHOT.jar
```

**Check:** the console shows `Tomcat initialized with port 8090 (http)`. The environment variable replaced the default. Stop the API with `Ctrl+C`.

---

## Common mistakes

> **Warning: `JWT_SECRET` not set.** The API stops at start with ``Could not resolve placeholder 'JWT_SECRET' in value "${JWT_SECRET}"``. Set the variable in the same terminal that runs `java -jar`, or in the service's settings file in step 3 of the next note.

> **Warning: a wrong database address.** The API stops with ``Unable to obtain isolated JDBC connection [ORA-12541: Cannot connect. No listener at host … port …``. The host or the port is wrong, or a firewall blocks it. A wrong password gives `ORA-01017`. Try the same login from your machine in SQL Developer first.

> **Warning: the health address answers `401`.** The rule from step 2 is missing, or it is below `.anyRequest().authenticated()`.

> **Warning: quotes or spaces in the wrong place.** `export JWT_SECRET = abc` fails in the shell. Write `export JWT_SECRET='abc'` with no spaces around `=`.

---

## Practice

1. Change `expiry-minutes` in `application.yaml` to `1`, rebuild, log in with `curl`, wait two minutes, and use the token. **Result:** the API answers `401`. Change it back to `30`.
2. Start the JAR with `SERVER_PORT=8091` and call `/actuator/health` on that port. **Result:** `{"status":"UP"}` on 8091, and nothing on 8080.
3. Start the JAR with a wrong `SPRING_DATASOURCE_PASSWORD`. **Result:** the API stops and the console shows `ORA-01017`.

---

## Recap

- `mvn package` makes one executable JAR with Tomcat inside. `java -jar` starts it.
- Environment variables override `application.yaml`. The same JAR runs on your machine and on the server with different logins.
- The database password and the JWT secret never go into the repository.
- Actuator gives `/actuator/health`. Open that one address in `SecurityConfig`, and keep everything else behind the login.

> **Not verified:** the JAR was built on macOS with Java 25 as the build JDK, Maven 3.9.11 and Spring Boot 4.1.1, in a copy of the project with the Actuator dependency added. The manifest says `Java-Version: 21`. The JAR started and used `SERVER_PORT` and `SPRING_DATASOURCE_URL` from the environment: with a wrong address it printed `ORA-12541 … host 127.0.0.1 port 1`, and with `SERVER_PORT=8099` it printed `Tomcat initialized with port 8099 (http)`. Without `JWT_SECRET` it stopped with the message quoted above. Oracle was not available, so the steps that need a working database (`Started …`, `{"status":"UP"}`, the `401` on the health address without the rule, `ORA-01017`) were not run. `SERVER_ADDRESS` was not tried. The `SecurityConfig` line was not compiled or run. The practice tasks were not run.
