package it.polito.wa2.group17.common.transaction

import org.springframework.stereotype.Component
import java.lang.reflect.Method

@Component
class MultiserviceTransactionLinker {
    private val rollbacks = HashMap<String, Method>()

    fun getRollbackFor(transactionName: String): Method {
        if (!rollbacks.contains(transactionName))
            throw IllegalStateException("No rollback found for transaction $transactionName!")
        return rollbacks[transactionName]!!
    }

    fun registerRollbackFor(transactionName: String, rollback: Method) {
        if (rollbacks.contains(transactionName))
            throw java.lang.IllegalStateException("Multiple rollbacks for $transactionName: ${rollbacks[transactionName]!!} and $rollback")
        rollbacks[transactionName] = rollback
    }
}
