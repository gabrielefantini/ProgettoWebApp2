package it.polito.wa2.group17.common.transaction

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Component
class MultiserviceTransactionChannel {

    private val listenerIndex = AtomicInteger(0)
    private val listeners = ConcurrentHashMap<Int, (MultiserviceTransactionMessage) -> Unit>()

    fun notifyTransactionStart(transactionID: String) {
        TODO()
    }

    fun notifyTransactionSuccess(transactionID: String) {
        TODO()

    }

    fun notifyTransactionFailure(transactionID: String) {
        TODO()
    }

    @Synchronized
    fun subscribeToTransactionMessages(listener: (MultiserviceTransactionMessage) -> Unit): Int {
        val id = listenerIndex.getAndIncrement()
        listeners[id] = listener
        return id
    }

    @Synchronized
    fun deSubscribeToTransactionMessages(subscriptionID: Int) {
        listeners.remove(subscriptionID)
    }
}
