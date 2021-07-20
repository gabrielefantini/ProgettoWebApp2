package it.polito.wa2.group17.wallet.entity

import it.polito.wa2.group17.common.utils.BaseEntity
import it.polito.wa2.group17.wallet.enums.RoleName
import javax.persistence.Entity
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull


@Entity
class UserEntity(
    @NotNull
    @NotEmpty
    var roles: Set<RoleName>
) : BaseEntity<Long>()
