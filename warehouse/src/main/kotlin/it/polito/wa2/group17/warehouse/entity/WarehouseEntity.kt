package it.polito.wa2.group17.warehouse.entity

import it.polito.wa2.group17.common.utils.BaseEntity
import javax.persistence.Entity
import javax.persistence.OneToMany

@Entity
class WarehouseEntity(
    @OneToMany(mappedBy = "warehouse")
    var products: List<StoredProductEntity>
) : BaseEntity<Long>()
