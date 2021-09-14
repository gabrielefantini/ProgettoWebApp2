package it.polito.wa2.group17.order.dto

import it.polito.wa2.group17.order.entities.OrderStatus

data class OrderUpdate(
    val newOrder: OrderDto,
    val oldStatus: OrderStatus
)