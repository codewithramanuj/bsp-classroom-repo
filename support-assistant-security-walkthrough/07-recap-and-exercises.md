# Step 7 — Recap and exercises

> [Back to index](README.md) · Previous: [Testing the rules](06-testing-the-rules.md) · Next: none

## Goal

Pull the pieces together, and practise them on your own.

## Why this matters

You wrote a few small classes, and the application now knows who is calling and what they may do. Going over where each rule lives makes the next change quick: you know which file to open.

## Quick reference

| You want to | Where | Step |
|---|---|---|
| Open a URL to everyone | `permitAll()` in `SecurityConfig` | 3 |
| Make every other URL need a login | `anyRequest().authenticated()`, last | 3 |
| Log in with a user name and password in the header | `httpBasic(Customizer.withDefaults())` | 3 |
| Add a user | `User.withUsername(...)` in `UsersConfig` | 4 |
| Store a password safely | `passwordEncoder.encode(...)`, with a `BCryptPasswordEncoder` bean | 4 |
| Read who is calling | An `Authentication` parameter in a controller method | 4 |
| Allow one role on one URL and method | `requestMatchers(HttpMethod.X, "/path").hasRole("ROLE")` | 5 |
| Allow `POST` and `PATCH` for a header-login API | `csrf(csrf -> csrf.disable())` | 5 |
| Check a rule | MockMvc with `.with(httpBasic(...))` | 6 |

| Code | Meaning |
|---|---|
| `401` | The application does not know who you are |
| `403` | It knows who you are, and your role is not allowed |

## Gotchas

| Symptom | Cause | Step |
|---|---|---|
| The password in the log is different at each start | It is generated. Once you make a `UserDetailsService`, the line disappears | 2, 4 |
| `403` on a URL that needs only a login, with the right password | `httpBasic(...)` is missing from the chain | 3 |
| `POST` and `PATCH` give `401` with the right password, `GET` works | CSRF is on | 5 |
| `ROLE_` written inside `roles(...)` or `hasRole(...)` | Spring adds the prefix | 4, 5 |
| A rule after `anyRequest()` | Spring refuses it at start-up | 5 |

## Discussion questions

1. The filter answers `401` before it looks for a controller. What does a caller learn about your URLs from that?
2. `curl -u` sends the password with every request. What could an attacker read on a network without HTTPS?
3. The passwords of `meera` and `karan` are in the code. What would you change before a real release, and where would you put the users instead?
4. `/api/public/ping` is open to everyone. Which URLs of a real support desk would you also open, and which would you never open?
5. The ticket controller has no security code at all. What are the good and the bad sides of keeping all the rules in `SecurityConfig`?

## Exercises

Ordered from easiest to hardest. No answers are given here.

1. Add a third user, `nisha`, with the role `AGENT`. Log in as `nisha` with `curl -u` and call `PATCH`.
2. Write `Authorization: Basic ...` by hand for `karan:karan123`. Use `echo -n karan:karan123 | base64`, then call the API with `curl -H "Authorization: Basic YOUR_VALUE"`.
3. Make `GET /api/tickets` open to everyone. Which test fails? Change the test so it describes the new rule.
4. Add a test: Karan calls `GET /api/me` and gets the one role `ROLE_AGENT`.
5. Add a URL `GET /api/reports` that only an `AGENT` may call. Write the controller method, the rule, and two tests: one for Karan and one for Meera.
6. Remove everything inside `SecurityConfig` and `SecurityTests`, and write them again from memory. Compare with the finished files. Which rule did you forget?

## What's next

Your users live in memory, and they are made again at each start. The next project keeps the same rules and reads the users from an Oracle table: see [support-assistant-security-database](../support-assistant-security-database/).
