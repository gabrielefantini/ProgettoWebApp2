package it.polito.wa2.group17.order.model

import java.time.Instant
import javax.validation.constraints.NotNull

data class TransactionModel(
    var id: Long? = null,
    @field:NotNull val reason: String,
    @field:NotNull val amount: Double,
    @field:NotNull val userId: Long,
    val timeInstant: Instant
)