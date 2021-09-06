package it.polito.wa2.group17.warehouse.dto

import java.util.*

data class ProductDto (
    val id: Long,
    val name: String,
    val description: String,
    val pictureURL: String,
    val category: String,
    val price: Double,
    val avgRating: Double,
    val creationDate: Date,
)