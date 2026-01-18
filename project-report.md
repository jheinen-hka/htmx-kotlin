
# HTMX in Kotlin - Project Report

## 1 Introduction

Modern web applications are often implemented as Single-Page Applications (SPAs) using frameworks such as React, Angular, or Vue. These approaches enable highly interactive user interfaces, but they also introduce significant complexity: large JavaScript bundles, complex build pipelines, and a strong separation between frontend and backend logic.

In contrast, classic server-side rendered applications send a full HTML page for each user interaction (for example, form submission or clicking a link). This architecture is simple and robust, but from a modern perspective often feels slow and less interactive.

The JavaScript library **HTMX** positions itself between these two extremes. It allows developers to access modern browser features directly from HTML, without building a full SPA [2]. Instead of JSON APIs and extensive JavaScript code, the concept of *HTML over the wire* moves back into focus: the server sends HTML fragments that are dynamically inserted into the DOM in the browser.

In the blog article *“A Quick Guide to HTMX in Kotlin”* by Codersee, it is shown how HTMX can be combined with a Kotlin backend (Ktor) to dynamically update user interfaces without building a separate JavaScript SPA [1]. This project work builds on that idea, but uses **Spring Boot** as the backend framework and implements its own example application to demonstrate the integration.

Over the course of the work, the original minimal message example was extended into a small **HTMX dashboard** consisting of several cards:

- a *Message Center* that manages a list of text messages,
- a *Task Board* that demonstrates CRUD-like operations for tasks,
- a *User Card* that manages richer user objects and shows inline editing,
- and a *Live User Search* that illustrates debounced searching and filtering on the server.

All of these features are implemented with Kotlin/Spring Boot on the backend and a static HTML page using HTMX (plus a lightweight CSS framework) on the frontend.

### 1.1 Objectives of the project

The objectives of this project are:

- to explain the fundamental concepts of HTMX,
- to show how HTMX can be integrated with a Kotlin backend (here: Spring Boot),
- to implement and document a working example application,
- to demonstrate a richer use case including multiple entities (messages, tasks, users) and search,
- and to discuss advantages and disadvantages of this approach compared to classical SPA architectures and purely server-side applications.

### 1.2 Structure of this report

The report is structured as follows:

- Chapter 2 introduces the necessary basics of web architectures, Kotlin in the backend, and HTMX.
- Chapter 3 details the core concepts and mechanisms of HTMX.
- Chapter 4 describes different architectural variants of “HTMX in Kotlin” and relates them to the Codersee example.
- Chapter 5 documents the implementation of the demo dashboard with Spring Boot and HTMX.
- Chapter 6 sketches extended scenarios that could be implemented with HTMX in Kotlin beyond the current demo.
- Chapter 7 compares and evaluates the approach.
- Chapter 8 discusses how HTMX can coexist with other consumers in a multipurpose backend.
- Chapter 9 describes the error handling strategy used in the application.
- Chapter 10 provides an overview of the HTMX attributes used in the demo and explains their semantics.
- Chapter 11 summarizes the findings and gives an outlook on possible future extensions.
- Chapter 12 lists the references used.

---

## 2 Fundamentals

### 2.1 Overview of web architectures

To put HTMX into context, it is useful to distinguish between three common architectural approaches:

1. **Classic server-side rendering**

   The browser sends an HTTP request to the server for each user interaction (for example, form submission or clicking a link). The server renders a full HTML page and sends it back. The browser then reloads the entire page.  
   Advantages:
    - simple architecture,
    - little JavaScript required,
    - good SEO.  
      Disadvantages:
    - full page reloads for many interactions,
    - can feel slow and less responsive.

2. **Single-Page Applications (SPA)**

   A JavaScript application runs in the browser, typically communicates with the backend via JSON APIs, and is responsible for DOM updates on the client.  
   Advantages:
    - highly interactive user interfaces,
    - smooth user experience without full page reloads.  
      Disadvantages:
    - increased complexity (state management, routing, build tooling),
    - large initial JavaScript payload,
    - business logic is split between frontend and backend.

3. **HTML over the wire / hypermedia-driven applications**

   The server renders HTML fragments which are inserted into existing page sections. JavaScript mainly acts as a thin transport and integration layer. HTMX is a prominent example of this approach.  
   Advantages:
    - most business logic stays in the backend,
    - frontend stays simple (HTML plus a bit of JavaScript via HTMX),
    - still allows interactive user interfaces.

HTMX can be understood as a middle ground between classic server-side rendering and SPAs: it relies on standard HTTP semantics and HTML to increase interactivity, without requiring a full client-side application.

### 2.1.1 History and position of HTMX compared to SPA frameworks

HTMX is a relatively young library: version 1.0.0 was released in November 2020 as a redesigned successor of intercooler.js, which had originally been created and released around 2013 [3][4]. In contrast, the main single-page application (SPA) frameworks and libraries are significantly older. React was open-sourced by Facebook in 2013 [5], the first public release of Vue.js followed in 2014 [6], and Angular (as a complete rewrite of AngularJS) was introduced in 2016 [7]. HTMX therefore appeared after almost a decade of SPA-dominated frontend development and can be seen as a deliberate response to the increasing complexity of large client-side frameworks. Instead of moving more and more logic into the browser, HTMX aims to bring back server-driven HTML while still providing modern interactivity through declarative attributes on standard HTML elements.

The chart below shows the increasing popularity of HTMX over the years measured by the number of GitHub stars that reflect the popularity of the library.

[![Star History Chart](https://api.star-history.com/svg?repos=bigskysoftware/htmx&type=date&legend=top-left)](https://www.star-history.com/#bigskysoftware/htmx&type=date&legend=top-left)

The picture below shows the popularity of HTMX in the year 2024, making it the 22nd most popular web framework.

![HTMX Popularity](doc-images/htmx-popularity.png)  
(https://hamy.xyz/blog/2024-09_the-state-of-htmx)  
(https://survey.stackoverflow.co/2024/technology#1-web-frameworks-and-technologies)

The last picture shows how admired HTMX as a library is, according to Stack Overflow's 2024 Developer Survey.

![HTMX Admired](doc-images/htmx-admired.png)  
(https://hamy.xyz/blog/2024-09_the-state-of-htmx)  
(https://survey.stackoverflow.co/2024/technology#1-web-frameworks-and-technologies)

### 2.2 Kotlin in the backend

Kotlin is a modern, statically typed language targeting the JVM. It is widely used in Android development, but also increasingly for server-side applications (for example with Spring Boot or Ktor) [9]. Relevant properties for backend development include:

- null safety enforced by the type system,
- expressive syntax and data classes,
- support for DSL-like APIs (for example the HTML DSL in Ktor),
- interoperability with existing Java libraries.

In this project, Kotlin is used together with **Spring Boot**. Spring Boot provides [8]:

- an embedded web server (e.g. Tomcat),
- an established MVC model with controllers,
- strong integration with the Spring ecosystem.

The example application shows that HTMX can be integrated into a classical Spring MVC application without any special libraries beyond HTMX itself.

### 2.3 HTMX - basic idea

HTMX is a JavaScript library that is primarily used through HTML attributes. Instead of writing imperative JavaScript code, HTML elements are decorated with `hx-*` attributes to trigger HTTP requests and insert the responses into the DOM [2].

Some central attributes are:

- `hx-get`: executes an HTTP GET request,
- `hx-post`: executes an HTTP POST request,
- `hx-put`: executes an HTTP PUT request,
- `hx-delete`: executes an HTTP DELETE request,
- `hx-trigger`: defines when the request should be sent (for example `load`, `click`, `changed`),
- `hx-target`: specifies which DOM element should be updated with the response,
- `hx-swap`: defines how the response is inserted into the target element,
- `hx-swap-oob`: enables *out-of-band* updates of DOM elements that are not the main target of the request,
- `hx-indicator`: links an element to the loading state of a request (for example, to show a spinner),
- `hx-on` / `hx-on::event`: attaches client-side event handlers directly in HTML.

The library therefore acts as a small client-side layer which drives HTTP requests from HTML and processes the server’s HTML responses. HTMX itself is backend-agnostic; in this project, a Kotlin/Spring Boot backend is used.

---

## 3 HTMX concepts and mechanisms

This chapter describes the most important HTMX concepts in more detail. These basics are then used in the implementation chapter to explain the example application.

### 3.1 HTTP methods and `hx-*` attributes

HTMX extends HTML elements with attributes that trigger HTTP requests. Some examples:

- `hx-get="/messages"`  
  Triggers a GET request to `/messages`.
- `hx-post="/add-message"`  
  Triggers a POST request, typically including form data.
- `hx-put` and `hx-delete`  
  Can be used for update and delete operations.

The server’s response is interpreted as HTML and, by default, applied to the DOM element specified by the `hx-target` attribute.

In the demo dashboard, all four HTTP verbs are used:

- `hx-get` is used to load message, task and user lists as well as search results.
- `hx-post` is used to create new messages, tasks and users.
- `hx-put` is used to update existing users via an inline edit form.
- `hx-delete` is used to delete tasks and users.

### 3.2 Triggers and events

The `hx-trigger` attribute defines when a request is fired. Common examples include:

- `hx-trigger="load"` - when the element is loaded into the DOM,
- `hx-trigger="click"` - when the element is clicked,
- `hx-trigger="change"` - when the value of an input field changes.

HTMX also allows more advanced triggers, for example including debounce behaviour:

- `hx-trigger="keyup changed delay:300ms"` - wait until the user has stopped typing for 300 ms before sending the request.

In the example application:

- `hx-trigger="load"` is used to load the initial lists (messages, tasks, users) as soon as the page and the corresponding card have been rendered.
- `hx-trigger="keyup changed delay:300ms"` is used for the live user search input field to avoid sending a request for every keystroke.
- `hx-trigger="change"` is used for the gender filter in the live search card.

### 3.3 Target element and swap strategy

Two further key attributes are:

- `hx-target`  
  Defines which DOM element should be updated with the server response. The target is specified via CSS selector (for example `#messages` or `closest tr`).
- `hx-swap`  
  Defines how the response is inserted into the target, for example:
    - `innerHTML` (default): replaces the inner content of the target element,
    - `outerHTML`: replaces the element itself,
    - `beforebegin`, `afterbegin`, `beforeend`, `afterend`: insert content relative to the element.

In the demo application:

- `hx-target="#messages"` and `hx-swap="innerHTML"` are used to completely replace the HTML representation of the message list.
- `hx-target="#task-list"` with `hx-swap="outerHTML"` replaces the entire container of the task list so that the backend controls the full markup.
- For inline editing of users, `hx-target="closest tr"` and `hx-swap="outerHTML"` are used in the server-rendered HTML to replace only the table row that was edited.

### 3.4 Out-of-band updates with `hx-swap-oob`

One special feature of HTMX is out-of-band updates. If an element in the server’s response has the attribute `hx-swap-oob="true"`, it will not be inserted at the response’s main target location. Instead, HTMX looks for a matching element elsewhere in the DOM and replaces or updates it there.

This can be used, for example, to update a navigation bar or a badge counter in response to a request whose main target is a different part of the page.

In early iterations of the demo, `hx-swap-oob` was used to clear the input field of the message form by sending an additional `<input>` element from the backend. In the final version of the application, this behaviour is instead implemented using the more explicit `hx-on::after-request` mechanism on the client side (see Chapter 9), while `hx-swap-oob` is still documented as an important concept of HTMX.

---

## 4 HTMX with Kotlin - architectural variants

HTMX is not tied to a specific backend framework. In the Kotlin ecosystem, two popular options are:

1. **Ktor + HTMX**  
   In the Codersee example, HTMX is combined with Ktor. Ktor allows generating HTML using a Kotlin DSL. Routes can return HTML pages or fragments directly from code. HTMX integration means that controllers return HTML and that the generated templates contain appropriate `hx-*` attributes.

2. **Spring Boot + HTMX**  
   In this project, Spring Boot is used. Spring MVC controllers can return `String` values which represent HTML fragments. HTMX integration is done by:
    - returning HTML fragments instead of JSON from controllers,
    - using HTMX in the frontend via a CDN and `hx-*` attributes,
    - avoiding any heavy client-side frameworks.

In both variants, the business logic remains in the Kotlin backend. The browser primarily acts as a rendering client for HTML, enriched with HTMX for dynamic updates.

---

## 5 Implementation of the demo dashboard

This chapter documents the implementation of the demo dashboard developed for this project. The full source code is available in a GitHub repository and is organized into two subprojects:

```text
htmx-kotlin/
├─ htmx-be/   // Spring Boot backend (Kotlin)
└─ htmx-fe/   // Static frontend (HTML + CSS + HTMX)
```

The backend exposes HTML endpoints specifically designed for HTMX as well as JSON endpoints that illustrate how the same domain logic can be reused for a multipurpose backend.

### 5.1 Requirements

The example application is intended to fulfil the following functional requirements:

- A list of text messages is displayed on the page and can be extended.
- Tasks can be created, listed, toggled (done / open) and deleted.
- Users can be created, listed, edited and deleted (CRUD operations on a richer object).
- A live search allows filtering users by name and gender with a debounced request pattern.
- All lists should update without reloading the entire page.
- Basic error handling should notify the user when the backend is not reachable or when a request fails.
- The implementation should be minimal (no database, no authentication) to keep the focus on HTMX plus Kotlin.

### 5.2 Backend: Spring Boot application

The Spring Boot application is started by the `HtmxBeApplication` class:

```kotlin
@SpringBootApplication
class HtmxBeApplication

fun main(args: Array<String>) {
    runApplication<HtmxBeApplication>(*args)
}
```

When started, an embedded web server listens on port 8080 and handles incoming HTTP requests.

#### 5.2.1 Data model and repositories

The application uses three simple domain types:

- `Message` - a short text note,
- `Task` - a to-do item with a completion flag,
- `User` - a richer object with name, age and gender.

All data is stored in memory for the purposes of the demo.

**Messages**

```kotlin
data class Message(
    val id: Long,
    val text: String
)
```

Messages are stored in an in-memory repository similar to the following:

```kotlin
@Repository
class MessageRepository {
    private val seq = AtomicLong(1)
    private val data = CopyOnWriteArrayList<Message>().apply {
        add(Message(seq.getAndIncrement(), "I am working on the topic HTMX"))
        add(Message(seq.getAndIncrement(), "This is for my Projektarbeit 2 in Informatik Master"))
        add(Message(seq.getAndIncrement(), "I will have to show how HTMX works with a Kotlin Backend"))
    }

    fun findAll(): List<Message> = data.toList()

    fun add(text: String): Message {
        val m = Message(seq.getAndIncrement(), text)
        data.add(m)
        return m
    }
}
```

**Tasks**

A task contains a title and a completion flag:

```kotlin
data class Task(
    val id: Long,
    val title: String,
    val completed: Boolean
)
```

The corresponding repository keeps a list of demo tasks in memory and offers methods to list, create, toggle and delete tasks.

**Users**

The user domain object models a slightly richer example:

```kotlin
enum class Gender {
    FEMALE, MALE, OTHER, UNKNOWN
}

data class User(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val age: Int?,
    val gender: Gender
)
```

The in-memory `UserRepository` provides basic CRUD operations and initialises a list of demo users with different first names and genders to make the live search and filtering tangible.

Key points across all repositories:

- A real database is intentionally not used; data is kept in memory for simplicity.
- `AtomicLong` generates unique IDs.
- Thread-safe collections (for example `CopyOnWriteArrayList`) are used to keep the demo robust in a multi-threaded environment.

#### 5.2.2 Service layer

Domain logic is encapsulated in services, each using the corresponding repository. An example for messages:

```kotlin
@Service
class MessageService(private val repo: MessageRepository) {

    fun getMessages(): List<Message> = repo.findAll()

    fun addMessage(text: String): List<Message> {
        require(text.isNotBlank()) { "message must not be blank" }
        repo.add(text.trim())
        return repo.findAll()
    }
}
```

The same pattern is used for tasks and users:

- The **Task service** validates titles, toggles the `completed` flag and removes items.
- The **User service** validates required fields, maps incoming form data to the domain model and implements create, update and delete operations.

Across all services, input validation is done on the server side, and invalid calls result in exceptions or appropriate HTTP error codes. This is complemented by HTML5 validation in the frontend (for example through the `required` attribute) and by error handling mechanisms described in Chapter 9.

#### 5.2.3 HTMX controllers

For each domain type there is a dedicated controller that returns HTML fragments tailored to the HTMX frontend. All HTMX routes are grouped under the `/htmx/...` path.

**Messages**

```kotlin
@RestController
@CrossOrigin(
    origins = [
        "http://localhost:63342", "http://127.0.0.1:63342",
        "http://localhost:5500", "http://127.0.0.1:5500"
    ]
)
class MessageHtmxController(private val service: MessageService) {

    @GetMapping("htmx/messages", produces = [MediaType.TEXT_HTML_VALUE])
    fun messagesHtml(): String =
        renderList(service.getMessages())

    @PostMapping(
        "htmx/add-message",
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.TEXT_HTML_VALUE]
    )
    fun addMessage(@RequestParam("message") text: String): String {
        val updated = service.addMessage(text)
        // The form itself is reset on the client using hx-on::after-request
        return renderList(updated)
    }

    private fun renderList(items: List<Message>): String =
        buildString {
            append("<ul style='margin:0;padding-left:1.2rem'>")
            for (item in items) append("<li>#${'$'}{item.id}: ${'$'}{escape(item.text)}</li>")
            if (items.isEmpty()) append("<li><em>No messages</em></li>")
            append("</ul>")
        }

    private fun escape(s: String) = buildString {
        for (c in s) append(
            when (c) {
                '<' -> "&lt;"; '>' -> "&gt;"; '&' -> "&amp;"; '"' -> "&quot;"; '\'' -> "&#39;"
                else -> c
            }
        )
    }
}
```

Important aspects for HTMX:

- Both endpoints produce `text/html`.
- `GET /htmx/messages` returns the HTML representation of the message list.
- `POST /htmx/add-message` validates and adds the message and returns the updated list as HTML.
- Clearing the input field is not handled via `hx-swap-oob` but via an `hx-on::after-request` handler in the frontend, which resets the form after a successful request.

**Tasks**

The task controller returns an HTML snippet that contains the entire task list, including buttons for toggling and deleting tasks. The buttons themselves carry `hx-` attributes so that subsequent interactions are also handled via HTMX:

- `hx-post` for creating a task,
- `hx-put` for toggling completion,
- `hx-delete` for deleting tasks,
- `hx-target="#task-list"` / `hx-target="closest tr"` to update either the entire list or a single row,
- `hx-swap="outerHTML"` to replace the container or row with the HTML sent by the server.

**Users**

The user HTMX controller returns:

- a full table of users,
- inline edit forms for a single user row,
- buttons for editing and deleting.

The edit button typically looks like this in the rendered HTML:

```html
<button
    hx-get="/htmx/users/42/edit"
    hx-target="closest tr"
    hx-swap="outerHTML">
    Edit
</button>
```

The server responds with a table row containing a form that is submitted via `hx-put` to update the user and then replaces the row again with the normal, non-editable view after the update succeeds.

#### 5.2.4 JSON API controllers

In addition to the HTMX-specific controllers, the application also exposes JSON endpoints for messages, tasks and users. These controllers are placed under `/api/...` paths and return DTOs instead of HTML.

A simplified example for messages:

```kotlin
@RestController
@RequestMapping("/api/messages")
class MessageApiController(private val service: MessageService) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getMessages(): List<MessageDto> =
        service.getMessages().map { MessageDto.from(it) }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun addMessage(@RequestBody request: CreateMessageRequest): MessageDto {
        val updated = service.addMessage(request.text)
        return MessageDto.from(updated.last())
    }
}
```

This illustrates how the same domain logic can support both HTML-over-the-wire interactions for HTMX and JSON-based APIs for other clients, such as SPAs or mobile apps. The multipurpose backend aspects are discussed in more detail in Chapter 8.

#### 5.2.5 CORS configuration

Because the frontend runs under a different origin (e.g. `localhost:5500`), CORS must be configured to allow cross-origin requests:

```kotlin
@Configuration
class WebCorsConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(
                "http://localhost:63342", "http://127.0.0.1:63342",
                "http://localhost:5500", "http://127.0.0.1:5500"
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    }
}
```

This configuration allows requests from typical local development servers such as VS Code Live Server or IntelliJ’s built-in web server.

### 5.3 Frontend: HTMX dashboard with HTML and CSS

The frontend consists of a static HTML file (`index.html`) and a CSS file (`style.css`). The base styling is provided by the lightweight CSS framework Pico.css, with project-specific adjustments layered on top.

#### 5.3.1 Including HTMX and configuring cross-origin requests

In the `<head>` of the HTML file, HTMX is loaded via CDN and configured to allow cross-origin requests to the backend:

```html
<script src="https://cdn.jsdelivr.net/npm/htmx.org@2.0.7"></script>
<script>
    htmx.config.selfRequestsOnly = false;  // allow cross-origin requests to http://localhost:8080
</script>
```

The configuration `selfRequestsOnly = false` allows HTMX to send requests to a different origin than the one that served the HTML page, in this case the Spring Boot backend at `http://localhost:8080`.

#### 5.3.2 Message Center card

The *Message Center* card shows a list of messages and a form to add a new one:

```html
<form class="compact-form"
      id="new-message-form"
      action="http://localhost:8080/htmx/add-message"
      method="post"
      hx-post="http://localhost:8080/htmx/add-message"
      hx-target="#messages"
      hx-swap="innerHTML"
      hx-on::after-request="if (event.detail.successful) this.reset()">
    <label>
        <span class="field-label">New message</span>
        <input type="text"
               name="message"
               placeholder="Type a message and press Enter"
               required>
    </label>
    <button type="submit" class="primary">
        Send
    </button>
</form>
```

- `hx-post` sends a POST request to `/htmx/add-message` when the form is submitted.
- Form data is sent as `application/x-www-form-urlencoded`.
- `hx-target="#messages"` specifies that the response HTML should be applied to `div#messages`.
- `hx-swap="innerHTML"` replaces only the inner content of the target element.
- `hx-on::after-request="if (event.detail.successful) this.reset()"` resets the form after a successful HTMX request, so that input fields are cleared for the next entry.

The list itself is loaded via HTMX when the card is rendered:

```html
<div id="messages"
     class="message-list"
     hx-get="http://localhost:8080/htmx/messages"
     hx-trigger="load"
     hx-indicator="#messages-indicator">
    Loading messages...
</div>
```

- `hx-get` triggers a GET request to `/htmx/messages`.
- `hx-trigger="load"` ensures that the request is executed as soon as the element has been loaded into the DOM.
- `hx-indicator="#messages-indicator"` links a small spinner to the loading state of the request; HTMX automatically toggles the `htmx-request` class on the indicator element while the request is running.

#### 5.3.3 Task Board card

The *Task Board* card demonstrates CRUD-like behaviour for tasks. It consists of:

- a form to create a new task initialized with `hx-post="/htmx/tasks"`,
- a container that holds the rendered task list, initially loaded with `hx-get="/htmx/tasks"`,
- toggle and delete buttons inside the rendered HTML, using `hx-put` and `hx-delete`.

The form uses a similar pattern as the message form to reset itself after successful submissions:

```html
<form class="compact-form"
      action="http://localhost:8080/htmx/tasks"
      method="post"
      hx-post="http://localhost:8080/htmx/tasks"
      hx-target="#task-list"
      hx-swap="outerHTML"
      hx-on::after-request="if (event.detail.successful) this.reset()">
    <label>
        <span class="field-label">New task</span>
        <input type="text"
               name="title"
               placeholder="Describe a task"
               required>
    </label>
    <button type="submit" class="secondary">
        Add task
    </button>
</form>
```

The container is defined as:

```html
<div id="task-list"
     class="placeholder-box"
     hx-get="http://localhost:8080/htmx/tasks"
     hx-trigger="load"
     hx-swap="outerHTML"
     hx-indicator="#tasks-indicator">
    <p class="muted">
        The initial task list is loaded from the backend when this card becomes visible.
    </p>
</div>
```

The backend returns a `<div id="task-list"> ... </div>` fragment that contains a table or list of tasks, where each row contains HTMX-enabled buttons for toggle and delete actions.

#### 5.3.4 User Card: CRUD for richer objects

The *User Card* shows how HTMX can be used for CRUD operations on a richer domain object with multiple fields.

The create form is defined as follows:

```html
<form class="user-form"
      action="http://localhost:8080/htmx/users"
      method="post"
      hx-post="http://localhost:8080/htmx/users"
      hx-target="#user-list"
      hx-swap="outerHTML"
      hx-on::after-request="if (event.detail.successful) this.reset()">
    <div class="user-form-row">
        <label>
            <span class="field-label">First name</span>
            <input type="text"
                   name="firstName"
                   placeholder="First name"
                   required>
        </label>
        <label>
            <span class="field-label">Last name</span>
            <input type="text"
                   name="lastName"
                   placeholder="Last name"
                   required>
        </label>
    </div>
    <div class="user-form-row">
        <label>
            <span class="field-label">Age</span>
            <input type="number"
                   name="age"
                   min="0"
                   max="130"
                   placeholder="Age">
        </label>
        <label>
            <span class="field-label">Gender</span>
            <select name="gender">
                <option value="">Select gender</option>
                <option value="FEMALE">Female</option>
                <option value="MALE">Male</option>
                <option value="OTHER">Other</option>
                <option value="UNKNOWN">Prefer not to say</option>
            </select>
        </label>
    </div>
    <button type="submit" class="secondary">
        Create user
    </button>
</form>
```

The user list is loaded and updated via:

```html
<div id="user-list"
     class="placeholder-box"
     hx-get="http://localhost:8080/htmx/users"
     hx-trigger="load"
     hx-swap="outerHTML"
     hx-indicator="#users-indicator">
    <p class="muted">
        The initial list of users will be loaded from the backend. Each row provides
        edit and delete actions rendered by the server.
    </p>
</div>
```

On the server side, the `UserHtmxController` renders:

- a table of users,
- per-row **Edit** buttons that use `hx-get="/htmx/users/{id}/edit"` with `hx-target="closest tr"` and `hx-swap="outerHTML"` to switch a row into edit mode,
- per-row **Delete** buttons that use `hx-delete` to remove a user and return an updated table or row.

When editing, the backend returns a table row containing a form that uses `hx-put="/htmx/users/{id}"` and `hx-target="closest tr"` with `hx-swap="outerHTML"`. This way, the edit form is restricted to a single row and replaced with the updated view after the update succeeds.

#### 5.3.5 Live User Search card

The *Live User Search* card demonstrates debounced search and server-side filtering based on multiple query parameters. It consists of two input controls:

- a search field for the name, and
- a drop-down for the gender filter.

The search field is defined as:

```html
<input id="user-search-input"
       type="search"
       name="q"
       placeholder="Type a name (e.g. Alex, Chris, Sam)"
       hx-get="http://localhost:8080/htmx/users/search"
       hx-target="#user-search-results"
       hx-trigger="keyup changed delay:300ms"
       hx-indicator="#user-search-indicator"
       hx-include="#user-search-gender">
```

The gender filter is defined as:

```html
<select id="user-search-gender" name="gender"
        hx-get="http://localhost:8080/htmx/users/search"
        hx-target="#user-search-results"
        hx-trigger="change"
        hx-indicator="#user-search-indicator"
        hx-include="#user-search-input">
    <option value="">All genders</option>
    <option value="FEMALE">Female</option>
    <option value="MALE">Male</option>
    <option value="OTHER">Other</option>
    <option value="UNKNOWN">Prefer not to say</option>
</select>
```

Key aspects:

- Both controls send requests to `/htmx/users/search`.
- `hx-trigger="keyup changed delay:300ms"` ensures that name search is debounced.
- `hx-include` ensures that both `q` and `gender` are always sent, regardless of which control triggered the request.
- The backend returns an HTML fragment with a list or table of matching users, which replaces the contents of `#user-search-results`.

### 5.4 User flow

From the user’s perspective, the flow looks as follows:

1. The user opens the frontend page via a local development server (for example `http://127.0.0.1:5500/index.html`).
2. When the page loads, HTMX fires GET requests with `hx-trigger="load"` for the message list, task list and user list; the cards are filled with data without refreshing the page.
3. The user enters a new message into the form and clicks “Send”.
    - HTMX sends a POST request to `/htmx/add-message`.
    - The backend adds the new message to the in-memory list and returns the updated list as HTML.
    - HTMX updates the `#messages` container and, via `hx-on::after-request`, the form is reset so that the input field is cleared.
4. The user creates tasks on the Task Board and toggles them between open and completed.
    - All operations result in partial page updates driven by HTMX.
5. The user creates, edits and deletes users in the User Card.
    - Inline editing is realized by swapping a single table row between view and edit mode using HTMX attributes.
6. The user types into the live search field and/or selects a gender filter.
    - HTMX sends debounced GET requests to the search endpoint with the current query parameters.
    - Matching users are rendered server-side and swapped into the results area.

All of this happens without a full page reload. Business logic and HTML generation remain entirely on the server side; the JavaScript code in the frontend is reduced to including HTMX and a small script for error handling.

---

## 6 Extended scenarios with HTMX in Kotlin

The current example application covers a significantly richer set of HTMX use cases than the initial minimal message example: it demonstrates CRUD-like interactions, inline editing, and live search. Nevertheless, many further scenarios could be explored in larger projects.

### 6.1 Full CRUD interfaces

The demo already implements full CRUD functionality for users and a near-CRUD set for tasks (create, read, update toggling, delete). In more complex applications, this pattern can be extended to entire management interfaces, where:

- tables are paginated,
- modal dialogs or inline forms are used for editing,
- and complex validation logic is shown via dynamically rendered error messages.

HTMX provides suitable attributes (`hx-put`, `hx-delete`, `hx-target`, `hx-swap`) for these operations. Combined with a Kotlin backend (Spring Boot or Ktor), classical CRUD applications can be built where rows in a table or individual forms are dynamically updated.

### 6.2 Pagination and lazy loading

For larger data sets, lists could be paginated. HTMX can be used to:

- load additional items via a “Load more” button,
- or automatically load further content when scrolling (using appropriate triggers and events).

In the demo application, lists are small and therefore loaded in a single request. Pagination and infinite scrolling would be natural future extensions.

### 6.3 Validation and advanced error handling

The demo already integrates basic validation and error handling:

- HTML5 validation attributes such as `required` and range restrictions (for example for age),
- server-side validation with `require` and type-safe mapping in Kotlin services,
- a global error banner that reacts to HTMX error events (Chapter 9).

Further extensions could include:

- using Bean Validation annotations on DTOs or entities,
- returning HTML fragments that contain field-specific error messages,
- integrating these fragments into the UI via dedicated `hx-target` and `hx-swap` attributes.

### 6.4 Integration with template engines

In the demo application, HTML fragments are built manually as strings in the controllers. In a real-world project, using a template engine (such as Thymeleaf, Mustache or FreeMarker) is usually more maintainable and expressive. HTMX can be combined with server-side templates by defining partial templates (for example fragments for table rows or card contents) that are rendered and returned as HTML snippets.

---

## 7 Comparison and evaluation

### 7.1 HTMX + Kotlin vs. classical SPA

Compared to a classical SPA architecture, the following picture emerges:

**Advantages of HTMX + Kotlin:**

- lower complexity in the frontend: no large JavaScript application is required,
- business logic stays in the backend and remains strongly typed,
- a separate JSON API layer is not strictly necessary; the server can return HTML directly,
- simpler SEO and faster initial load times due to server-side rendering,
- the same domain services can still be reused by JSON APIs for other clients.

**Disadvantages and limitations:**

- very complex client-side interactions and offline capabilities are harder to achieve with pure HTML fragments,
- advanced client-side state management is not the primary focus of this approach,
- frontend developers who are used to SPA frameworks need to adapt to a different style of development.

### 7.2 HTMX + Kotlin vs. pure server-side rendering

Compared to classic server-side rendering without HTMX, using HTMX offers:

- a better user experience: parts of the page can be updated without reloading the entire document,
- more granular interactions without writing large amounts of JavaScript,
- a familiar programming model (controllers, templates) extended with fragment-based updates.

The additional effort is mostly in adding appropriate `hx-*` attributes and designing endpoints that return HTML fragments instead of complete pages.

---

## 8 Multipurpose backends and coexistence with HTMX

In many real-world systems the backend is not built exclusively for a single web user interface, but serves multiple types of clients at the same time. Typical consumers include:

- browser-based applications (classical server-rendered pages, SPAs, or HTMX-based UIs),
- native mobile apps,
- other backend services (for example via REST or gRPC),
- integration or batch processes.

In such a setting the backend is usually expected to expose a **stable JSON API** that is independent of any particular frontend technology. When HTMX is introduced on top of this, the question arises how HTML-over-the-wire endpoints can coexist with existing JSON endpoints without making the architecture brittle.

This section outlines three common strategies and discusses their trade-offs. It then explains which approach is used in the project.

### 8.1 Separation of concerns: shared services, separate presentation layers

A useful way to structure a multipurpose backend is to distinguish between:

- a **shared domain and service layer** (business logic, validation, persistence),
- multiple **presentation layers** on top of it:
    - one or more JSON APIs (for SPAs, mobile apps, other services),
    - one or more HTML/HTMX endpoints for server-driven web interfaces.

With this separation the business logic is implemented only once, while different clients can receive data in the representation that fits them best.

### 8.2 Strategy 1: Separate endpoints and controllers (recommended)

The most straightforward and maintainable approach is to expose **different endpoints** for JSON and HTMX/HTML, even if they use the same service layer internally. For example:

```kotlin
@RestController
@RequestMapping("/api/messages")
class MessageApiController(private val service: MessageService) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getMessagesJson(): List<MessageDto> =
        service.getMessages().map { MessageDto.from(it) }
}
```

and

```kotlin
@RestController
@RequestMapping("/htmx/messages")
class MessageHtmxController(private val service: MessageService) {

    @GetMapping(produces = [MediaType.TEXT_HTML_VALUE])
    fun getMessagesHtml(): String =
        renderList(service.getMessages())
}
```

Characteristics of this approach:

- The **service layer** (`MessageService`) is shared.
- The JSON API (`/api/messages`) is clearly separated from the HTMX endpoints (`/htmx/messages`).
- Documentation, monitoring, and testing can treat the JSON API like any other public interface, while the HTMX endpoints can be considered part of a specific web user interface.
- There is no hidden coupling to a specific frontend; HTMX is just one additional consumer.

For multipurpose backends this strategy usually provides the best clarity and long-term maintainability, because responsibilities are explicit and each endpoint has a single, well-defined contract.

### 8.3 Strategy 2: Content negotiation via `Accept` header

A second option is to use **HTTP content negotiation** on the same URL. Spring Boot allows defining multiple handler methods for the same path but different `produces` values:

```kotlin
@GetMapping("/messages", produces = [MediaType.APPLICATION_JSON_VALUE])
fun getMessagesJson(): List<MessageDto> = ...

@GetMapping("/messages", produces = [MediaType.TEXT_HTML_VALUE])
fun getMessagesHtml(): String = ...
```

Clients that send `Accept: application/json` will receive JSON, whereas HTMX can be configured (or left with its defaults) to request `text/html`.

Advantages:

- The URL space stays smaller: `/messages` is the single logical resource.
- The distinction between representations uses standard HTTP mechanisms.

Disadvantages:

- Debugging can be more difficult if the wrong `Accept` header is sent.
- The behaviour of an endpoint is no longer obvious when looking only at the URL; one has to inspect the content types as well.
- Tooling (for example API documentation generators) may require additional configuration to represent both variants correctly.

This strategy is technically sound but often better suited for mature teams and projects that already use content negotiation consistently.

### 8.4 Strategy 3: Switching behaviour based on the `HX-Request` header

HTMX automatically sends a request header `HX-Request: true` for its requests. A controller can inspect this header and return different representations from a single method:

```kotlin
@GetMapping("/messages")
fun messages(request: HttpServletRequest): Any {
    val messages = service.getMessages()
    return if (request.getHeader("HX-Request") == "true") {
        renderList(messages)             // HTML for HTMX
    } else {
        messages.map { MessageDto.from(it) }  // JSON for other clients
    }
}
```

Although this can be convenient for small applications and prototypes, it has important downsides in a multipurpose backend:

- One endpoint now has **two responsibilities** (API and view rendering).
- The response format is determined by a non-standard header rather than HTTP content negotiation.
- It becomes harder to document, test, and evolve the contract independently for different clients.

For these reasons this strategy is usually not recommended for larger systems, even though it can be acceptable in simple demos.

### 8.5 Relation to the Backend-for-Frontend pattern

In more complex environments, it is common to introduce a **Backend-for-Frontend (BFF)** layer that sits in front of shared domain services. A BFF is tailored to the needs of a specific user interface (for example a web UI or a mobile app) and is responsible for:

- aggregating data from multiple internal services,
- shaping responses in a frontend-friendly format (HTML for HTMX, JSON suited to the UI’s view models),
- enforcing UI-specific security and performance requirements.

HTMX endpoints fit naturally into such a BFF layer: they can expose HTML fragments optimised for the HTMX-based UI, while other backends or APIs continue to use JSON or other protocols.

### 8.6 Approach used in this project

The example application in this project is intentionally small and focused on demonstrating HTMX with a Kotlin/Spring Boot backend. It currently provides **HTML endpoints specifically designed for the HTMX-based frontend** and illustrative JSON endpoints. The design is compatible with the **separate endpoints / controllers** strategy described above:

- The existing services and repository layer are independent of the presentation format.
- HTMX-specific controllers are used for HTML fragments.
- Additional controllers under `/api/...` can expose the same data as JSON for other clients.

In other words, the project follows the idea of a shared service layer with distinct controllers for different representations, which is the recommended approach when HTMX coexists with other consumers in a multipurpose backend.

---

## 9 Error handling in an HTMX + Kotlin application

Error handling is a crucial aspect of user experience in web applications. In an HTMX + Kotlin setting there are several layers where errors can occur:

- on the client side (for example invalid input that fails HTML5 validation),
- in the network (for example when the backend is not reachable),
- and on the server side (for example exceptions or 5xx responses).

The demo application implements a combination of these mechanisms to give meaningful feedback to the user without leaving the current page.

### 9.1 Client-side validation

The HTML forms in the dashboard use built-in browser validation features:

- Required fields are marked with the `required` attribute (for example the message text, task title, first and last name).
- Numeric fields have ranges (for example `age` with `min="0"` and `max="130"`).

Before HTMX sends a request, the browser performs its own validation. If input is missing or invalid, the request is blocked and the user sees a standard validation message. This reduces the number of invalid requests that reach the server.

### 9.2 Server-side validation and HTTP status codes

On the server, the Kotlin services use checks such as `require(text.isNotBlank())` to validate inputs. If invalid data reaches the backend, an exception is thrown and translated by Spring Boot into an appropriate HTTP error response (typically a 400 or 500 status code, depending on the configuration).

For illustrative purposes, the demo keeps error handling in the controllers fairly simple. In a production system, validation errors would usually be mapped to specific HTTP status codes and, for HTMX requests, to HTML fragments that contain error messages rendered next to the relevant input fields.

### 9.3 HTMX error events

HTMX provides several events that fire when a request fails or times out:

- `htmx:sendError` - raised when a request cannot be sent (for example network errors, DNS issues, CORS problems).
- `htmx:responseError` - raised when the server responds with a non-2xx HTTP status code (for example 4xx or 5xx).
- `htmx:timeout` - raised when a request exceeds the configured timeout.
- `htmx:afterRequest` - raised after every request, regardless of whether it was successful; the `event.detail.successful` flag indicates success.

The demo application listens for these events in a small JavaScript snippet at the bottom of `index.html` and shows a global error banner at the top of the page:

```html
<script>
    // Tracks whether we have a persistent "server unreachable" situation.
    let htmxServerIssue = false;

    // Network-level errors: server not reachable, DNS issues, CORS, etc.
    document.body.addEventListener('htmx:sendError', function (event) {
        htmxServerIssue = true;
        showHtmxError('Network error: could not reach the server.', true);
    });

    // HTTP errors: 4xx/5xx status codes from the server.
    document.body.addEventListener('htmx:responseError', function (event) {
        const status = event.detail.xhr ? event.detail.xhr.status : 'unknown';
        showHtmxError('Server error: request failed with status ' + status + '.', false);
    });

    // Request timeout: server took too long to respond.
    document.body.addEventListener('htmx:timeout', function (event) {
        showHtmxError('Request timeout: the server took too long to respond.', false);
    });

    // Successful request after a server issue: hide the banner again.
    document.body.addEventListener('htmx:afterRequest', function (event) {
        if (event.detail.successful && htmxServerIssue) {
            hideHtmxError();
            htmxServerIssue = false;
        }
    });

    function showHtmxError(message, persistent) {
        const banner = document.getElementById('htmx-error-banner');
        const text = document.getElementById('htmx-error-text');
        if (!banner || !text) return;

        text.textContent = message;
        banner.hidden = false;

        // Cancel any previous auto-hide
        window.clearTimeout(banner._hideTimeout);

        // Only auto-hide if not persistent
        if (!persistent) {
            banner._hideTimeout = window.setTimeout(function () {
                banner.hidden = true;
            }, 6000);
        }
    }

    function hideHtmxError() {
        const banner = document.getElementById('htmx-error-banner');
        if (!banner) return;
        banner.hidden = true;
    }
</script>
```

The corresponding banner is defined at the top of the dashboard:

```html
<div id="htmx-error-banner" class="error-banner" hidden>
    <strong>Warning:</strong>
    <span id="htmx-error-text">An error occurred while talking to the server.</span>
</div>
```

Key design decisions:

- **Network failures** (for example when the backend is down) set the flag `htmxServerIssue = true` and show the banner as *persistent*.
    - The banner then remains visible until a successful HTMX request is processed, indicating that connectivity has been restored.
- **Server-side errors** (response errors, timeouts) show a banner that automatically hides after a few seconds, because the application as a whole may still be functional.
- The global banner avoids the typical “nothing happens” experience when a background request fails.

### 9.4 Clearing input fields after successful requests

For a good user experience it is often desirable to clear input fields after successfully submitting a form. This is implemented using the `hx-on::after-request` attribute on the relevant forms:

```html
<form
    ...
    hx-on::after-request="if (event.detail.successful) this.reset()">
    ...
</form>
```

- The handler is executed after each HTMX request originating from the form.
- The `event.detail.successful` flag is `true` for 2xx responses; in this case `this.reset()` clears the form fields.
- If the request fails (for example due to a network error), the form contents remain in place so that the user can try again or adjust the input.

This pattern is used consistently in the Message Center, Task Board and User Card create forms.

---

## 10 Overview of HTMX attributes used in the demo

This chapter summarises the HTMX attributes used in the demo dashboard and explains their semantics and typical usage.

### 10.1 Request attributes

- **`hx-get`**  
  Sends an HTTP GET request to the specified URL. In the demo it is used for:
    - loading initial lists (`hx-get="/htmx/messages"`, `"/htmx/tasks"`, `"/htmx/users"`),
    - running the live user search (`hx-get="/htmx/users/search"`),
    - switching a user row into edit mode (`hx-get="/htmx/users/{id}/edit"`).

- **`hx-post`**  
  Sends an HTTP POST request, typically including form data. In the demo it is used for:
    - creating messages (`hx-post="/htmx/add-message"`),
    - creating tasks (`hx-post="/htmx/tasks"`),
    - creating users (`hx-post="/htmx/users"`).

- **`hx-put`**  
  Sends an HTTP PUT request. It is used for update operations, for example:
    - updating a user via the inline edit form in the User Card.

- **`hx-delete`**  
  Sends an HTTP DELETE request. It is used to:
    - delete tasks,
    - delete users.

### 10.2 Targeting and swapping

- **`hx-target`**  
  Specifies which element should be updated with the server response. It accepts any CSS selector, including:
    - `#messages`, `#task-list`, `#user-list`, `#user-search-results` for updating entire containers,
    - `closest tr` to restrict updates to the table row that contains the button or form.

- **`hx-swap`**  
  Controls how the response HTML is applied to the target element. Common values in the demo:
    - `innerHTML` - replaces only the inner content of the target element (used for the message list).
    - `outerHTML` - replaces the target element itself (used for task and user list containers and for individual table rows in inline editing).

- **`hx-swap-oob`**  
  Enables *out-of-band* swaps, where elements in the response are applied to different targets than the main `hx-target`. While the final version of the demo no longer uses `hx-swap-oob`, it was initially used to clear the message input field by sending a replacement `<input>` element from the server. The concept remains important for cases where multiple parts of the page must be updated in response to a single request.

### 10.3 Triggers and inclusion of extra fields

- **`hx-trigger`**  
  Defines when a request is initiated. Important usages in the demo:
    - `hx-trigger="load"` - fire once when the element is inserted into the DOM (initial list loading).
    - `hx-trigger="click"` - implicit for buttons when not explicitly set.
    - `hx-trigger="change"` - send a request when the value of a select element changes (gender filter in live search).
    - `hx-trigger="keyup changed delay:300ms"` - debounced live search for users, sending a request 300 ms after the user stops typing.

- **`hx-include`**  
  Specifies additional elements whose values should be included in the request. In the demo:
    - The live user search input and gender select mutually include each other, so that both `q` and `gender` are always present in the query parameters, regardless of which control triggered the request.

### 10.4 Indicators and event handlers

- **`hx-indicator`**  
  Links a DOM element (for example a spinner) to the request. HTMX automatically toggles the `htmx-request` class on the indicator element during the lifecycle of the request. In the demo:
    - Small circular spinners are shown next to the controls while messages, tasks, users or search results are being loaded.

- **`hx-on` / `hx-on::event`**  
  Attaches inline JavaScript handlers to HTMX events. The demo uses the modern `hx-on::after-request` notation:

  ```html
  hx-on::after-request="if (event.detail.successful) this.reset()"
  ```

  This pattern is used to reset forms after successful submissions. In addition, global event listeners in a `<script>` block are used to react to error events (`htmx:sendError`, `htmx:responseError`, `htmx:timeout`) and show or hide the error banner.

---

## 11 Conclusion and outlook

This project has shown how HTMX can be used in combination with a Kotlin backend to build interactive web interfaces without heavy SPA frameworks. Building on an existing tutorial that combines HTMX with Kotlin, an own example application using Spring Boot and a static HTMX frontend was developed and analysed.

The implementation demonstrates that:

- HTMX integrates smoothly with Spring Boot,
- the HTML-over-the-wire approach works well with Kotlin,
- interactive user interfaces are possible without large JavaScript code bases,
- richer use cases such as CRUD operations and live search can be implemented with relatively little frontend complexity,
- HTMX-based endpoints can coexist with JSON APIs in a multipurpose backend.

HTMX and Kotlin are particularly suitable for applications where:

- frontend complexity should remain limited,
- server-side logic is central,
- short development cycles and simple deployments are important,
- and developers prefer to keep most business logic in a type-safe backend.

For more complex scenarios - such as very rich client-side interactions, offline support, or highly dynamic UIs - classical SPA approaches may still be preferable. Hybrid architectures, where some parts are implemented as SPAs and others with HTMX, are also an interesting option.

### 11.1 Possible extensions

Future work on the demo application could include:

- persisting messages, tasks and users in a database (for example PostgreSQL) instead of in-memory lists,
- adding authentication and user-specific data,
- integrating form validation and error message display based on Bean Validation and template fragments,
- using a server-side template engine for better structure and reusability of HTML fragments,
- adding pagination and infinite scrolling for large lists,
- exploring additional HTMX features such as `hx-push-url`, `hx-boost`, or fine-grained loading indicators.

Through these extensions, the practicality of HTMX in real-world Kotlin applications could be evaluated in more depth.

---

## 12 References

[1] Codersee: *A Quick Guide to HTMX in Kotlin* (blog article, accessed 2026).  
[2] HTMX: Official documentation, https://htmx.org (accessed 2026).  
[3] HTMX: GitHub repository and release notes, especially version 1.0.0 (accessed 2026).  
[4] intercooler.js: Project documentation and archive, original hypermedia/AJAX library preceding HTMX (accessed 2026).  
[5] React: Official documentation and history, https://react.dev (accessed 2026).  
[6] Vue.js: Official documentation and release history, https://vuejs.org (accessed 2026).  
[7] Angular: Official documentation, https://angular.io (accessed 2026).  
[8] Spring Boot: Reference documentation, https://spring.io/projects/spring-boot (accessed 2026).  
[9] Kotlin: Language reference, https://kotlinlang.org/docs/reference/ (accessed 2026).  
