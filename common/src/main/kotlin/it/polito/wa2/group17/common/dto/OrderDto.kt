package it.polito.wa2.group17.common.dto

import it.polito.wa2.group17.common.utils.converter.ConvertibleCollection
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

data class OrderDto(
    val id: Long,
    val buyerId: Long,
    @ConvertibleCollection(ProductOrderModel::class)
    val productOrders: List<ProductOrderModel>,
    val price: Double,
    val status: OrderStatus = OrderStatus.ISSUED,
)

enum class OrderStatus {
    ISSUED,DELIVERING,DELIVERED,FAILED,CANCELED
}

data class ProductOrderModel(
    @field:NotNull val productId: Long,
    @field:NotNull @field:Min(0) val quantity: Long,
    @field:NotNull @field:Min(0) val price: Double,
)
