package it.polito.wa2.group17.common.transaction

import it.polito.wa2.group17.common.utils.Cache
import it.polito.wa2.group17.common.utils.putIfAbsentAndThen
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
class MultiserviceTransactionSynchronizer {

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(MultiserviceTransactionSynchronizer::class.java)
    }

    @Autowired
    private lateinit var transactionInvoker: TransactionInvoker

    @Autowired
    private lateinit var transactionChannel: MultiserviceTransactionChannel

    @Value("\${transaction.rollbackTimeout:10000}")
    private var rollbackTimeout: Long = 10000

    private lateinit var transactionCache: Cache<String, MultiserviceTransactionData>


    private lateinit var uncompletedTransactions:
            Cache<String, MutableSet<String>> // chiave id transazione, valore set di id servizi che l'hanno startata


    @PostConstruct
    private fun init() {
        transactionChannel.subscribe(this::handleTransactionMessage)

        transactionCache = Cache(rollbackTimeout, TimeUnit.MILLISECONDS)
        transactionCache.subscribe {
            if (uncompletedTransactions[it.first]?.isNotEmpty() == true) {
                logger.warn(
                    "Multiservice transaction {} has reached timeout without being completed by all services!",
                    it.first
                )
                handleExpiredTransaction(it.second)
            }
        }

        uncompletedTransactions = Cache(3 * rollbackTimeout, TimeUnit.MILLISECONDS)
        uncompletedTransactions.addVoter { !transactionCache.containsKey(it.first) }


    }

    @Synchronized
    private fun handleTransactionMessage(multiserviceTransactionMessage: MultiserviceTransactionMessage) {
        when (multiserviceTransactionMessage.status) {
            MultiserviceTransactionStatus.STARTED -> {
                synchronized(this) {
                    uncompletedTransactions.putIfAbsentAndThen(
                        multiserviceTransactionMessage.transactionID,
                        HashSet()
                    ) {
                        add(multiserviceTransactionMessage.serviceID)
                    }

                }
            }
            MultiserviceTransactionStatus.COMPLETED -> {
                synchronized(this) {
                    uncompletedTransactions[multiserviceTransactionMessage.transactionID]?.remove(
                        multiserviceTransactionMessage.serviceID
                    )

                }
            }
            MultiserviceTransactionStatus.FAILED -> {
                synchronized(this) {
                    uncompletedTransactions.remove(multiserviceTransactionMessage.transactionID)
                }
                rollbackTransaction(multiserviceTransactionMessage.transactionID)
            }
        }
    }


    private fun handleExpiredTransaction(multiserviceTransactionData: MultiserviceTransactionData) {
        synchronized(this) {
            uncompletedTransactions.remove(multiserviceTransactionData.id)
        }
        rollbackTransaction(multiserviceTransactionData.id, false)
    }


    fun <T> invokeWithinMultiserviceTransaction(
        rollback: Method,
        args: Array<Any?>,
        instance: Any,
        function: () -> T
    ): T {
        val currentTransactionID = MultiserviceTransactionContextHolder.getCurrentTransactionID()
        logger.info("Performing multiservice transaction step of transaction {}", currentTransactionID)
        val transactionData =
            MultiserviceTransactionData(currentTransactionID, rollback, args, instance, ReentrantLock(), function)

        transactionCache[currentTransactionID] = transactionData
        return transactionData.lock.withLock { startMultiserviceTransaction(transactionData) }
    }


    private fun <T> startMultiserviceTransaction(transactionData: MultiserviceTransactionData): T {
        val transactionID = transactionData.id
        try {
            logger.debug("Starting multiservice transaction {}", transactionID)
            transactionChannel.notifyTransactionStart(transactionID)
            val result = transactionInvoker.invokeWithinTransaction(transactionData.function) as T
            logger.debug("Multiservice transaction {} completed", transactionID)
            transactionChannel.notifyTransactionSuccess(transactionID)
            return result
        } catch (t: Throwable) {
            logger.error(
                "Error during multiservice transaction {}. Notifying other services after having performed rollback.",
                transactionID,
                t
            )
            transactionChannel.notifyTransactionFailure(transactionID)
            throw t
        }
    }

    private fun rollbackTransaction(transactionID: String, cleanTransactionCache: Boolean = true) {
        logger.warn("Performing manual rollback of multiservice transaction {}", transactionID)

        val transactionData = transactionCache[transactionID] ?: return
        transactionChannel.notifyTransactionFailure(transactionID)

        transactionData.lock.withLock {
            doRollbackTransaction(transactionData)
        }
        if (cleanTransactionCache)
            transactionCache.remove(transactionID)
    }

    private fun doRollbackTransaction(transactionData: MultiserviceTransactionData) {
        val transactionID = transactionData.id
        try {
            transactionData.rollback.invoke(transactionData.instance, *transactionData.args)
            logger.debug("Manual rollback of multiservice transaction {} performed successfully", transactionID)
        } catch (t2: Throwable) {
            logger.error("Error performing manual rollback of multiservice transaction {}", transactionID, t2)
            throw TransactionSystemException(
                "Application exception overridden by multiservice transaction exception",
                t2
            )
        }
    }
}
