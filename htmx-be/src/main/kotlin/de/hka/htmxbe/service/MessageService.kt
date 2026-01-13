package de.hka.htmxbe.service

import de.hka.htmxbe.entity.Message
import de.hka.htmxbe.repository.MessageRepository
import org.springframework.stereotype.Service

@Service
class MessageService(private val repo: MessageRepository) {

    fun getMessages(): List<Message> = repo.findAll()

    fun addMessage(text: String): List<Message> {
        require(text.isNotBlank()) { "message must not be blank" }
        repo.add(text.trim())
        return repo.findAll()
    }
}