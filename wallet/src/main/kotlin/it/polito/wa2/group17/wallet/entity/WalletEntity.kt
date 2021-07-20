package it.polito.wa2.group17.wallet.entity


import it.polito.wa2.group17.common.utils.BaseEntity
import it.polito.wa2.group17.common.utils.converter.ConvertibleCollection
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.validation.constraints.Min

@Entity
class WalletEntity(
    @Min(value = 0)
    var amount: Double = 0.0,

    /**
     * Not lazy in order to let data be present for the converter which expects it in order to instantiate the data class dto.
     * Additionally, it's just a single field which does not contain much data. It can be afforded to load it every time
     * even if it is not used.
     */
    @ManyToOne
    @JoinColumn(name = "userId")
    var user: UserEntity,

    @OneToMany(mappedBy = "source")
    @field:ConvertibleCollection(TransactionEntity::class)
    var transactions: MutableList<TransactionEntity> = mutableListOf()

) : BaseEntity<Long>()
