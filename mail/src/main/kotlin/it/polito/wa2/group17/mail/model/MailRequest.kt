package it.polito.wa2.group17.mail.model

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class MailRequest(
    @field:NotNull @field:NotEmpty val destination: String,
    @field:NotNull val subject: String,
    @field:NotNull val body: String
)
