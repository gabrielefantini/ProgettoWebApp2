package it.polito.wa2.group17.order.model

import javax.validation.constraints.Min
import javax.validation.constraints.NotNull


data class StoredProductModel(
    @field: NotNull
    val productId: Long,
    @field: NotNull
    @field: Min(0)
    var quantity: Int,
)