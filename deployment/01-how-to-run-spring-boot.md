# Choose How to Run the API

**Time: about 20 minutes**

Back to the [Day 8 index](README.md) · Next: [Build the JAR](02-build-the-jar.md)

## What you will learn

- What the embedded Tomcat is, and what a separate Tomcat would add.
- Why this course runs the JAR.
- What to check with your server team before you start.

---

## The ideas

### The embedded server

Tomcat is a program that receives web requests and hands them to your Java code. A Spring Boot application carries its own copy of Tomcat inside the JAR file. When you run `java -jar`, the application starts Tomcat itself. This is called the **embedded** Tomcat. The one file you copy is the whole application.

You have seen it in every console log: the line `Tomcat initialized with port 8080 (http)` is the embedded Tomcat starting.

### The other way: a WAR on a separate Tomcat

A WAR file is an application packed to be placed in a Tomcat that is already installed on the server. Someone installs, patches and starts Tomcat, and you copy the WAR into its `webapps/` folder. Several applications can share one Tomcat.

### Comparing the two

| | JAR with embedded Tomcat | WAR on a separate Tomcat |
|---|---|---|
| What you copy | One JAR | One WAR, into an installed Tomcat |
| Start command | `java -jar helpdesk-api.jar` | Start Tomcat |
| Tomcat version | The one you tested with, inside the JAR | Whatever is installed on the server |
| Settings | `application.yaml` and environment variables | Tomcat files such as `server.xml` and `setenv.sh`, plus the application |
| Changes to your project | None | `packaging` set to `war`, plus a `SpringBootServletInitializer` class |
| If one application crashes | Only that process stops | Every application in that Tomcat is affected |

**This course runs the JAR.** Your project is already built this way, and it needs no change.

Use a WAR only when your organization already runs a managed Tomcat and requires every application to go there. Ask your server team this before Day 8:

1. May we run our own `java -jar` process on the server?
2. Which port may the API use? (These notes use 8080.)
3. Which Java version is installed? (These notes need Java 21.)

> **Warning:** Do not run the JAR on the server by hand in a terminal and leave it there. When you close the terminal or the server restarts, the API stops. Step 3 runs it as a service.

---

## Practice

1. On your machine, build the project with `mvn -q clean package -DskipTests` and run `java -jar target/support-assistant-security-database-0.0.1-SNAPSHOT.jar`. **Result:** the console shows `Tomcat initialized with port 8080 (http)`. This line is Tomcat starting inside the JAR. If the API cannot reach Oracle or has no `JWT_SECRET`, it stops a moment later with an error. That is fine for now, and step 2 fixes it.
2. Open `pom.xml` and find the `spring-boot-maven-plugin`. **Result:** you can say in one sentence what it does (it repackages the JAR so that it carries its libraries and Tomcat).

---

## Recap

- A Spring Boot JAR carries its own Tomcat. `java -jar` is all it takes to start.
- A separate Tomcat with a WAR is for organizations that already run one and require it.
- Ask your server team about permission, port and Java version before you start.
