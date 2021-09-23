package it.polito.wa2.group17.login.dto

import it.polito.wa2.group17.login.security.RoleName
import it.polito.wa2.group17.login.security.RoleNameGrantedAuthority
import it.polito.wa2.group17.login.security.StringToRoleNamesConverter
import it.polito.wa2.group17.login.domain.User
import it.polito.wa2.group17.common.utils.converter.CustomConversion
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class UserDetailsDto(
    val id: Long? = null,
    private val username: String,
    private val password: String,
    val email: String,
    private val isEnabled: Boolean,
    @param:CustomConversion(CustomConversion.Using(StringToRoleNamesConverter::class, User::class))
    val roles: Set<RoleName>,
    val name: String,
    val surname: String,
    val deliveryAddr: String,
) : UserDetails, ConvertibleDto<User> {

    override fun getPassword(): String = password

    override fun getUsername(): String = username

    override fun isEnabled(): Boolean = isEnabled


    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        roles.map { RoleNameGrantedAuthority(it) }.toMutableList()

    override fun isAccountNonExpired(): Boolean = isEnabled


    override fun isAccountNonLocked(): Boolean = isEnabled

    override fun isCredentialsNonExpired(): Boolean = isEnabled
}
