package it.polito.wa2.group17.catalog.security

import org.springframework.security.core.GrantedAuthority

class RoleNameGrantedAuthority(private val roleName: RoleName) : GrantedAuthority {
    override fun getAuthority(): String = roleName.name
}
