package it.polito.wa2.group17.warehouse.dto

import kotlin.properties.Delegates

class UserDto {
    var id by Delegates.notNull<Long>()
    lateinit var email: String
}
