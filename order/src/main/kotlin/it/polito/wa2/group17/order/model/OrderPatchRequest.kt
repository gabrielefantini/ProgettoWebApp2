package it.polito.wa2.group17.order.model

import it.polito.wa2.group17.order.entities.OrderStatus
import org.jetbrains.annotations.NotNull

data class OrderPatchRequest(
    @field:NotNull val status: OrderStatus,
)