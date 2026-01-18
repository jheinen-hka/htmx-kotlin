package de.hka.htmxbe.controller

import de.hka.htmxbe.dto.TaskDto
import de.hka.htmxbe.service.TaskService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(
    origins = [
        "http://localhost:63342", "http://127.0.0.1:63342",
        "http://localhost:5500", "http://127.0.0.1:5500"
    ]
)
class TaskApiController(
    private val service: TaskService
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getTasks(): List<TaskDto> =
        service.getTasks().map(TaskDto::fromEntity)

    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun createTask(@RequestBody request: CreateTaskRequest): TaskDto {
        val created = service.createTaskAndReturnSingle(request.title)
        return TaskDto.fromEntity(created)
    }

    @PutMapping("/{id}/toggle", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun toggleTask(@PathVariable id: Long): ResponseEntity<TaskDto> {
        val updated = service.toggleTask(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(TaskDto.fromEntity(updated))
    }

    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: Long): ResponseEntity<Void> {
        val removed = service.deleteTask(id)
        return if (removed) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }

    data class CreateTaskRequest(
        val title: String
    )
}