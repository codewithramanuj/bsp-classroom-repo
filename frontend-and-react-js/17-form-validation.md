# Form Validation: Messages as You Type

**Time: about 45 minutes**

You need the project from [Sending Data to the Backend with a POST Request](11-sending-data-with-post.md) or later: `src/components/TicketForm.jsx` with the `onCreate` prop. These steps also work in the project from [note 16](16-sending-the-token.md). The Bootstrap look from [note 08](08-bootstrap-and-react-bootstrap.md) is optional.

Right now the form lets you send anything. The only check is the browser's own `required` on the title. In this note the form checks the values itself, and tells you what is wrong while you type.

## What you will build

- A check for the **title**: it must not be empty, and it must have 100 characters or fewer.
- A check for the **description**: 500 characters or fewer.
- A **character counter** under each field, such as `12/100`.
- Red messages that appear and disappear as you type.
- A **Create ticket** button that sends nothing while a value is wrong.

---

## The ideas

### Why check in the page

**Validation** means checking a value before it is used. If the page checks the title, you see the problem at once, without waiting for the API to answer. The API checks too, and its answer is the final one: a page can be bypassed, so the API never trusts it. The checks in the page are for your user's convenience.

The rules of the page follow the rules of the API:

| Field | Rule |
|---|---|
| Title | Required (not empty, and not only spaces). At most 100 characters |
| Description | Optional. At most 500 characters |
| Priority | The list offers only `HIGH`, `MEDIUM` and `LOW`, so there is nothing to check |

### A rule is a small function

Write each rule as a plain function. It receives the value and returns a **message**, or an **empty text** when the value is fine. This is plain JavaScript, not React:

```javascript
const TITLE_MAX = 100

function validateTitle(title) {
  if (title.trim() === '') {
    return 'Title is required.'
  }
  if (title.length > TITLE_MAX) {
    return 'Title must have ' + TITLE_MAX + ' characters or fewer.'
  }
  return ''
}

console.log(validateTitle(''))
console.log(validateTitle('   '))
console.log(validateTitle('Printer is jammed'))
console.log(validateTitle('x'.repeat(101)))
console.log(validateTitle('Printer is jammed') === '')
```

```text
Title is required.
Title is required.

Title must have 100 characters or fewer.
true
```

`title.trim()` removes the spaces at both ends, so a title made only of spaces counts as empty. The third line of the output is empty: that is the "fine" answer.

### The message follows the state

The title is already in state (`title`), and React runs your component again each time the state changes. So you can work out the message **while drawing the form**, with no extra state:

```jsx
const titleError = validateTitle(title)
```

When you type a letter, `title` changes, the component runs again, `titleError` is worked out again, and the message appears or disappears. That is what "as you type" means here.

### Show a message only after the field is touched

A new form has an empty title, so `titleError` is `Title is required.` from the very first moment. A form that shows a red message before you type anything feels like a scolding. So the form remembers whether you have **touched** the title, which means you typed in it, or left it. The message is shown only when `touched` is true.

### The browser's checks and yours

The `required` attribute makes the browser check the field and show its own bubble, `Please fill out this field.`, in place of your message. When you do the checking yourself, turn the browser's check off with `noValidate` on the `<form>`, and remove `required`.

---

## Build steps

Work in `hello-react` with `npm run dev` running. Open the page that shows the form: `/` in note 11, or `/new` if you did note 14.

### 1. A rule, and a message

Add the rule to the top of `src/components/TicketForm.jsx`, under the `import` line:

```jsx
const TITLE_MAX = 100

function validateTitle(title) {
  if (title.trim() === '') {
    return 'Title is required.'
  }
  if (title.length > TITLE_MAX) {
    return 'Title must have ' + TITLE_MAX + ' characters or fewer.'
  }
  return ''
}
```

Inside `TicketForm`, below the `useState` lines, add:

```jsx
const titleError = validateTitle(title)
```

Under the title `<input … />`, and inside the same `<div>`, add the message:

```jsx
{titleError && <p className="field-error">{titleError}</p>}
```

An empty text counts as false, so when `titleError` is `''` nothing is drawn.

Add this rule at the end of `src/index.css`:

```css
.field-error { color: #b00020; margin: 0.25rem 0 0.5rem; }
```

**Check:** the form shows the red message `Title is required.` under the title, before you type anything. Type a letter: the message goes. Delete it: the message comes back. That first, early message is what the next step fixes.

### 2. Show it only after the field is touched

Add one piece of state, below the other `useState` lines:

```jsx
const [titleTouched, setTitleTouched] = useState(false)
```

Change the title `<input>`. It sets `titleTouched` when you type, and when you leave the field (`onBlur`):

```jsx
<input
  id="title"
  value={title}
  onChange={(event) => {
    setTitle(event.target.value)
    setTitleTouched(true)
  }}
  onBlur={() => setTitleTouched(true)}
  required
/>
```

Change the message line to:

```jsx
{titleTouched && titleError && <p className="field-error">{titleError}</p>}
```

**Check:** reload the page. The form has no message. Click in the title box and click somewhere else: `Title is required.` appears. Type a letter: it goes. Delete the letter: it is back at once. Type only spaces: the message stays.

### 3. A counter, a longer rule, and the description

Add a second rule next to the first, and the limit above it:

```jsx
const DESCRIPTION_MAX = 500

function validateDescription(description) {
  if (description.length > DESCRIPTION_MAX) {
    return 'Description must have ' + DESCRIPTION_MAX + ' characters or fewer.'
  }
  return ''
}
```

Next to `titleError`, add:

```jsx
const descriptionError = validateDescription(description)
```

Under the title `<input>`, **above** the message, add a counter:

```jsx
<small className="counter">{title.length}/{TITLE_MAX}</small>
```

Under the description `<textarea>`, add a counter and a message:

```jsx
<small className="counter">{description.length}/{DESCRIPTION_MAX}</small>
{descriptionError && <p className="field-error">{descriptionError}</p>}
```

The description message needs no `touched`: it can only appear after you have typed too much.

Add a rule at the end of `src/index.css`, so that the counter sits on its own line:

```css
.counter { display: block; color: #555; }
```

**Check:** both counters start at `0/100` and `0/500`, and grow as you type. To see the messages without typing 100 letters, change `TITLE_MAX` to `10` for a moment: type 11 letters, and the message `Title must have 10 characters or fewer.` appears, with the counter at `11/10`. Remove one letter, and it goes. Then set `TITLE_MAX` back to `100`.

### 4. Do not send a wrong ticket

Replace `src/components/TicketForm.jsx` with the final version. Compared with step 3, `handleSubmit` stops when a rule fails, the `required` is gone, and the form has `noValidate`:

```jsx
import { useState } from 'react'

const TITLE_MAX = 100
const DESCRIPTION_MAX = 500

function validateTitle(title) {
  if (title.trim() === '') {
    return 'Title is required.'
  }
  if (title.length > TITLE_MAX) {
    return 'Title must have ' + TITLE_MAX + ' characters or fewer.'
  }
  return ''
}

function validateDescription(description) {
  if (description.length > DESCRIPTION_MAX) {
    return 'Description must have ' + DESCRIPTION_MAX + ' characters or fewer.'
  }
  return ''
}

function TicketForm({ onCreate }) {
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [priority, setPriority] = useState('MEDIUM')
  const [titleTouched, setTitleTouched] = useState(false)
  const [error, setError] = useState('')

  const titleError = validateTitle(title)
  const descriptionError = validateDescription(description)

  async function handleSubmit(event) {
    event.preventDefault()
    setTitleTouched(true)
    if (titleError || descriptionError) {
      return
    }
    setError('')
    const newTicket = { title, description, priority }
    try {
      await onCreate(newTicket)
      setTitle('')
      setDescription('')
      setPriority('MEDIUM')
      setTitleTouched(false)
    } catch (err) {
      setError('Could not create the ticket. ' + err.message)
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <h2>Raise a Ticket</h2>

      <div>
        <label htmlFor="title">Title</label>
        <br />
        <input
          id="title"
          value={title}
          onChange={(event) => {
            setTitle(event.target.value)
            setTitleTouched(true)
          }}
          onBlur={() => setTitleTouched(true)}
        />
        <small className="counter">{title.length}/{TITLE_MAX}</small>
        {titleTouched && titleError && <p className="field-error">{titleError}</p>}
      </div>

      <div>
        <label htmlFor="description">Description</label>
        <br />
        <textarea
          id="description"
          value={description}
          onChange={(event) => setDescription(event.target.value)}
        />
        <small className="counter">{description.length}/{DESCRIPTION_MAX}</small>
        {descriptionError && <p className="field-error">{descriptionError}</p>}
      </div>

      <div>
        <label htmlFor="priority">Priority</label>
        <br />
        <select
          id="priority"
          value={priority}
          onChange={(event) => setPriority(event.target.value)}
        >
          <option value="HIGH">HIGH</option>
          <option value="MEDIUM">MEDIUM</option>
          <option value="LOW">LOW</option>
        </select>
      </div>

      {error && <p>{error}</p>}

      <button type="submit">Create ticket</button>
    </form>
  )
}

export default TicketForm
```

Two lines are new in `handleSubmit`. `setTitleTouched(true)` makes the title message show, even if you never clicked in the box. Then `if (titleError || descriptionError) { return }` ends the function before anything is sent: both are empty texts (false) when the values are fine.

The `error` state from note 11 stays. It shows what the **API** says, such as a refused login or a server problem, and the new messages show what the **page** found.

**Check:** with the form empty, click **Create ticket**. The message `Title is required.` appears, and the browser's bubble does not. Open the **Network** tab and click the button again: no new request to `tickets` is sent. Type a title such as `Valid ticket` and click **Create ticket**: the ticket is sent, and the form clears.

### 5. Run the checks

```bash
npm run lint
npm run build
```

**Check:** `npm run lint` prints nothing but its own header, and `npm run build` ends with `✓ built in …`.

### If you did note 08: the Bootstrap look

Bootstrap shows a red box and a message when the field has the class `is-invalid`. The message goes in a `<div className="invalid-feedback">` right after the field, and Bootstrap hides it until the field is marked. Work out the flag once, below `descriptionError`:

```jsx
const showTitleError = titleTouched && titleError
```

Then, for the title:

```jsx
<input
  id="title"
  className={'form-control' + (showTitleError ? ' is-invalid' : '')}
  value={title}
  onChange={(event) => {
    setTitle(event.target.value)
    setTitleTouched(true)
  }}
  onBlur={() => setTitleTouched(true)}
/>
<div className="invalid-feedback">{titleError}</div>
<div className="form-text">{title.length}/{TITLE_MAX}</div>
```

Do the same for the description, with `descriptionError` in place of `showTitleError`. The other classes (`form-label`, `form-select`, `btn btn-primary`) are the ones from note 10. You do not need the two CSS rules from this note.

**Check:** a title of 101 letters shows a red box with the message `Title must have 100 characters or fewer.` under it, and the counter `101/100` below.

---

## Common mistakes

> **Warning: `required` is still on the title, and the form has no `noValidate`.** When you click **Create ticket** with an empty title, the browser shows its own bubble, `Please fill out this field.`, and your `handleSubmit` is never called. Your message does not appear. Remove `required` and add `noValidate` to the `<form>`.

> **Warning: no `return` when a rule fails.** The messages show, but the ticket is sent anyway: the **Network** tab shows a request to `tickets`. A check that does not stop the request only decorates the form. End `handleSubmit` with `return` when `titleError` or `descriptionError` has text.

> **Warning: `title === ''` in place of `title.trim() === ''`.** A title of three spaces gives no message and is sent. Use `trim()`.

---

## Practice

1. Make the **Create ticket** button disabled while a rule fails: `disabled={titleError !== '' || descriptionError !== ''}`. **Result:** the button is grey on a new form and while a value is wrong. Think about what the user sees on a new form, and whether that is better or worse than the message on click.
2. Add a rule of your own to `validateTitle`: at least 3 characters, with the message `Title must have at least 3 characters.` **Result:** a title of two letters shows the message, and three letters clear it. The API has no such rule, so this one is only the page's.
3. Make the counter turn red when the text is too long: give it the class `field-error` as well when `title.length > TITLE_MAX`. **Result:** `101/100` is red, and `100/100` is not.

---

## Recap

- **Validation** checks a value before it is sent. The page's checks help the user. The API checks again, and its answer is the final one.
- Write each rule as a plain function that returns a message, or an empty text when the value is fine.
- Work the message out while drawing the form: `const titleError = validateTitle(title)`. It then follows the state as you type.
- Show a message only after the field is **touched**, with one piece of state set in `onChange` and `onBlur`.
- In `handleSubmit`, stop with `return` when a rule fails. Use `noValidate` on the form and drop `required`, so that your messages are the ones that show.

> **Not verified:** the steps were run on macOS with Node.js 22.19.0, Vite 8.3.5, React 19.3.1 and `react-router` 7.18.5, in headless Chrome, with the form on the `/new` page from note 14 and the login from notes 15 and 16 (the API was `support-assistant-security-database` on a temporary H2 copy, with the `SecurityConfig` line from note 16). A script typed into the form: a new form showed no message; leaving the empty title showed `Title is required.`; one letter removed it; deleting the letter, and a title of spaces, brought it back; 101 letters gave the 100-character message with the counter at `101/100`; 100 letters cleared it; 501 and 500 characters in the description did the same with the 500-character message. Submitting the empty form kept the page on the form, with the message, and the API received no request. Submitting a valid title sent one request, which was answered `201`, and returned to the list. The step 1 version showed the message on a new form. The three common mistakes were each tried: with `required`, the form was not valid, `handleSubmit` did not run and no message appeared (the browser's bubble is not part of the page, so it was not seen); with no `return`, the request was sent; with `title === ''`, spaces were not caught and the request was sent. The Bootstrap form showed a red box and the message on a screenshot. Changing `TITLE_MAX` to `10` in step 3 was not run, and the 101-letter title was used in its place. The plain JavaScript example was run in Node. The messages of the API itself for a bad title (`Title is required`, `Title must have 100 characters or fewer`) were read in the code of `support-assistant-jpa-h2`, and were not triggered from this form. The practice tasks were not run. Only Chrome was used.
