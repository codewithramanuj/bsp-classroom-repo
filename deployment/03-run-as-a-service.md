# Run the API as a Service

**Time: about 35 minutes**

Back to the [Day 8 index](README.md) · Previous: [Build the JAR](02-build-the-jar.md) · Next: [Build the React app](04-build-the-react-app.md)

You need the JAR from step 2, and a Linux server with `sudo` for you and Java 21 installed. These steps use `systemd`, which starts programs at boot on most Linux servers.

## What you will build

- A user on the server that only runs the API.
- A settings file that holds the database login and the JWT secret.
- A `systemd` service, `helpdesk-api`, that starts the JAR at boot and starts it again if it stops.

---

## The ideas

### A service

A service is a program that the operating system starts and watches for you. `systemd` reads a small text file, called a **unit file**, that says what to run, as which user, and what to do when it stops. After that you control the program with `systemctl start`, `stop`, `restart` and `status`.

### A settings file

You gave the settings by `export` in step 2. A service has no terminal, so the settings go in a file. The service reads the file when it starts. The file holds secrets, so only the administrator may read it.

---

## Build steps

### 1. Check Java and copy the JAR

On the server:

```bash
java -version
```

**Check:** the first line shows `21`, for example `openjdk version "21.0.5"`. If Java is missing, install it with your server's package manager. If the server cannot reach the internet, point the package manager at `MIRROR_URL`, your organization's mirror.

On your machine, copy the JAR to the server:

```bash
scp target/support-assistant-security-database-0.0.1-SNAPSHOT.jar SERVER_USER@SERVER_HOST:/tmp/helpdesk-api.jar
```

**Check:** on the server, `ls -l /tmp/helpdesk-api.jar` shows a file of about 65 MB.

### 2. Make a user and the folders

On the server:

```bash
sudo useradd --system --no-create-home --shell /usr/sbin/nologin helpdesk
sudo mkdir -p /opt/helpdesk-api /etc/helpdesk-api
sudo mv /tmp/helpdesk-api.jar /opt/helpdesk-api/helpdesk-api.jar
sudo chown -R helpdesk:helpdesk /opt/helpdesk-api
```

The user `helpdesk` cannot log in. It exists to own the files and run the API, so a problem in the API cannot touch anything else.

**Check:** `ls -l /opt/helpdesk-api` shows `helpdesk helpdesk` and the file `helpdesk-api.jar`.

### 3. Write the settings file

Create `/etc/helpdesk-api/helpdesk-api.env` with `sudo`, using your editor:

```text
SPRING_DATASOURCE_URL=jdbc:oracle:thin:@//ORACLE_HOST:ORACLE_PORT/ORACLE_SERVICE
SPRING_DATASOURCE_USERNAME=DB_USERNAME
SPRING_DATASOURCE_PASSWORD=DB_PASSWORD
JWT_SECRET=the 64-character line from step 4 of the last note
SERVER_ADDRESS=127.0.0.1
SERVER_PORT=8080
```

Write each line as `NAME=value`: no `export`, no quotes, no spaces around `=`. Then lock the file:

```bash
sudo chmod 600 /etc/helpdesk-api/helpdesk-api.env
```

`SERVER_ADDRESS=127.0.0.1` makes the API listen only on the server itself. Step 5 puts nginx in front of it.

**Check:** `sudo ls -l /etc/helpdesk-api/` shows `-rw-------` and the file name.

### 4. Write the unit file

Create `/etc/systemd/system/helpdesk-api.service` with `sudo`:

```ini
[Unit]
Description=Support Assistant API
After=network.target

[Service]
User=helpdesk
WorkingDirectory=/opt/helpdesk-api
EnvironmentFile=/etc/helpdesk-api/helpdesk-api.env
ExecStart=/usr/bin/java -jar /opt/helpdesk-api/helpdesk-api.jar
SuccessExitStatus=143
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```

What the lines mean:

| Line | Meaning |
|---|---|
| `User=helpdesk` | Run as the user from step 2 |
| `EnvironmentFile=` | Read the settings from step 3 |
| `ExecStart=` | The command to run. Use the path from `which java` if it is not `/usr/bin/java` |
| `SuccessExitStatus=143` | A Java program stopped by `systemctl stop` exits with 143. Count that as a normal stop |
| `Restart=on-failure` | Start the API again if it crashes |

### 5. Start it

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now helpdesk-api
sudo systemctl status helpdesk-api
```

`enable` makes it start at every boot. `--now` also starts it now.

**Check:** `status` shows `Active: active (running)`.

### 6. Read the log and call the API

```bash
journalctl -u helpdesk-api -n 30 --no-pager
curl http://127.0.0.1:8080/actuator/health
```

**Check:** the log ends with `Started SupportAssistantApplication in … seconds`, and `curl` prints `{"status":"UP"}`.

### 7. Check that it restarts

```bash
sudo kill -9 $(systemctl show -p MainPID --value helpdesk-api)
sleep 10
sudo systemctl status helpdesk-api
```

**Check:** the status shows `active (running)` again, with a new process number. `systemd` started the API after the crash.

---

## Common mistakes

> **Warning: the service fails at once.** `systemctl status` shows `Active: failed` or `activating (auto-restart)`. Read the reason with `journalctl -u helpdesk-api -n 50 --no-pager`.

| Message in the log | Cause | Fix |
|---|---|---|
| ``Could not resolve placeholder 'JWT_SECRET'`` | `JWT_SECRET` is missing from the settings file, or a line has a typo | Fix the file, then `sudo systemctl restart helpdesk-api` |
| `ORA-12541: Cannot connect. No listener` | Wrong host or port, or a firewall | Check `SPRING_DATASOURCE_URL` and the firewall between the two servers |
| `ORA-01017` | Wrong user name or password | Fix the two lines in the settings file |
| `Port 8080 was already in use` | Another program uses port 8080 | Stop it, or change `SERVER_PORT` here and in the nginx file |
| `status=203/EXEC` in `systemctl status` | The `java` path in `ExecStart` is wrong | Run `which java` and copy the path |

> **Warning: changing the settings file and nothing happens.** The service reads the file only when it starts. Run `sudo systemctl restart helpdesk-api`.

> **Warning: changing the unit file and nothing happens.** Run `sudo systemctl daemon-reload` first, then restart.

**Check yourself:**

- `systemctl status helpdesk-api` shows `active (running)`.
- `curl http://127.0.0.1:8080/actuator/health` shows `{"status":"UP"}`.
- `sudo systemctl is-enabled helpdesk-api` prints `enabled`.
- `ls -l /etc/helpdesk-api/helpdesk-api.env` shows `-rw-------`.

---

## Practice

1. Restart the server (or ask for it to be restarted) and check `systemctl status helpdesk-api`. **Result:** the API is running again without you starting it.
2. Remove the `JWT_SECRET` line from the settings file and restart the service. **Result:** the status is `failed` and the log shows the `Could not resolve placeholder` message. Put the line back.
3. Run `sudo systemctl stop helpdesk-api`, then `systemctl status helpdesk-api`. **Result:** `inactive (dead)`, and the curl call from step 6 fails with `Connection refused`.

---

## Recap

- A `systemd` service starts the JAR at boot and starts it again after a crash.
- The settings go in a file that only the administrator can read. The unit file points to it with `EnvironmentFile`.
- The API runs as its own user and listens on `127.0.0.1` only.
- `journalctl -u helpdesk-api` shows the log. Restart after any change to the settings file, and `daemon-reload` after any change to the unit file.

> **Not verified:** this note was **not run on a server**. It was written on macOS, which has no `systemd`, and no Linux server was available. The unit file, the commands, and the table of messages follow the standard `systemd` and Spring Boot behaviour. Only the Spring Boot side was seen: the JAR reads `SPRING_DATASOURCE_URL`, `SERVER_PORT` and `JWT_SECRET` from the environment and fails with the quoted `JWT_SECRET` and `ORA-12541` messages. `SERVER_ADDRESS`, `SuccessExitStatus=143`, `status=203/EXEC`, the `useradd` options and the restart test were not run. Try each step on your server and correct what differs.
