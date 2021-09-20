package it.polito.wa2.group17.common.dto

import javax.validation.constraints.NotNull

data class PostPicture (
    @field:NotNull val picture: String
)
