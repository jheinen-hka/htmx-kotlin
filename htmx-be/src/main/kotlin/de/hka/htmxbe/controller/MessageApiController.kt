package de.hka.htmxbe.controller

import de.hka.htmxbe.dto.MessageDto
import de.hka.htmxbe.service.MessageService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(
    origins = [
        "http://localhost:63342", "http://127.0.0.1:63342",
        "http://localhost:5500", "http://127.0.0.1:5500"
    ]
)
class MessageApiController(
    private val service: MessageService
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getMessages(): List<MessageDto> =
        service.getMessages().map(MessageDto::fromEntity)

    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun addMessage(@RequestBody request: CreateMessageRequest): List<MessageDto> {
        val updated = service.addMessage(request.text)
        return updated.map(MessageDto::fromEntity)
    }

    data class CreateMessageRequest(
        val text: String
    )
}
