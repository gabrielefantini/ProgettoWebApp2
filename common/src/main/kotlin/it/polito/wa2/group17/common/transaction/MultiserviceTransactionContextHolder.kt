package it.polito.wa2.group17.common.transaction

import org.springframework.stereotype.Component

@Component
class MultiserviceTransactionContextHolder {
    companion object {
        private val currentTransactionId = InheritableThreadLocal<String>()
        fun getCurrentTransactionID(): String = currentTransactionId.get()
    }

    fun setCurrentTransactionId(id: String) = currentTransactionId.set(id)
}
