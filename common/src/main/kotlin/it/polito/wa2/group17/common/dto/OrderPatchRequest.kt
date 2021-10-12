package it.polito.wa2.group17.common.dto

import org.jetbrains.annotations.NotNull

data class OrderPatchRequest(
    @field:NotNull val status: OrderStatus,
)
