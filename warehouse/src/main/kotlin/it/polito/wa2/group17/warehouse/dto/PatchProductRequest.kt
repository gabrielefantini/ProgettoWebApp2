package it.polito.wa2.group17.warehouse.dto

import java.util.*

data class PatchProductRequest (
    var name: String?=null,
    var description: String?=null,
    var pictureURL: String?=null,
    var category: String?=null,
    var price: Double?=null,
    var avgRating: Double?=null,
    var creationDate: Date?=null,
)