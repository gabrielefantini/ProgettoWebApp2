package it.polito.wa2.group17.wallet.model

import java.time.Instant
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

data class TransactionRequest(
    @field:NotNull val reason: String,
    @field:NotNull @field:Min(0) val amount: Double,
    @field:NotNull val userId: Long,
    val timeInstant: Instant = Instant.now(),
)
