package it.polito.wa2.group17.common.utils

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

abstract class AbstractSubscribable<T> {
    private val listenerIndex = AtomicInteger(0)
    protected val listeners = ConcurrentHashMap<Int, (T) -> Unit>()

    fun subscribe(listener: (T) -> Unit): Int {
        synchronized(listeners){
            val id = listenerIndex.getAndIncrement()
            listeners[id] = listener
            return id
        }
    }

    fun unsubscribe(subscriptionID: Int) {
        synchronized(listeners){
            listeners.remove(subscriptionID)
        }
    }
}
