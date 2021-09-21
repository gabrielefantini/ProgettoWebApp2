package it.polito.wa2.group17.common.dto

import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

data class AddProductRequest(
    @field:NotNull @field:Min(0) val quantity: Int,
    @field:NotNull @field:Min(0) val minimumQuantity: Int,
    @field:NotNull val productId: Long
)
