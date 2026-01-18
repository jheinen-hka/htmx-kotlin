package de.hka.htmxbe.dto

import de.hka.htmxbe.entity.Task

data class TaskDto(
    val id: Long,
    val title: String,
    val done: Boolean
) {
    companion object {
        fun fromEntity(entity: Task): TaskDto =
            TaskDto(
                id = entity.id,
                title = entity.title,
                done = entity.done
            )
    }
}