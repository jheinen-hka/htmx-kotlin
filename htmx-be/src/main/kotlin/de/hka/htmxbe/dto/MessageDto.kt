package de.hka.htmxbe.dto

import de.hka.htmxbe.entity.Message

data class MessageDto(
    val id: Long,
    val text: String
) {
    companion object {
        fun fromEntity(entity: Message): MessageDto =
            MessageDto(
                id = entity.id,
                text = entity.text
            )
    }
}