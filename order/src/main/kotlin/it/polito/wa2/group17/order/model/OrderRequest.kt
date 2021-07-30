package it.polito.wa2.group17.order.model

import org.jetbrains.annotations.NotNull
import javax.validation.Valid

data class OrderRequest(
    @field:NotNull val userId: Long,
    @field:NotNull val productOrders: List<@Valid ProductOrderModel>
)