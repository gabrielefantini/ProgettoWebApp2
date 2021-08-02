package it.polito.wa2.group17.warehouse.entity

import it.polito.wa2.group17.common.utils.SafeLongIdEntity
import javax.persistence.Entity

@Entity
class ProductEntity(
    id: Long? = null
) : SafeLongIdEntity(id)


