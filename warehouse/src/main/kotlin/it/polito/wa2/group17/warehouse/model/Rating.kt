package it.polito.wa2.group17.warehouse.model

import java.util.*

data class Rating (
    val id: Long,
    val stars: Int,
    val comment: String,
    val title: String,
    val creation_date: Date?
)
