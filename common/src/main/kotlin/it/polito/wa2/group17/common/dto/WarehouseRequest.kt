package it.polito.wa2.group17.common.dto

import javax.validation.Valid
import javax.validation.constraints.NotNull

data class WarehouseRequest(
    @field:NotNull val products: List<@Valid StoredProductDto>
)
