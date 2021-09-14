package it.polito.wa2.group17.order.model

import javax.validation.constraints.Min

data class UpdateProductRequest(
    @field:Min(0) val quantity: Int?,
    @field:Min(0) val minimumQuantity: Int?=null,
)
