package it.polito.wa2.group17.common.dto

import java.time.Instant
import javax.validation.constraints.NotNull

data class TransactionRequest(
    val reason: String,
    val amount: Double,
    val userId: Long,
    val timeInstant: Instant = Instant.now(),
)