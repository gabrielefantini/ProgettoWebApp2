package it.polito.wa2.group17.login.domain

import it.polito.wa2.group17.login.security.RoleName
import it.polito.wa2.group17.login.security.StringToRoleNamesConverter
import it.polito.wa2.group17.common.utils.BaseEntity
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import org.springframework.validation.annotation.Validated
import javax.persistence.*

@Entity
@Table(indexes = [Index(name = "index", columnList = "username", unique = true)])
class User(
    @NotNull
    @NotEmpty
    @Validated
    var username: String = "",

    var password: String = "",

    @Column(unique = true)
    @Validated
    var email: String = "",


    val name: String = "",
    val surname: String = "",
    val deliveryAddr: String = "",

    var isEnabled: Boolean = false,

    var roles: String = "",


) : BaseEntity<Long>() {

    fun getRoleNames(): Set<RoleName> = StringToRoleNamesConverter().invoke(roles)

    fun addRoleName(role: String) {
        if (!roles.contains(role)) roles += "$role "
    }

    fun removeRoleName(role: String) {
        roles = roles.replace("$role ", "")
    }
}
