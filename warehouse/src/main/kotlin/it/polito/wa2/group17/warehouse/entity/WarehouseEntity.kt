package it.polito.wa2.group17.warehouse.entity

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.OneToMany

@Entity
class WarehouseEntity(
    @OneToMany(mappedBy = "warehouse", fetch = FetchType.EAGER)
    var products: MutableList<StoredProductEntity> = mutableListOf(),
    id: Long? = null
) : LongIdEntity(id)
