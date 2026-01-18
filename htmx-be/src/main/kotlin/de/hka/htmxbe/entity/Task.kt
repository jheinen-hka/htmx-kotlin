package de.hka.htmxbe.entity

data class Task(
    val id: Long,
    val title: String,
    val done: Boolean = false
)