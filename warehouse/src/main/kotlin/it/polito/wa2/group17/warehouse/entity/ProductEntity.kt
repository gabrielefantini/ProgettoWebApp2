package it.polito.wa2.group17.warehouse.entity

import it.polito.wa2.group17.common.utils.BaseEntity
import it.polito.wa2.group17.common.utils.SafeLongIdEntity
import java.util.*
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.OneToMany
import javax.persistence.OneToOne

@Entity
class ProductEntity(
    id: Long? = null,
    var name: String? = null,
    var description: String? = null,
    var pictureURL: String? = null,
    var category: String? = null,
    var price: Double? = 0.0,
    @OneToMany(mappedBy = "product")
    var ratings: MutableList<RatingEntity>,
    var avgRating: Double? = null,
    var creationDate: Date? = null,
    @OneToOne
    var storedProductEntity: StoredProductEntity,
) : SafeLongIdEntity(id)


