package it.polito.wa2.group17.order.entities

import it.polito.wa2.group17.common.utils.BaseEntity
import org.jetbrains.annotations.NotNull
import javax.persistence.Entity
import javax.persistence.OneToMany
import javax.validation.constraints.Min

@Entity
class OrderEntity(
    @NotNull
    var buyer: String,
    @OneToMany(mappedBy = "orderId")
    var productOrders: MutableList<ProductOrderEntity>,
    @NotNull
    @Min(0)
    var price: Long,
) : BaseEntity<Long>()