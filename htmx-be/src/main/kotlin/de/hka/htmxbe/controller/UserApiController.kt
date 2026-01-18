package de.hka.htmxbe.controller

import de.hka.htmxbe.dto.UserDto
import de.hka.htmxbe.entity.Gender
import de.hka.htmxbe.service.UserService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
@CrossOrigin(
    origins = [
        "http://localhost:63342", "http://127.0.0.1:63342",
        "http://localhost:5500", "http://127.0.0.1:5500"
    ]
)
class UserApiController(
    private val service: UserService
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUsers(): List<UserDto> =
        service.getUsers().map(UserDto::fromEntity)

    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<UserDto> {
        val gender = parseGender(request.gender)
        val command = UserService.CreateUserCommand(
            firstName = request.firstName,
            lastName = request.lastName,
            age = request.age,
            gender = gender
        )

        val created = service.createUserAndReturnSingle(command)
        return ResponseEntity.ok(UserDto.fromEntity(created))
    }

    private fun parseGender(value: String?): Gender =
        when (value?.uppercase()) {
            "FEMALE" -> Gender.FEMALE
            "MALE" -> Gender.MALE
            "OTHER" -> Gender.OTHER
            else -> Gender.UNKNOWN
        }

    data class CreateUserRequest(
        val firstName: String,
        val lastName: String,
        val age: Int?,
        val gender: String?
    )
}