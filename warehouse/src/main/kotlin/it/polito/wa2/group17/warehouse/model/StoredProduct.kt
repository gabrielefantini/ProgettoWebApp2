package it.polito.wa2.group17.warehouse.model

import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

data class StoredProduct(
    @field:NotNull
    val productId: Long,

    @field:NotNull
    @field:Min(0)
    val quantity: Int,

    @field:NotNull
    @field:Min(0)
    val minimumQuantity: Int
)
