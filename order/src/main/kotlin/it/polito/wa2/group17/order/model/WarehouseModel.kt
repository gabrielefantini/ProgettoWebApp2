package it.polito.wa2.group17.order.model

import javax.validation.Valid
import javax.validation.constraints.NotNull

data class WarehouseModel(
    val id: Long,
    @field: NotNull
    val products: List<@Valid StoredProductModel>
)

