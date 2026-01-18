package de.hka.htmxbe.repository

import de.hka.htmxbe.entity.Gender
import de.hka.htmxbe.entity.User
import org.springframework.stereotype.Repository
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong

@Repository
class UserRepository {

    private val seq = AtomicLong(1)
    private val data = CopyOnWriteArrayList<User>().apply {
        add(
            User(
                id = seq.getAndIncrement(),
                firstName = "Ada",
                lastName = "Lovelace",
                age = 36,
                gender = Gender.FEMALE
            )
        )
        add(
            User(
                id = seq.getAndIncrement(),
                firstName = "Alan",
                lastName = "Turing",
                age = 41,
                gender = Gender.MALE
            )
        )
        add(
            User(
                id = seq.getAndIncrement(),
                firstName = "Grace",
                lastName = "Hopper",
                age = 85,
                gender = Gender.FEMALE
            )
        )
    }

    fun findAll(): List<User> = data.toList()

    fun findById(id: Long): User? =
        data.firstOrNull { it.id == id }

    fun add(user: User): User {
        data.add(user)
        return user
    }

    fun update(user: User): User? {
        val index = data.indexOfFirst { it.id == user.id }
        if (index < 0) return null
        data[index] = user
        return user
    }

    fun delete(id: Long): Boolean =
        data.removeIf { it.id == id }

    fun nextId(): Long = seq.getAndIncrement()
}
