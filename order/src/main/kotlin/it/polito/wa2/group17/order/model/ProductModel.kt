package it.polito.wa2.group17.order.model

import java.util.*
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

data class ProductModel (
    @field: NotNull
    val name: String,
    @field: NotNull
    @field: Min(0)
    val price: Double,
)