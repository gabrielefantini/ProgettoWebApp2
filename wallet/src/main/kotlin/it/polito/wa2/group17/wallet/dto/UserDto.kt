package it.polito.wa2.group17.wallet.dto

import it.polito.wa2.group17.wallet.enums.Role
import kotlin.properties.Delegates

class UserDto {
    var id by Delegates.notNull<Long>()
    lateinit var role: Role
}
