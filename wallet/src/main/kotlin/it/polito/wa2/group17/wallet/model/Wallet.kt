package it.polito.wa2.group17.wallet.model

import it.polito.wa2.group17.common.utils.converter.ConvertibleAlias
import it.polito.wa2.group17.common.utils.converter.ConvertibleCollection
import it.polito.wa2.group17.common.utils.converter.CustomConversion
import it.polito.wa2.group17.common.utils.converter.impl.IdAnnotatedExtractor
import it.polito.wa2.group17.wallet.entity.WalletEntity


data class Wallet(
    val id: Long,

    @param:CustomConversion(CustomConversion.Using(IdAnnotatedExtractor::class, WalletEntity::class))
    @param:ConvertibleAlias(ConvertibleAlias.From(WalletEntity::class, "customer"))
    val customerId: Long,

    @param:ConvertibleCollection(
        Long::class,
        CustomConversion(CustomConversion.Using(IdAnnotatedExtractor::class, WalletEntity::class)),
        HashSet::class
    )
    @param:ConvertibleAlias(ConvertibleAlias.From(WalletEntity::class, "incomingTransactions"))
    val incomingTransactionsIds: MutableSet<Long> = mutableSetOf(),

    @param:ConvertibleCollection(
        Long::class,
        CustomConversion(CustomConversion.Using(IdAnnotatedExtractor::class, WalletEntity::class)),
        HashSet::class
    )
    @param:ConvertibleAlias(ConvertibleAlias.From(WalletEntity::class, "outgoingTransactions"))
    val outgoingTransactionsIds: MutableSet<Long> = mutableSetOf(),

    val amount: Double = 0.0

)

