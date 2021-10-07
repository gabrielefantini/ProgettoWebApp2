package it.polito.wa2.group17.warehouse.dto

import it.polito.wa2.group17.common.utils.converter.ConvertibleCollection
import it.polito.wa2.group17.warehouse.model.Rating
import java.util.*

data class ProductDto (
    var id: Long?,
    var name: String?,
    var description: String?,
    var pictureURL: String?,
    var category: String?,
    var price: Double?,
    @ConvertibleCollection(Rating::class)
    val ratings: List<Rating>?,
    var avgRating: Double?,
    var creationDate: Date?,
)
