package de.hka.htmxbe.controller

import de.hka.htmxbe.service.MessageService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin(origins = ["http://localhost:63342", "http://127.0.0.1:63342"])
class MessageController(private val service: MessageService) {

    @GetMapping("/messages", produces = [MediaType.TEXT_HTML_VALUE])
    fun messagesHtml(): String = renderList(service.getMessages())

    @PostMapping("/add-message", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE], produces = [MediaType.TEXT_HTML_VALUE])
    fun addMessage(@RequestParam("message") text: String): String {
        val updated = service.addMessage(text)

        return """
        ${renderList(updated)}
        <input type="text" name="message" placeholder="Your Message" value="" hx-swap-oob="true">
        """.trimIndent()
    }

    private fun renderList(items: List<de.hka.htmxbe.entity.Message>): String =
        buildString {
            append("<ul style='margin:0;padding-left:1.2rem'>")
            for (m in items) append("<li>#${m.id}: ${escape(m.text)}</li>")
            if (items.isEmpty()) append("<li><em>Keine Nachrichten</em></li>")
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