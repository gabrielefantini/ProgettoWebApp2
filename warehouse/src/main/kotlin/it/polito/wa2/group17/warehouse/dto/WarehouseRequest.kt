package it.polito.wa2.group17.warehouse.dto

import it.polito.wa2.group17.warehouse.model.StoredProduct
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class WarehouseRequest(
    @field:NotNull val products: List<@Valid StoredProduct>
)
