package it.polito.wa2.group17.wallet.exception

import it.polito.wa2.group17.common.exception.GenericBadRequestException

class InvalidTransactionException(
    sourceWalletId: Long,
    amount: Double,
    desc: String = ""
) : GenericBadRequestException("Invalid transaction of $amount from $sourceWalletId. $desc")
