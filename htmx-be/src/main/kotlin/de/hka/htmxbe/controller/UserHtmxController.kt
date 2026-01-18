package de.hka.htmxbe.controller

import de.hka.htmxbe.entity.Gender
import de.hka.htmxbe.entity.User
import de.hka.htmxbe.service.UserService
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
class UserHtmxController(
    private val service: UserService
) {

    // Adjust this base URL if your backend runs on a different host/port
    private val backendBaseUrl = "http://localhost:8080"

    @GetMapping("/users", produces = [MediaType.TEXT_HTML_VALUE])
    fun getUsersHtml(): String =
        renderUserListContainer(service.getUsers())

    @PostMapping(
        "/users",
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.TEXT_HTML_VALUE]
    )
    fun createUser(
        @RequestParam("firstName") firstName: String,
        @RequestParam("lastName") lastName: String,
        @RequestParam("age") ageRaw: String?,
        @RequestParam("gender") genderRaw: String?
    ): String {
        val age = ageRaw?.takeIf { it.isNotBlank() }?.toIntOrNull()
        val gender = parseGender(genderRaw)

        val updated = service.createUser(
            UserService.CreateUserCommand(
                firstName = firstName,
                lastName = lastName,
                age = age,
                gender = gender
            )
        )

        return renderUserListContainer(updated)
    }

    @GetMapping("/users/{id}/row", produces = [MediaType.TEXT_HTML_VALUE])
    fun getUserRow(@PathVariable id: Long): ResponseEntity<String> {
        val user = service.getUsers().firstOrNull { it.id == id }
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(renderUserRow(user))
    }

    @GetMapping("/users/{id}/edit", produces = [MediaType.TEXT_HTML_VALUE])
    fun editUserRow(@PathVariable id: Long): ResponseEntity<String> {
        val user = service.getUsers().firstOrNull { it.id == id }
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(renderUserEditRow(user))
    }

    @PutMapping("/users/{id}", produces = [MediaType.TEXT_HTML_VALUE])
    fun updateUser(
        @PathVariable id: Long,
        @RequestParam("firstName") firstName: String,
        @RequestParam("lastName") lastName: String,
        @RequestParam("age") ageRaw: String?,
        @RequestParam("gender") genderRaw: String?
    ): ResponseEntity<String> {
        val age = ageRaw?.takeIf { it.isNotBlank() }?.toIntOrNull()
        val gender = parseGender(genderRaw)

        val updated = service.updateUser(
            id,
            UserService.UpdateUserCommand(
                firstName = firstName,
                lastName = lastName,
                age = age,
                gender = gender
            )
        ) ?: return ResponseEntity.notFound().build()

        // After update, return the normal display row again
        return ResponseEntity.ok(renderUserRow(updated))
    }

    @DeleteMapping("/users/{id}", produces = [MediaType.TEXT_HTML_VALUE])
    fun deleteUser(@PathVariable id: Long): ResponseEntity<String> {
        val removed = service.deleteUser(id)
        return if (removed) {
            // HTMX will remove the <tr> because of hx-swap="outerHTML" on the row
            ResponseEntity.ok("")
        } else {
            ResponseEntity.notFound().build()
        }
    }

    private fun parseGender(value: String?): Gender =
        when (value?.uppercase()) {
            "FEMALE" -> Gender.FEMALE
            "MALE" -> Gender.MALE
            "OTHER" -> Gender.OTHER
            else -> Gender.UNKNOWN
        }

    private fun renderUserListContainer(users: List<User>): String =
        buildString {
            append("<div id=\"user-list\" class=\"placeholder-box\">")
            if (users.isEmpty()) {
                append("<p class=\"muted\">No users yet. Use the form above to create a user.</p>")
            } else {
                append("<table role=\"grid\">")
                append("<thead>")
                append("<tr>")
                append("<th>First name</th>")
                append("<th>Last name</th>")
                append("<th>Age</th>")
                append("<th>Gender</th>")
                append("<th>Actions</th>")
                append("</tr>")
                append("</thead>")
                append("<tbody>")
                for (user in users) {
                    append(renderUserRow(user))
                }
                append("</tbody>")
                append("</table>")
            }
            append("</div>")
        }

    private fun renderUserRow(user: User): String =
        buildString {
            append("<tr data-user-id=\"${user.id}\">")
            append("<td>${escape(user.firstName)}</td>")
            append("<td>${escape(user.lastName)}</td>")
            append("<td>${user.age ?: ""}</td>")
            append("<td>${user.gender}</td>")
            append("<td>")
            append(
                """
                <button 
                    class="secondary outline small"
                    hx-get="$backendBaseUrl/htmx/users/${user.id}/edit"
                    hx-target="closest tr"
                    hx-swap="outerHTML">
                    Edit
                </button>
                """.trimIndent()
            )
            append(" ")
            append(
                """
                <button 
                    class="secondary outline small"
                    hx-delete="$backendBaseUrl/htmx/users/${user.id}"
                    hx-target="closest tr"
                    hx-swap="outerHTML"
                    hx-confirm="Delete this user?">
                    Delete
                </button>
                """.trimIndent()
            )
            append("</td>")
            append("</tr>")
        }

    private fun renderUserEditRow(user: User): String =
        buildString {
            append("<tr data-user-id=\"${user.id}\">")
            append("<td>")
            append(
                """
                <input type="text" 
                       name="firstName" 
                       value="${escapeAttribute(user.firstName)}"
                       required>
                """.trimIndent()
            )
            append("</td>")
            append("<td>")
            append(
                """
                <input type="text" 
                       name="lastName" 
                       value="${escapeAttribute(user.lastName)}"
                       required>
                """.trimIndent()
            )
            append("</td>")
            append("<td>")
            append(
                """
                <input type="number" 
                       name="age" 
                       min="0" 
                       max="130" 
                       value="${user.age ?: ""}">
                """.trimIndent()
            )
            append("</td>")
            append("<td>")
            append("<select name=\"gender\">")
            append(option("FEMALE", "Female", user.gender))
            append(option("MALE", "Male", user.gender))
            append(option("OTHER", "Other", user.gender))
            append(option("UNKNOWN", "Prefer not to say", user.gender))
            append("</select>")
            append("</td>")
            append("<td>")
            append(
                """
                <button 
                    class="secondary small"
                    hx-put="$backendBaseUrl/htmx/users/${user.id}"
                    hx-target="closest tr"
                    hx-swap="outerHTML"
                    hx-include="closest tr">
                    Save
                </button>
                """.trimIndent()
            )
            append(" ")
            append(
                """
                <button 
                    class="secondary outline small"
                    type="button"
                    hx-get="$backendBaseUrl/htmx/users/${user.id}/row"
                    hx-target="closest tr"
                    hx-swap="outerHTML">
                    Cancel
                </button>
                """.trimIndent()
            )
            append("</td>")
            append("</tr>")
        }

    private fun option(value: String, label: String, selectedGender: Gender): String {
        val selectedAttr = if (selectedGender.name == value) " selected" else ""
        return """<option value="$value"$selectedAttr>$label</option>"""
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

    private fun escapeAttribute(input: String): String =
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