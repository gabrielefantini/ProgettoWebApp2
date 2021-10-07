package it.polito.wa2.group17.common.dto

import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

data class RatingRequest(
    @field:NotNull val title: String,
    @field:NotNull @field:Min(1) @field:Max(5) val stars: Int,
    @field:NotNull val comment: String
)
