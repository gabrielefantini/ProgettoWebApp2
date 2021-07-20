package it.polito.wa2.group17.common.transaction

import it.polito.wa2.group17.common.utils.Cache
import it.polito.wa2.group17.common.utils.Loggable
import it.polito.wa2.group17.common.utils.Loggable.Companion.logger
import it.polito.wa2.group17.common.utils.putIfAbsentAndThen
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.TransactionSystemException
import java.lang.reflect.Method
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.PostConstruct
import kotlin.concurrent.withLock

@Component
class MultiserviceTransactionSynchronizer : Loggable {
    @Autowired
    private lateinit var transactionInvoker: TransactionInvoker

    @Autowired
    private lateinit var transactionChannel: MultiserviceTransactionChannel

    @Value("\${transaction.rollbackTimeout:10000}")
    private var rollbackTimeout: Long = 10000

    private lateinit var transactionCache: Cache<String, MultiserviceTransactionData<*>>


    private lateinit var uncompletedTransactionsCache:
            Cache<String, MutableSet<String>> // chiave id transazione, valore set di id servizi che l'hanno startata


    @PostConstruct
    private fun init() {
        transactionChannel.subscribe(this::handleTransactionMessage)

        transactionCache = Cache(rollbackTimeout, TimeUnit.MILLISECONDS, "Transaction Cache")
        transactionCache.subscribe {
            if (uncompletedTransactionsCache[it.first]?.isNotEmpty() == true) {
                logger.warn(
                    "Multiservice transaction ${it.first} has reached timeout without being completed by all services!"
                )
                handleExpiredTransaction(it.second)
            }
        }

        uncompletedTransactionsCache =
            Cache(3 * rollbackTimeout, TimeUnit.MILLISECONDS, "Uncompleted transaction Cache")
        //quelle che riguardano il servizio vengono rimosse in altri punti
        //la pulizia automatica di questa cache rimuove solo le transazioni non completate che riguardano altri servizi ma non questo
        uncompletedTransactionsCache.addVoter { !transactionCache.containsKey(it.first) }
    }

    @Synchronized
    private fun handleTransactionMessage(multiserviceTransactionMessage: MultiserviceTransactionMessage) {
        when (multiserviceTransactionMessage.status) {
            MultiserviceTransactionStatus.STARTED -> {
                synchronized(this) {
                    uncompletedTransactionsCache.putIfAbsentAndThen(
                        multiserviceTransactionMessage.transactionID,
                        HashSet()
                    ) {
                        add(multiserviceTransactionMessage.serviceID)
                    }

                }
            }
            MultiserviceTransactionStatus.COMPLETED -> {
                synchronized(this) {
                    uncompletedTransactionsCache[multiserviceTransactionMessage.transactionID]?.remove(
                        multiserviceTransactionMessage.serviceID
                    )
                }
            }
            MultiserviceTransactionStatus.FAILED -> {
                synchronized(this) {
                    uncompletedTransactionsCache.remove(multiserviceTransactionMessage.transactionID)
                }
                rollbackTransaction(multiserviceTransactionMessage.transactionID)
            }
        }
    }


    private fun handleExpiredTransaction(multiserviceTransactionData: MultiserviceTransactionData<*>) {
        synchronized(this) {
            uncompletedTransactionsCache.remove(multiserviceTransactionData.id)
        }
        rollbackTransaction(multiserviceTransactionData.id, false)
    }


    fun <T> invokeWithinMultiserviceTransaction(
        rollback: Method,
        args: Array<Any?>,
        instance: Any,
        invokingMethod: Method,
        shouldPropagateResult: Boolean,
        joinPoint: () -> T
    ): T? {
        val currentTransactionID = MultiserviceTransactionContextHolder.getCurrentTransactionID()
        logger.info("Performing multiservice transaction step of transaction $currentTransactionID")
        val transactionData =
            MultiserviceTransactionData(
                currentTransactionID,
                rollback,
                args,
                instance,
                ReentrantLock(),
                invokingMethod,
                joinPoint,
                instance.javaClass,
                shouldPropagateResult
            )

        transactionCache[currentTransactionID] = transactionData
        return transactionData.lock.withLock { startMultiserviceTransaction(transactionData) }
    }


    private fun <T> startMultiserviceTransaction(transactionData: MultiserviceTransactionData<T>): T? {
        val transactionID = transactionData.id

        logger.debug("Starting multiservice transaction $transactionID")
        transactionChannel.notifyTransactionStart(transactionID)
        return try {
            val result = transactionInvoker.invokeWithinTransaction(transactionData.joinPoint)
            transactionData.transactionResult = result
            result
        } catch (t: Throwable) {
            logger.error(
                "Error during multiservice transaction $transactionID. Notifying other services after having performed rollback.",
                t
            )
            transactionChannel.notifyTransactionFailure(transactionID)
            throw t
        }.also {
            logger.debug("Multiservice transaction $transactionID completed")
            transactionChannel.notifyTransactionSuccess(transactionID)
        }
    }

    private fun rollbackTransaction(transactionID: String, cleanTransactionCache: Boolean = true) {
        logger.warn("Performing manual rollback of multiservice transaction $transactionID")

        val transactionData = transactionCache[transactionID] ?: return
        transactionChannel.notifyTransactionFailure(transactionID)

        try {
            transactionData.lock.withLock {
                doRollbackTransaction(transactionData)
            }
        } finally {
            if (cleanTransactionCache)
                transactionCache.remove(transactionID)
        }

    }

    private fun doRollbackTransaction(transactionData: MultiserviceTransactionData<*>) {
        val transactionID = transactionData.id
        try {
            val args = if (transactionData.shouldPropagateResult)
                arrayOf(*transactionData.args, transactionData.transactionResult) else transactionData.args

            transactionInvoker.invokeWithinTransaction {
                transactionData.rollback.invoke(transactionData.instance, *args)
            }
            logger.debug("Manual rollback of multiservice transaction $transactionID performed successfully")
        } catch (t2: Throwable) {
            logger.error("Error performing manual rollback of multiservice transaction $transactionID", t2)
            throw TransactionSystemException(
                "Couldn't rollback multiservice transaction $transactionID",
                t2
            )
        }
    }


}
