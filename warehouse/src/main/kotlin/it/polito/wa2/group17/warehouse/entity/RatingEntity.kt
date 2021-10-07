package it.polito.wa2.group17.warehouse.entity

import it.polito.wa2.group17.common.utils.SafeLongIdEntity
import java.util.*
import javax.persistence.*

@Entity
class RatingEntity (
    id: Long? = null,

    var title: String,

    var stars:Int,

    var comment: String,

    var creationDate: Date,

    @ManyToOne
    @JoinColumn(name = "product")
    var product: ProductEntity
) : SafeLongIdEntity(id)
