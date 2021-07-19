package it.polito.wa2.group17.common.utils

import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class Cache<K, V>(val timeout: Long, val timeUnit: TimeUnit) :
    AbstractSubscribable<Pair<K, V>>(),
    MutableMap<K, V> {

    private val innerMap = HashMap<K, Pair<Instant, V>>()
    private val voters = HashSet<(Pair<K, V>) -> Boolean>().apply { add { true } }
    private val lock = ReentrantReadWriteLock()
    private lateinit var cleanSchedule: ScheduledFuture<*>

    init {
        restart()
    }

    fun addVoter(voter: (Pair<K, V>) -> Boolean) {
        synchronized(voters) {
            voters.add(voter)
        }
    }

    fun removeVoter(voter: (Pair<K, V>) -> Boolean) {
        synchronized(voters) {
            voters.remove(voter)
        }
    }

    fun restart() {
        if (this::cleanSchedule.isInitialized && !(cleanSchedule.isDone || cleanSchedule.isCancelled))
            throw IllegalAccessException("Cannot restart a cache which is not stopped!")

        cleanSchedule = Executors.newScheduledThreadPool(1)
            .scheduleAtFixedRate(this::cleanMap, timeout, timeout, timeUnit)
    }


    fun stop() {
        cleanSchedule.cancel(false)
    }

    private fun cleanMap() {
        lock.write {
            val iter = innerMap.iterator()
            val cleanTime = Instant.now()
            while (iter.hasNext()) {
                val value = iter.next()
                if (value.value.first.plus(timeout, timeUnit.toChronoUnit()).isBefore(cleanTime) &&
                    voters.all { it(value.key to value.value.second) }
                ) {
                    listeners.values.forEach { it(value.key to value.value.second) }
                    iter.remove()
                }
            }
        }
    }

    override val size: Int
        get() = lock.read { innerMap.size }


    override fun containsKey(key: K): Boolean =
        lock.read { innerMap.containsKey(key) }


    override fun containsValue(value: V): Boolean =
        lock.read { innerMap.values.stream().anyMatch { it.second?.equals(value) == true } }


    override fun get(key: K): V? =
        lock.read { innerMap[key]?.second }


    override fun isEmpty(): Boolean = lock.read { innerMap.isEmpty() }


    override fun clear() = lock.write { innerMap.clear() }

    override fun put(key: K, value: V): V? =
        lock.write {
            innerMap.put(key, Instant.now() to value)?.second
        }

    override fun putAll(from: Map<out K, V>) {
        lock.write {
            for (entries in from) {
                innerMap[entries.key] = Instant.now() to entries.value
            }
        }
    }

    override fun remove(key: K): V? = lock.write { innerMap.remove(key)?.second }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = throw IllegalAccessException("Cannot access entries of ${javaClass.name}")

    override val keys: MutableSet<K>
        get() = lock.read { innerMap.keys }
    override val values: MutableCollection<V>
        get() = throw IllegalAccessException("Cannot access values of ${javaClass.name}")


}
