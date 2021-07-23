package it.polito.wa2.group17.warehouse.dto

import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

data class SellRequest(
    @field:NotNull val productID: Long,
    @field:NotNull @field:Min(0) val quantity: Long,
    var warehouseID: Long?
)
