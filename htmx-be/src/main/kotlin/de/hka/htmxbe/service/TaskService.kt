package de.hka.htmxbe.service

import de.hka.htmxbe.entity.Task
import de.hka.htmxbe.repository.TaskRepository
import org.springframework.stereotype.Service

@Service
class TaskService(
    private val repo: TaskRepository
) {

    fun getTasks(): List<Task> = repo.findAll()

    fun createTask(title: String): List<Task> {
        require(title.isNotBlank()) { "title must not be blank" }
        repo.add(title.trim())
        return repo.findAll()
    }

    fun createTaskAndReturnSingle(title: String): Task {
        require(title.isNotBlank()) { "title must not be blank" }
        return repo.add(title.trim())
    }

    fun toggleTask(id: Long): Task? =
        repo.toggleDone(id)

    fun deleteTask(id: Long): Boolean =
        repo.delete(id)
}