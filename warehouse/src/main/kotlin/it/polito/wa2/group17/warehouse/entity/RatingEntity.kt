package it.polito.wa2.group17.warehouse.entity

import it.polito.wa2.group17.common.utils.SafeLongIdEntity
import javax.persistence.*

@Entity
class RatingEntity (
    id: Long? = null,

    var stars:Int,

    var comment: String,

    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    var product: ProductEntity
) : SafeLongIdEntity(id)
