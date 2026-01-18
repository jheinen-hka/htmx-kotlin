package de.hka.htmxbe.dto

import de.hka.htmxbe.entity.Gender
import de.hka.htmxbe.entity.User

data class UserDto(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val age: Int?,
    val gender: Gender
) {
    companion object {
        fun fromEntity(entity: User): UserDto =
            UserDto(
                id = entity.id,
                firstName = entity.firstName,
                lastName = entity.lastName,
                age = entity.age,
                gender = entity.gender
            )
    }
}