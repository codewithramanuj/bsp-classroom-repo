# Step 3 — Plain JUnit

> [Back to index](README.md) · Previous: [The starting point](02-the-starting-point.md) · Next: [A first Mockito test](04-first-mockito-test.md)

## Goal

`StatusRulesTest` has three tests, and they pass. You also break the rule once, and see them fail.

## Why this matters

`StatusRules` uses no Spring, no database, and no other class. There is nothing to fake. This is the simplest test there is: call a method, check the answer. Tests like this run in a few milliseconds, so you can have hundreds of them.

A test that has never failed has not proved anything. It may pass for the wrong reason. In 3.3 you break the code on purpose, and watch the test notice.

### 3.1 The first test

Open `src/test/java/com/bsp/supportassistant/model/StatusRulesTest.java`. It has two imports and an empty class. Add the first test inside the class:

```java
    @Test
    void aTicketMovesOneStepForward() {
        assertThat(StatusRules.isAllowed("OPEN", "IN_PROGRESS")).isTrue();
        assertThat(StatusRules.isAllowed("RESOLVED", "CLOSED")).isTrue();
    }
```

| Part | Meaning |
|---|---|
| `@Test` | Marks a method as a test. JUnit finds and runs every such method |
| The method name | Says what should happen, in words. Read it as a sentence |
| `assertThat(value).isTrue()` | The check. If the value is not `true`, the test fails and prints why |
| Same package as `StatusRules` | The convention: `model` in the code, `model` in the test. `StatusRules` needs no import because of this |

The test has no `public`. JUnit 5 does not need it.

**Check:** run `mvn test`. You should see `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0` and `BUILD SUCCESS`.

### 3.2 Two more tests

Add these two methods inside the class, after the first one:

```java
    @Test
    void aStepCannotBeSkipped() {
        assertThat(StatusRules.isAllowed("OPEN", "CLOSED")).isFalse();
    }

    @Test
    void aTicketCannotGoBackOrMoveAfterClosed() {
        assertThat(StatusRules.isAllowed("RESOLVED", "OPEN")).isFalse();
        assertThat(StatusRules.isAllowed("CLOSED", "OPEN")).isFalse();
    }
```

One test checks one idea. When a test fails, its name tells you which rule broke.

**Check:** run `mvn test`. You should see `Tests run: 3, Failures: 0, Errors: 0, Skipped: 0`.

### 3.3 Break the code on purpose

Open `src/main/java/com/bsp/supportassistant/model/StatusRules.java`. In `isAllowed`, replace `ORDER.get(position + 1).equals(to)` with `true`. The line becomes:

```java
        return position >= 0 && position + 1 < ORDER.size() && true;
```

Now every step is allowed. Run `mvn test`. You should see two failures:

```text
[ERROR] Tests run: 3, Failures: 2, Errors: 0, Skipped: 0
[ERROR]   StatusRulesTest.aStepCannotBeSkipped:18 
Expecting value to be false but was true
[ERROR]   StatusRulesTest.aTicketCannotGoBackOrMoveAfterClosed:23 
Expecting value to be false but was true
```

The test that checks a good step still passes, and the two that check a refused step fail. The name of each failed test tells you which rule is broken.

**Put the line back** (`ORDER.get(position + 1).equals(to)`) and run `mvn test` again. It shows `Tests run: 3, Failures: 0`.

## Try it

```bash
mvn test
```

Expected, among the last lines:

```text
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, ... -- in com.bsp.supportassistant.model.StatusRulesTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

In IntelliJ you can also click the green arrow beside the class name or a method name.

## Checkpoint

`src/test/java/com/bsp/supportassistant/model/StatusRulesTest.java` - This is the finished file.

<details>
<summary>Full <code>src/test/java/com/bsp/supportassistant/model/StatusRulesTest.java</code></summary>

```java
package com.bsp.supportassistant.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Test 1: plain JUnit. No Spring, no mock. Call the method, check the answer.
class StatusRulesTest {

    @Test
    void aTicketMovesOneStepForward() {
        assertThat(StatusRules.isAllowed("OPEN", "IN_PROGRESS")).isTrue();
        assertThat(StatusRules.isAllowed("RESOLVED", "CLOSED")).isTrue();
    }

    @Test
    void aStepCannotBeSkipped() {
        assertThat(StatusRules.isAllowed("OPEN", "CLOSED")).isFalse();
    }

    @Test
    void aTicketCannotGoBackOrMoveAfterClosed() {
        assertThat(StatusRules.isAllowed("RESOLVED", "OPEN")).isFalse();
        assertThat(StatusRules.isAllowed("CLOSED", "OPEN")).isFalse();
    }
}
```

</details>

## Common mistakes

| Symptom | Cause | Fix |
|---|---|---|
| `class, interface, enum, or record expected` | A test method is outside the closing brace of the class | Move the method inside the class |
| `Tests run: 0` | The method has no `@Test` above it | Add `@Test` |
| The test passes after you broke the code | The test does not check the broken part | Make the check stricter, or add the case that you broke |
| You forgot to put the code back | The next steps fail in `StatusRules` | Restore `ORDER.get(position + 1).equals(to)` |

Next: **[A first Mockito test](04-first-mockito-test.md)**.
