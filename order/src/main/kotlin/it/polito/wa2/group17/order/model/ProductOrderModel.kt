package it.polito.wa2.group17.order.model

import javax.validation.constraints.NotNull
import javax.validation.constraints.Min

data class ProductOrderModel(
    @field:NotNull val productId: Long,
    @field:NotNull @field:Min(0) val quantity: Long,
    @field:NotNull @field:Min(0) val price: Long,
)