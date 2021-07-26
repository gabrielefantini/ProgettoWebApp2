package it.polito.wa2.group17.catalog.model

import it.polito.wa2.group17.catalog.constraints.FieldsMatch
import javax.validation.constraints.Email

@FieldsMatch("password", "passwordConfirm", "Passwords don't match")
data class UserRegistration(
    val username: String,
    @field:Email(message = "Email is not valid")
    val email: String,
    val name: String,
    val surname: String,
    val address: String,
    val password: String,
    val passwordConfirm: String
)
