package it.polito.wa2.group17.common.transaction

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.TransactionSystemException
import java.lang.reflect.Method
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.PostConstruct
import kotlin.concurrent.withLock

@Component
class MultiserviceTransactionSynchronizer {

    private companion object {
        val logger = LoggerFactory.getLogger(MultiserviceTransactionSynchronizer::class.java)
    }

    @Autowired
    private lateinit var transactionInvoker: TransactionInvoker

    @Autowired
    private lateinit var transactionChannel: MultiserviceTransactionChannel

    @Value("\${transaction.rollbackTimeout:10000}")
    private var rollbackTimeout: Long = 10000

    private val transactionMap = ConcurrentHashMap<String, MultiserviceTransactionData>()

    @PostConstruct
    private fun initializeMonitor() {
        transactionChannel.subscribeToTransactionMessages(this::handleTransactionMessage)
        Executors.newScheduledThreadPool(1)
            .scheduleAtFixedRate(this::cleanTransactions, rollbackTimeout, rollbackTimeout, TimeUnit.MILLISECONDS)
    }

    private fun handleTransactionMessage(multiserviceTransactionMessage: MultiserviceTransactionMessage) {
        when (multiserviceTransactionMessage.status) {
            MultiserviceTransactionStatus.STARTED -> TODO()
            MultiserviceTransactionStatus.COMPLETED -> TODO()
            MultiserviceTransactionStatus.FAILED -> rollbackTransaction(multiserviceTransactionMessage.transactionID)
        }
    }

    private fun cleanTransactions() {
        //FIXME
        logger.info("Performing multiservice transactions clean")
        val transactionIterator = transactionMap.iterator()
        val cleanTimePoint = Instant.now()
        while (transactionIterator.hasNext()) {
            val transaction = transactionIterator.next()
            if (transaction.value.startingTime.plusMillis(rollbackTimeout).isBefore(cleanTimePoint)) {
                logger.info("Removing multiservice transaction {}", transaction.key)
                transaction.value.lock.withLock { transactionIterator.remove() }
                logger.debug("Multiservice Transaction {} removed", transaction.key)
            }
        }
    }

    fun <T> invokeWithinMultiserviceTransaction(
        rollback: Method,
        args: Array<Any?>,
        instance: Any,
        function: () -> T
    ): T {
        val currentTransactionID: String = "a" //TODO()
        logger.info("Performing multiservice transaction step of transaction {}", currentTransactionID)
        val transactionData =
            MultiserviceTransactionData(currentTransactionID, rollback, args, instance, ReentrantLock(), function)

        transactionMap[currentTransactionID] = transactionData
        return transactionData.lock.withLock { startMultiserviceTransaction(transactionData) }
    }


    private fun <T> startMultiserviceTransaction(transactionData: MultiserviceTransactionData): T {
        val transactionID = transactionData.id
        try {
            transactionChannel.notifyTransactionStart(transactionID)
            val result = transactionInvoker.invokeWithinTransaction(transactionData.function) as T
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

    private fun rollbackTransaction(transactionID: String) {
        logger.warn("Performing manual rollback of multiservice transaction {}", transactionID)
        val transactionData = transactionMap[transactionID] ?: return
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
