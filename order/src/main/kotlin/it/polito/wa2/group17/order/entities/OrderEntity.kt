package it.polito.wa2.group17.order.entities

import it.polito.wa2.group17.common.utils.BaseEntity
import org.jetbrains.annotations.NotNull
import javax.persistence.Entity
import javax.persistence.OneToMany
import javax.validation.constraints.Min

@Entity
class OrderEntity(
    @NotNull
    var buyerId: Long,

    @OneToMany(mappedBy = "order")
    var productOrders: MutableList<ProductOrderEntity>,

    @OneToMany(mappedBy = "order")
    var deliveryList: MutableList<DeliveryEntity>,

    @NotNull
    @Min(0)
    var price: Double,

    @NotNull
    var status: OrderStatus,
) : BaseEntity<Long>()

enum class OrderStatus {
    ISSUED,DELIVERING,DELIVERED,FAILED,CANCELED
}