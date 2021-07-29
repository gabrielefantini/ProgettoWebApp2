package it.polito.wa2.group17.wallet.entity


import it.polito.wa2.group17.common.utils.BaseEntity
import it.polito.wa2.group17.common.utils.converter.ConvertibleCollection
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.OneToMany
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

@Entity
class WalletEntity(
    @Min(value = 0)
    var amount: Double = 0.0,

    @NotNull
    var userId: Long,

    @OneToMany(mappedBy = "source",cascade = [CascadeType.ALL])
    @field:ConvertibleCollection(TransactionEntity::class)
    var transactions: MutableList<TransactionEntity> = mutableListOf()

) : BaseEntity<Long>()
