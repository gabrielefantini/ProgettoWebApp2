package it.polito.wa2.group17.order.model

import javax.validation.Valid
import javax.validation.constraints.NotNull

data class WarehouseModel(
    @field: NotNull
    val productList: List<@Valid StoredProductModel>
)

