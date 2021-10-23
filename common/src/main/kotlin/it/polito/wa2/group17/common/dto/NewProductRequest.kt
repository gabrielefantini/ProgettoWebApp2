package it.polito.wa2.group17.common.dto

import java.util.*
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

data class NewProductRequest (
    @field:NotNull val name: String?,
    val description: String?,
    val pictureURL: String?,
    @field:NotNull val category: String?,
    @field:NotNull @field:Min(0) val price: Double?,
    @field:Min(0) val avgRating: Double?,
    val creationDate: Date?,
)