package it.polito.wa2.group17.warehouse.entity

import it.polito.wa2.group17.common.utils.BaseEntity
import javax.persistence.*
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

@Entity
class StoredProductEntity(
    @NotNull
    @OneToOne(fetch = FetchType.EAGER)
    var product: ProductEntity,

    @NotNull
    @Min(0)
    var quantity: Int,

    @NotNull
    @Min(0)
    var minimumQuantity: Int,

    @ManyToOne
    @JoinColumn(name = "warehouse", referencedColumnName = "id")
    val warehouse: WarehouseEntity

) : BaseEntity<Long>()
