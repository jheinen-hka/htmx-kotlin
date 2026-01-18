package de.hka.htmxbe.service

import de.hka.htmxbe.entity.Gender
import de.hka.htmxbe.entity.User
import de.hka.htmxbe.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val repo: UserRepository
) {

    fun getUsers(): List<User> = repo.findAll()

    fun createUser(command: CreateUserCommand): List<User> {
        val user = createValidatedUser(command)
        repo.add(user)
        return repo.findAll()
    }

    fun createUserAndReturnSingle(command: CreateUserCommand): User {
        val user = createValidatedUser(command)
        return repo.add(user)
    }

    fun deleteUser(id: Long): Boolean =
        repo.delete(id)

    fun updateUser(id: Long, command: UpdateUserCommand): User? {
        require(command.firstName.isNotBlank()) { "firstName must not be blank" }
        require(command.lastName.isNotBlank()) { "lastName must not be blank" }

        val age = command.age
        if (age != null) {
            require(age in 0..130) { "age must be between 0 and 130" }
        }

        val existing = repo.findById(id) ?: return null

        val updated = existing.copy(
            firstName = command.firstName.trim(),
            lastName = command.lastName.trim(),
            age = age,
            gender = command.gender
        )

        return repo.update(updated)
    }

    fun searchUsers(query: String?, gender: Gender?): List<User> {
        val normalizedQuery = query
            ?.trim()
            ?.lowercase()
            ?.takeIf { it.isNotEmpty() }

        val all = repo.findAll()

        return all.filter { user ->
            val matchesQuery = normalizedQuery?.let { q ->
                user.firstName.lowercase().contains(q) ||
                        user.lastName.lowercase().contains(q)
            } ?: true

            val matchesGender = gender?.let { g ->
                user.gender == g
            } ?: true

            matchesQuery && matchesGender
        }
    }

    private fun createValidatedUser(command: CreateUserCommand): User {
        require(command.firstName.isNotBlank()) { "firstName must not be blank" }
        require(command.lastName.isNotBlank()) { "lastName must not be blank" }

        val age = command.age
        if (age != null) {
            require(age in 0..130) { "age must be between 0 and 130" }
        }

        return User(
            id = repo.nextId(),
            firstName = command.firstName.trim(),
            lastName = command.lastName.trim(),
            age = age,
            gender = command.gender
        )
    }

    data class CreateUserCommand(
        val firstName: String,
        val lastName: String,
        val age: Int?,
        val gender: Gender
    )

    data class UpdateUserCommand(
        val firstName: String,
        val lastName: String,
        val age: Int?,
        val gender: Gender
    )
}