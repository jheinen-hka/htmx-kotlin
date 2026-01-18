package de.hka.htmxbe.entity

data class User(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val age: Int?,
    val gender: Gender
)