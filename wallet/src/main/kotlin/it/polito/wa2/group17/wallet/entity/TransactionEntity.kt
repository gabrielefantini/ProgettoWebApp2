package it.polito.wa2.group17.wallet.entity

import it.polito.wa2.group17.common.utils.BaseEntity
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull

@Entity
class TransactionEntity(
    @NotNull
    var timeInstant: Instant = Instant.now(),

    @NotNull
    var amount: Double,

    @NotNull
    var reason: String,

    /**
     * Not lazy in order to let data be present for the converter which expects it in order to instantiate the data class dto.
     * Additionally, it's just a single field which does not contain much data. It can be afforded to load it every time
     * even if it is not used.
     */
    @ManyToOne
    @JoinColumn(name = "source", referencedColumnName = "id")
    var source: WalletEntity,

) : BaseEntity<Long>()
