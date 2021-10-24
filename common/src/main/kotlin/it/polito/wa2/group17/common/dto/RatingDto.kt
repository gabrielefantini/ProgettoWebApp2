package it.polito.wa2.group17.common.dto

import java.util.*

data class RatingDto (
    val id: Long?,
    val stars: Int,
    val comment:String,
    val title: String,
    val creation_date: Date?,
)
