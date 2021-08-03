package it.polito.wa2.group17.common.utils

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

interface Subscribable<T> {
    fun subscribe(listener: (T) -> Unit): Int
    fun unsubscribe(subscriptionID: Int)
}

abstract class AbstractSubscribable<T>: Subscribable<T> {
    private val listenerIndex = AtomicInteger(0)
    protected val listeners = ConcurrentHashMap<Int, (T) -> Unit>()

    override fun subscribe(listener: (T) -> Unit): Int {
        synchronized(listeners) {
            val id = listenerIndex.getAndIncrement()
            listeners[id] = listener
            return id
        }
    }

    override fun unsubscribe(subscriptionID: Int) {
        synchronized(listeners) {
            listeners.remove(subscriptionID)
        }
    }

    protected fun sendToAllListeners(msg: T) {
        listeners.forEach { it.value(msg) }
    }

    protected fun sendToAllListeners(msgs: Iterable<T>) {
        msgs.forEach { msg -> listeners.forEach { it.value(msg) } }
    }

    protected fun <Q> sendToAllListeners(msgs: Iterable<Q>, mapper: (Q) -> T) {
        msgs.forEach { msg -> listeners.forEach { it.value(mapper(msg)) } }
    }
}
