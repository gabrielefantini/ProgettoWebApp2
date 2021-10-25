package it.polito.wa2.group17.common.dto

import org.jetbrains.annotations.NotNull
import javax.validation.Valid

data class NewOrderRequest(
    @field:NotNull val productOrders: List<@Valid ProductOrderModel>
)