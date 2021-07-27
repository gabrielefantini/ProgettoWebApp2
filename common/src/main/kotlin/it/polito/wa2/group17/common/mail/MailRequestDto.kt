package it.polito.wa2.group17.common.mail

data class MailRequestDto(
    val destination: String,
    val subject: String,
    val body: String
)
