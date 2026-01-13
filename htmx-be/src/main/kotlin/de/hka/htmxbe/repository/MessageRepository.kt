package de.hka.htmxbe.repository

import de.hka.htmxbe.entity.Message
import org.springframework.stereotype.Repository
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong

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