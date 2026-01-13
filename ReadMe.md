# HTMX Kotlin Demo

This project demonstrates how to integrate [HTMX](https://htmx.org/) with a Kotlin/Spring Boot backend.  
The backend returns HTML fragments instead of JSON, and the frontend uses HTMX attributes in plain HTML to dynamically update parts of the page without a full reload.

## Project structure

```text
htmx-kotlin-demo/
├─ htmx-be/   # Kotlin + Spring Boot backend
└─ htmx-fe/   # Static frontend (HTML + CSS + HTMX)
```

## Backend (htmx-be)

The backend is a Spring Boot application written in Kotlin.  
It exposes two endpoints:

- `GET /messages` – returns an HTML `<ul>` with all messages
- `POST /add-message` – adds a new message and returns the updated list + an out-of-band input reset

### Run backend

From the `htmx-be` directory:

```bash
./gradlew bootRun
# or on Windows:
gradlew.bat bootRun
```

The application will be available at:

```text
http://localhost:8080
```

## Frontend (htmx-fe)

The frontend is a simple static page that loads HTMX via CDN and talks to the backend.

Key HTMX attributes:

- `hx-get` + `hx-trigger="load"` to initially load the message list
- `hx-post` + `hx-target` + `hx-swap` to submit new messages and update the list

### Run frontend

Serve `index.html` from `htmx-fe` using a static file server, for example:

- VS Code **Live Server** (default: `http://127.0.0.1:5500`)
- IntelliJ/WebStorm built-in server (e.g. `http://localhost:63342`)

These origins are allowed by the backend CORS configuration.

Open `index.html` in the browser via the dev server URL (not via `file://`).

## Purpose

This demo is used in the context of a university project to show:

- how HTMX can be integrated into a Kotlin/Spring Boot application,
- how HTML-over-the-wire works in practice,
- and why HTMX can be a lightweight alternative to full SPA frameworks for many use cases.
