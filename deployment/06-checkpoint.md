# Health Check, Updates and the Checkpoint

**Time: about 35 minutes**

Back to the [Day 8 index](README.md) · Previous: [nginx](05-nginx.md)

You need the whole system from steps 1 to 5 running on the server.

## What you will build

- A health check you can run by hand or from a monitoring tool.
- A safe way to put a new version of the API on the server, and to go back.
- A safe way to put a new version of the React app on the server.

---

## The ideas

### A health check

A health check is a call that answers one question: "is the application working?" `/actuator/health` answers `{"status":"UP"}` when the API runs and can reach Oracle. It answers `{"status":"DOWN"}` with status `503` when the database cannot be reached. A monitoring tool, or a person with `curl`, can ask it every minute.

The check is private: nginx does not pass `/actuator/` on, so you run it on the server itself.

### An update

An update replaces the JAR or the React files. The API stops for a few seconds while the new JAR starts. For a training server this is acceptable. Keep the previous JAR, so you can go back.

---

## Build steps

### 1. Run the health check

On the server:

```bash
curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:8080/actuator/health
```

**Check:** it prints `200`. For the whole answer, leave out `-o` and `-w`.

### 2. See the check fail

Stop the API and call the check again:

```bash
sudo systemctl stop helpdesk-api
curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:8080/actuator/health
sudo systemctl start helpdesk-api
```

**Check:** the call prints `000`, which means nothing answered. After `start`, wait a few seconds, and the first command prints `200` again.

### 3. Update the API

On your machine, build the new JAR and copy it to the server under a new name:

```bash
mvn -q clean package -DskipTests
scp target/support-assistant-security-database-0.0.1-SNAPSHOT.jar SERVER_USER@SERVER_HOST:/tmp/helpdesk-api-new.jar
```

On the server, keep the old JAR and put the new one in its place:

```bash
sudo cp /opt/helpdesk-api/helpdesk-api.jar /opt/helpdesk-api/helpdesk-api.jar.previous
sudo mv /tmp/helpdesk-api-new.jar /opt/helpdesk-api/helpdesk-api.jar
sudo chown helpdesk:helpdesk /opt/helpdesk-api/helpdesk-api.jar
sudo systemctl restart helpdesk-api
```

Wait about ten seconds, then run the health check from step 1.

**Check:** it prints `200`. If it does not, go back:

```bash
sudo mv /opt/helpdesk-api/helpdesk-api.jar.previous /opt/helpdesk-api/helpdesk-api.jar
sudo systemctl restart helpdesk-api
```

### 4. Update the React app

On your machine, build and copy the new files to a temporary folder on the server:

```bash
npm run build
ssh SERVER_USER@SERVER_HOST 'mkdir -p /tmp/helpdesk-new'
scp -r dist/* SERVER_USER@SERVER_HOST:/tmp/helpdesk-new/
```

On the server, swap the files:

```bash
sudo rm -rf /var/www/helpdesk/*
sudo cp -r /tmp/helpdesk-new/* /var/www/helpdesk/
rm -rf /tmp/helpdesk-new
```

No restart is needed. nginx reads the files on each request.

**Check:** reload the page in the browser with `Ctrl+Shift+R` (`Cmd+Shift+R` on a Mac), which skips the saved copy. The change is on the page.

### 5. Read the logs

```bash
journalctl -u helpdesk-api -f
sudo tail -n 20 /var/log/nginx/error.log
```

`-f` follows the log live. Leave it with `Ctrl+C`.

**Check:** while the first command runs, log in from the browser. A line appears for the work the API does. The second command shows no new errors.

---

## Common mistakes

> **Warning: the browser shows the old page after an update.** The browser kept a copy of `index.html`. Reload with `Ctrl+Shift+R`.

> **Warning: the health check says `200`, and the site is broken.** The check tests the API and its database, not nginx or the React files. Open the site in a browser as well.

> **Warning: the health check says `401`.** The rule `.requestMatchers("/actuator/health").permitAll()` is missing from `SecurityConfig`, or it is below `anyRequest().authenticated()`. See [step 2 of note 2](02-build-the-jar.md).

> **Warning: updating the JAR while the file is owned by `root`.** The service user `helpdesk` cannot read it, and the service fails. Run the `chown` line from step 3.

> **Warning: a database script changes together with the JAR.** The scripts in `db/oracle/` are run by hand, before you restart the API with the new JAR. The API never changes the tables.

---

## Practice

1. Make a small visible change in the React app (the title in `.env.production`), then update the server by following step 4. **Result:** the browser shows the new title after a reload.
2. Put a broken JAR in place (copy any small text file to `helpdesk-api.jar`), restart, and go back with the commands in step 3. **Result:** the service fails, the health check prints `000`, and after the rollback it prints `200`.
3. Write a small script `check.sh` that prints `OK` if the health check returns `200`, and `FAILED` if not. **Result:** it prints `OK` while the API runs and `FAILED` when you stop the service.

---

## Recap

- `/actuator/health` tells you the API works and reaches Oracle. It is open to the server itself, and nginx does not pass it on.
- An API update is: copy the JAR, keep the previous one, restart, check the health.
- A React update is: build, copy the files, reload with a hard refresh.
- Logs: `journalctl -u helpdesk-api` for the API, `/var/log/nginx/error.log` for nginx.

---

## Checkpoint

Day 8 ends here. Tick every line before you move on.

- [ ] `mvn -q clean package` builds one JAR, and `npm run build` builds `dist/`.
- [ ] The API runs as `helpdesk-api.service`, as the user `helpdesk`, and `systemctl is-enabled helpdesk-api` prints `enabled`.
- [ ] The settings file is `-rw-------`, and no password or `JWT_SECRET` is in the repository.
- [ ] `curl http://127.0.0.1:8080/actuator/health` on the server prints `{"status":"UP"}`.
- [ ] `http://SERVER_HOST/` shows the login page, and `meera` and `karan` can log in.
- [ ] A reload on `/tickets` still shows the page.
- [ ] Port 8080 is not reachable from another machine.
- [ ] You updated the API and the React app once, and you went back once.

> **Not verified:** this note was **not run on a server** (no Linux server, no `systemd`, no nginx, no Oracle). The `200`, `503` and `000` results come from standard `curl` and Actuator behaviour. The Actuator health answer `DOWN` with `503` when the database is unreachable was not seen. `scp`, `ssh` and the log paths (`/var/log/nginx/error.log`, which can differ by distribution) were not run. The practice tasks were not run.
