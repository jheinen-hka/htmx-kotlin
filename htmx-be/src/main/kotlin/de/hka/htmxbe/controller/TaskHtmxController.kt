package de.hka.htmxbe.controller

import de.hka.htmxbe.entity.Task
import de.hka.htmxbe.service.TaskService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/htmx")
@CrossOrigin(
    origins = [
        "http://localhost:63342", "http://127.0.0.1:63342",
        "http://localhost:5500", "http://127.0.0.1:5500"
    ]
)
class TaskHtmxController(
    private val service: TaskService
) {

    // Adjust this base URL if your backend runs on a different host/port
    private val backendBaseUrl = "http://localhost:8080"

    @GetMapping("/tasks", produces = [MediaType.TEXT_HTML_VALUE])
    fun getTasksHtml(): String =
        renderTaskListContainer(service.getTasks())

    @PostMapping(
        "/tasks",
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.TEXT_HTML_VALUE]
    )
    fun createTask(@RequestParam("title") title: String): String {
        val updated = service.createTask(title)
        return renderTaskListContainer(updated)
    }

    @PutMapping("/tasks/{id}", produces = [MediaType.TEXT_HTML_VALUE])
    fun toggleTask(@PathVariable id: Long): ResponseEntity<String> {
        val updated = service.toggleTask(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(renderTaskItem(updated))
    }

    @DeleteMapping("/tasks/{id}", produces = [MediaType.TEXT_HTML_VALUE])
    fun deleteTask(@PathVariable id: Long): ResponseEntity<String> {
        val removed = service.deleteTask(id)
        return if (removed) {
            // HTMX will replace the <li> with an empty string because of hx-swap="outerHTML"
            ResponseEntity.ok("")
        } else {
            ResponseEntity.notFound().build()
        }
    }

    private fun renderTaskListContainer(tasks: List<Task>): String =
        buildString {
            append("<div id=\"task-list\" class=\"placeholder-box\">")
            if (tasks.isEmpty()) {
                append("<p class=\"muted\">No tasks yet. Add a task to get started.</p>")
            } else {
                append("<ul class=\"muted-list\">")
                for (task in tasks) {
                    append(renderTaskItem(task))
                }
                append("</ul>")
            }
            append("</div>")
        }

    private fun renderTaskItem(task: Task): String =
        buildString {
            append("<li data-task-id=\"${task.id}\">")
            append("<span>")
            if (task.done) {
                append("<strong>[✓]</strong> ")
                append("<s>${escape(task.title)}</s>")
            } else {
                append("<strong>[ ]</strong> ")
                append(escape(task.title))
            }
            append("</span>")
            append(" ")

            // Toggle button
            append(
                """
                <button 
                    class="secondary outline small"
                    hx-put="$backendBaseUrl/htmx/tasks/${task.id}"
                    hx-target="closest li"
                    hx-swap="outerHTML">
                    Toggle
                </button>
                """.trimIndent()
            )

            append(" ")

            // Delete button
            append(
                """
                <button 
                    class="secondary outline small"
                    hx-delete="$backendBaseUrl/htmx/tasks/${task.id}"
                    hx-target="closest li"
                    hx-swap="outerHTML"
                    hx-confirm="Delete this task?">
                    Delete
                </button>
                """.trimIndent()
            )

            append("</li>")
        }

    private fun escape(input: String): String =
        buildString {
            for (c in input) {
                append(
                    when (c) {
                        '<' -> "&lt;"
                        '>' -> "&gt;"
                        '&' -> "&amp;"
                        '"' -> "&quot;"
                        '\'' -> "&#39;"
                        else -> c
                    }
                )
            }
        }
}
