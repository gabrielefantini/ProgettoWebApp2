package it.polito.wa2.group17.order.dto

import it.polito.wa2.group17.common.utils.converter.ConvertibleCollection
import it.polito.wa2.group17.order.entities.OrderStatus
import it.polito.wa2.group17.order.model.ProductOrderModel

data class OrderDto(
    val id: Long,
    val buyerId: Long,
    @ConvertibleCollection(ProductOrderModel::class)
    val productOrders: List<ProductOrderModel>,
    val price: Double,
    val status: OrderStatus = OrderStatus.ISSUED,
)