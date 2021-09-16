package it.polito.wa2.group17.order.entities

import it.polito.wa2.group17.common.utils.BaseEntity
import org.jetbrains.annotations.NotNull
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.validation.constraints.Min

@Entity
class DeliveryEntity(
    @NotNull
    var deliveryAddr: String = "",

    @NotNull
    var warehouseId: Long? = null,

    @NotNull
    var productId: Long? = null,

    @NotNull
    @Min(0)
    var quantity: Long = 0,

    @ManyToOne
    @JoinColumn(name = "order_entity", referencedColumnName = "id")
    var order: OrderEntity? = null

): BaseEntity<Long>()