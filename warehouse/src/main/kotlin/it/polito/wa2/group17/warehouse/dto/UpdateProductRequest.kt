package it.polito.wa2.group17.warehouse.dto

import javax.validation.constraints.Min

data class UpdateProductRequest(
    @field:Min(0) val quantity: Int?,
    @field:Min(0) val minimumQuantity: Int?,
)
