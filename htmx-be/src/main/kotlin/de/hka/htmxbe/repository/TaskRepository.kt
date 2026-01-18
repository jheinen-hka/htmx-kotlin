package de.hka.htmxbe.repository

import de.hka.htmxbe.entity.Task
import org.springframework.stereotype.Repository
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong

@Repository
class TaskRepository {

    private val seq = AtomicLong(1)
    private val data = CopyOnWriteArrayList<Task>().apply {
        add(Task(seq.getAndIncrement(), "Explore HTMX basics", done = true))
        add(Task(seq.getAndIncrement(), "Integrate HTMX with Kotlin backend"))
        add(Task(seq.getAndIncrement(), "Extend demo with a Task Board"))
    }

    fun findAll(): List<Task> = data.toList()

    fun add(title: String): Task {
        val task = Task(seq.getAndIncrement(), title)
        data.add(task)
        return task
    }

    fun toggleDone(id: Long): Task? {
        val index = data.indexOfFirst { it.id == id }
        if (index < 0) return null
        val current = data[index]
        val updated = current.copy(done = !current.done)
        data[index] = updated
        return updated
    }

    fun delete(id: Long): Boolean {
        return data.removeIf { it.id == id }
    }
}