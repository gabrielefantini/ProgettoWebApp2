package it.polito.wa2.group17.common.dto


data class Wallet(
    val id: Long,

    val userId: Long,

    val transactions: MutableSet<Long> = mutableSetOf(),

    val amount: Double = 0.0

)
