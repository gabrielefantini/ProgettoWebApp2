package it.polito.wa2.group17.common.transaction

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class TransactionInvoker {

    @Transactional
    fun <T> invokeWithinTransaction(function: () -> T): T = function()

}
