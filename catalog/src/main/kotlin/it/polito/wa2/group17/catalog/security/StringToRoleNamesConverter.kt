package it.polito.wa2.group17.catalog.security

import it.polito.wa2.group17.common.utils.converter.CustomConverter

class StringToRoleNamesConverter : CustomConverter<String, Set<RoleName>> {
    override fun invoke(source: String): Set<RoleName> = source.split(" ").filter { it.isNotBlank() }
        .map { RoleName.valueOf(it) }.toSet()
}

enum class RoleName {
    CUSTOMER,
    ADMIN
}
