package it.polito.wa2.group17.common.transaction

import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.TransactionSystemException
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.PostConstruct
import kotlin.concurrent.withLock

@Component
@Slf4j
class MultiserviceTransactionSynchronizer {

    @Autowired
    private lateinit var transactionInvoker: TransactionInvoker

    private val transactionMap = ConcurrentHashMap<String, MultiserviceTransactionData>()

    @PostConstruct
    private fun initializeMonitor() {
        //TODO init monitor thread
    }

    fun <T> invokeWithinMultiserviceTransaction(
        rollback: Method,
        args: Array<Any?>,
        instance: Any,
        function: () -> T
    ): T {
        val currentTransactionID: String = "a" //TODO()
        val transactionData =
            MultiserviceTransactionData(currentTransactionID, rollback, args, instance, ReentrantLock(), function)

        transactionMap[currentTransactionID] = transactionData
        return transactionData.lock.withLock { startMultiserviceTransaction(transactionData) }
    }


    private fun <T> startMultiserviceTransaction(transactionData: MultiserviceTransactionData): T {
        try {
            return transactionInvoker.invokeWithinTransaction(transactionData.function) as T
        } catch (t: Throwable) {
            //TODO NOTIFY OTHERS
            throw t
        }
    }

    private fun rollbackTransaction(transactionID: String) {
        val transactionData = transactionMap[transactionID] ?: return
        try {
            transactionData.rollback.invoke(transactionData.instance, *transactionData.args)
        } catch (t2: Throwable) {
            throw TransactionSystemException("Application exception overridden by transaction exception", t2)
        }
    }
}
