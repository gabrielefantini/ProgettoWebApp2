package it.polito.wa2.group17.common.transaction

import lombok.EqualsAndHashCode
import java.lang.reflect.Method
import java.time.Instant
import java.util.concurrent.locks.Lock

data class MultiserviceTransactionData(
    val id: String,
    val rollback: Method,
    val args: Array<Any?>,
    val instance: Any?,
    val lock: Lock,
    val function: () -> Any?,
    val startingTime : Instant = Instant.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MultiserviceTransactionData

        if (id != other.id) return false
        if (rollback != other.rollback) return false
        if (!args.contentEquals(other.args)) return false
        if (instance != other.instance) return false
        if (lock != other.lock) return false
        if (function != other.function) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + rollback.hashCode()
        result = 31 * result + args.contentHashCode()
        result = 31 * result + (instance?.hashCode() ?: 0)
        result = 31 * result + lock.hashCode()
        result = 31 * result + function.hashCode()
        return result
    }

}
