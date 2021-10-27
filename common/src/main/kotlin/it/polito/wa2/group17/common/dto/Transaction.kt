package it.polito.wa2.group17.common.dto

import java.time.Instant

data class Transaction(
    val id: Long,
    val timeInstant: Instant,
    val amount: Double = 0.0,
    val reason: String,
)