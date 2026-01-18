package de.hka.htmxbe.repository

import de.hka.htmxbe.entity.Gender
import de.hka.htmxbe.entity.User
import org.springframework.stereotype.Repository
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong

@Repository
class UserRepository {

    private val seq = AtomicLong(1)
    private val data = CopyOnWriteArrayList<User>()

    init {
        seedDemoData()
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

    /**
     * Seed in-memory repository with demo data for the HTMX + Kotlin demo.
     * This keeps the repository usable without a database and illustrates the live search.
     */
    private fun seedDemoData() {
        data.add(
            User(
                id = seq.getAndIncrement(),
                firstName = "Ada",
                lastName = "Lovelace",
                age = 36,
                gender = Gender.FEMALE
            )
        )
        data.add(
            User(
                id = seq.getAndIncrement(),
                firstName = "Alan",
                lastName = "Turing",
                age = 41,
                gender = Gender.MALE
            )
        )
        data.add(
            User(
                id = seq.getAndIncrement(),
                firstName = "Grace",
                lastName = "Hopper",
                age = 85,
                gender = Gender.FEMALE
            )
        )
        data.add(
            User(
                id = seq.getAndIncrement(),
                firstName = "Alex",
                lastName = "Miller",
                age = 29,
                gender = Gender.OTHER
            )
        )
        data.add(
            User(
                id = seq.getAndIncrement(),
                firstName = "Alex",
                lastName = "Johnson",
                age = 34,
                gender = Gender.MALE
            )
        )
        data.add(
            User(
                id = seq.getAndIncrement(),
                firstName = "Chris",
                lastName = "Smith",
                age = 27,
                gender = Gender.MALE
            )
        )
        data.add(
            User(
                id = seq.getAndIncrement(),
                firstName = "Chris",
                lastName = "Anderson",
                age = 31,
                gender = Gender.FEMALE
            )
        )
        data.add(
            User(
                id = seq.getAndIncrement(),
                firstName = "Taylor",
                lastName = "Brown",
                age = 22,
                gender = Gender.UNKNOWN
            )
        )
        data.add(
            User(
                id = seq.getAndIncrement(),
                firstName = "Jordan",
                lastName = "Lee",
                age = 45,
                gender = Gender.OTHER
            )
        )
        data.add(
            User(
                id = seq.getAndIncrement(),
                firstName = "Sam",
                lastName = "Williams",
                age = 19,
                gender = Gender.MALE
            )
        )
        data.add(
            User(
                id = seq.getAndIncrement(),
                firstName = "Sam",
                lastName = "Taylor",
                age = 32,
                gender = Gender.FEMALE
            )
        )
    }
}
