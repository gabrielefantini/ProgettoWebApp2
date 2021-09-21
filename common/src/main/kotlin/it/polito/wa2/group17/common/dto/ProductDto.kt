package it.polito.wa2.group17.common.dto

import java.util.*

data class ProductDto (
    var id: Long?,
    var name: String?,
    var description: String?,
    var pictureURL: String?,
    var category: String?,
    var price: Double?,
    var avgRating: Double?,
    var creationDate: Date?,
)
