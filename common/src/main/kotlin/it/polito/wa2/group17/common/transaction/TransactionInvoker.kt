package it.polito.wa2.group17.common.transaction

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.Callable

@Component
class TransactionInvoker {

    @Transactional
    fun invokeWithinTransaction(runnable: Runnable) = runnable.run()

    @Transactional
    fun <T> invokeWithinTransaction(callable: Callable<T>): T = callable.call()

}
