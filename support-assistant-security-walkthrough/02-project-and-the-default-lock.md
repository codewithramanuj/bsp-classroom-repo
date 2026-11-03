# Step 2 — Project and the default lock

> [Back to index](README.md) · Previous: [Concepts and overview](01-concepts-and-overview.md) · Next: [Your own URL rules](03-your-own-url-rules.md)

## Goal

You have a running Spring Boot project with one open URL, and you see that Spring Security already locks it.

## Why this matters

You do not switch security on. Adding the Spring Security library is enough: every URL is locked at once. Seeing this first shows what the library does with no code from you, and what your own code will change.

## 2.1 Create the project

Create a Maven project with these values:

| Item | Value |
|---|---|
| Group | `com.bsp` |
| Artifact | `support-assistant-security` |
| Java | 21 |
| Spring Boot | 4.1.1 |
| Package | `com.bsp.supportassistant` |

You can make the project with Spring Initializr and choose **Spring Web** and **Spring Security**. Or copy the [pom.xml](../support-assistant-security/pom.xml) of the finished project. Either way, the `pom.xml` must hold these four dependencies:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

| Dependency | What it brings |
|---|---|
| `spring-boot-starter-webmvc` | REST controllers, JSON, the embedded Tomcat server |
| `spring-boot-starter-security` | Spring Security: the filter, the password hashing, the URL rules |
| `spring-boot-starter-webmvc-test` | JUnit, Mockito, MockMvc (used in step 6) |
| `spring-boot-starter-security-test` | `httpBasic(...)` for MockMvc (used in step 6) |

The application class is the usual one, in `com.bsp.supportassistant`:

```java
@SpringBootApplication
public class SupportAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupportAssistantApplication.class, args);
    }
}
```

In `src/main/resources/application.yaml`, set the name:

```yaml
spring:
  application:
    name: support-assistant-security
```

**Check:** run `mvn clean package -DskipTests`. You should see `BUILD SUCCESS`. (If the build fails on a missing library, the corporate proxy is the usual cause: see the Day 1 proxy notes.)

## 2.2 One URL to call

Create the package `api` and the class `AccountController` in it:

```java
@RestController
@RequestMapping("/api")
public class AccountController {

    @GetMapping("/public/ping")
    public Map<String, String> ping() {
        return Map.of("status", "up");
    }
}
```

`Map.of("status", "up")` becomes the JSON `{"status":"up"}`. The URL is `/api/public/ping`. The word `public` in the path is only a name. In step 3 you tell Spring Security to treat that part of the path as open.

| Name | Import |
|---|---|
| `RestController`, `RequestMapping`, `GetMapping` | `org.springframework.web.bind.annotation` |
| `Map` | `java.util.Map` |

**Check:** none yet. You run it in 2.3.

## 2.3 Run it and call it

Run the application:

```bash
mvn spring-boot:run
```

Look in the log for this block:

```text
Using generated security password: c8dc9496-2fbb-42de-8cc1-7b81fe479723

This generated password is for development use only. Your security configuration must be updated before running your application in production.
```

You wrote no security code, and Spring Security made a user for you. The user name is `user`. The password is the generated text, and it is **different at every start**.

Call the URL with no login:

```bash
curl -i http://localhost:8080/api/public/ping
```

```text
HTTP/1.1 401
WWW-Authenticate: Basic realm="Realm", charset="UTF-8"
```

The URL is called "public", and it is locked. Spring Security locks **every** URL until you say otherwise. `WWW-Authenticate` tells the client to log in with Basic.

Now call it as `user`, with the password from your log:

```bash
curl -i -u user:c8dc9496-2fbb-42de-8cc1-7b81fe479723 http://localhost:8080/api/public/ping
```

```text
HTTP/1.1 200
{"status":"up"}
```

**Check:** you see `401` without `-u` and `200` with the generated password. Stop the application with `Ctrl+C`.

## Try it

You should now have these files:

```text
support-assistant-security
├── pom.xml
└── src/main
    ├── java/com/bsp/supportassistant
    │   ├── SupportAssistantApplication.java
    │   └── api/AccountController.java
    └── resources/application.yaml
```

## Common mistakes

| Symptom | Cause | Fix |
|---|---|---|
| `401` even with `-u user:...` | The password in the command is from an earlier start | Read the password from the log of the current run. It changes at every start |
| `401` on a URL that has no controller | Spring Security answers before it looks for the controller | Expected. The lock comes first |
| `Port 8080 was already in use` | An earlier run is still going | Stop it, then start again |

Next: **[Your own URL rules](03-your-own-url-rules.md)**.
