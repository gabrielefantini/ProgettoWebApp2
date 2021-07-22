package it.polito.wa2.group17.wallet.model

import it.polito.wa2.group17.common.utils.converter.ConvertibleCollection
import it.polito.wa2.group17.common.utils.converter.CustomConversion
import it.polito.wa2.group17.common.utils.converter.impl.IdAnnotatedExtractor
import it.polito.wa2.group17.wallet.entity.WalletEntity


data class Wallet(
    val id: Long,

    val userId: Long,


    @param:ConvertibleCollection(
        Long::class,
        CustomConversion(CustomConversion.Using(IdAnnotatedExtractor::class, WalletEntity::class)),
        HashSet::class
    )
    val transactions: MutableSet<Long> = mutableSetOf(),

    val amount: Double = 0.0

)

