package it.polito.wa2.group17.warehouse.dto

import java.util.*
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

data class PutProductRequest (
    var id: Long,
    @field:NotNull val name: String,
    val description: String,
    val pictureURL: String,
    @field:NotNull val category: String,
    @field:NotNull @field:Min(0) val price: Double,
    @field:Min(0) val avgRating: Double,
    val creationDate: Date,
)