package it.polito.wa2.group17.common.dto

import org.jetbrains.annotations.NotNull
import javax.validation.Valid

data class OrderRequest(
    @field:NotNull val userId: Long,
    @field:NotNull val deliveryAddr: String,
    @field:NotNull val email: String,
    @field:NotNull val productOrders: List<@Valid ProductOrderModel>
)