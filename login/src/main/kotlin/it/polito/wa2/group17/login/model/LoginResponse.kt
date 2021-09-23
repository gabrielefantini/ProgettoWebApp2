package it.polito.wa2.group17.login.model

data class LoginResponse(
    val token: String,
    val id: Long,
    val username: String,
    val email: String,
    val roles: List<String>
)

data class BadLoginResponse(
    val error: String
)
