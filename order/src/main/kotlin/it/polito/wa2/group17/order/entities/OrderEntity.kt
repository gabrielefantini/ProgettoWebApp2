package it.polito.wa2.group17.order.entities

import it.polito.wa2.group17.common.utils.BaseEntity
import org.jetbrains.annotations.NotNull
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.OneToMany
import javax.validation.constraints.Min

@Entity
class OrderEntity(
    @NotNull
    var buyerId: Long? = null,

    @NotNull
    @Min(0)
    var price: Double = 0.0,

    @NotNull
    var status: OrderStatus? = null,

    @OneToMany(
        mappedBy = "order",
        cascade = [CascadeType.REMOVE]
    )
    val productOrders: MutableList<ProductOrderEntity> = mutableListOf<ProductOrderEntity>(),

    @OneToMany(
        mappedBy = "order",
        cascade = [CascadeType.REMOVE]
    )
    val deliveryList: MutableList<DeliveryEntity> =  mutableListOf<DeliveryEntity>(),


) : BaseEntity<Long>()

enum class OrderStatus {
    ISSUED,DELIVERING,DELIVERED,FAILED,CANCELED
}