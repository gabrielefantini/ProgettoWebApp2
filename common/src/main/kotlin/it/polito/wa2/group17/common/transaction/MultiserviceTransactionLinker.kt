package it.polito.wa2.group17.common.transaction

import it.polito.wa2.group17.common.utils.putIfAbsentAndThen
import it.polito.wa2.group17.common.utils.reflection.hasReturnType
import org.springframework.stereotype.Component
import java.lang.reflect.Method

@Component
class MultiserviceTransactionLinker {

    private data class MultiserviceTransactionsLink(var transaction: Method? = null, var rollback: Method? = null)

    private val links = HashMap<String, MultiserviceTransactionsLink>()

    fun getRollbackFor(transactionName: String): Method =
        links[transactionName]?.rollback
            ?: throw IllegalStateException("No rollback found for transaction $transactionName!")

    fun getTransactionFor(transactionName: String): Method =
        links[transactionName]?.transaction
            ?: throw IllegalStateException("No transaction found for transaction $transactionName!")


    fun registerRollbackFor(transactionName: String, rollback: Method) {
        links.putIfAbsentAndThen(transactionName, MultiserviceTransactionsLink()) {
            if (this.rollback != null)
                throw IllegalStateException("Multiple rollbacks for $transactionName: ${this.rollback} and $rollback")
            this.rollback = rollback
            if (this.transaction != null)
                checkTransactionLinkCompatibility(this)
        }
    }

    fun registerTransactionFor(transactionName: String, transaction: Method) {
        links.putIfAbsentAndThen(transactionName, MultiserviceTransactionsLink()) {
            if (this.transaction != null)
                throw IllegalStateException("Multiple transactions with id $transactionName: ${this.transaction} and $transaction")
            this.transaction = transaction
            if (this.rollback != null)
                checkTransactionLinkCompatibility(this)
        }
    }

    private fun checkTransactionLinkCompatibility(link: MultiserviceTransactionsLink) {
        val transaction = link.transaction!!
        val rollback = link.rollback!!
        val invokingMethodReturnType = transaction.returnType
        val hasReturnType = transaction.hasReturnType()

        val invokingMethodParamTypes = transaction.parameterTypes
        val rollbackParamTypes = rollback.parameterTypes

        if ((rollbackParamTypes.size - if (hasReturnType) 1 else 0) != invokingMethodParamTypes.size)
            incompatibleRollbackException(rollback, transaction)

        for ((i, param) in invokingMethodParamTypes.withIndex()) {
            if (!param.equals(rollbackParamTypes[i]))
                incompatibleRollbackException(rollback, transaction)
        }

        if (hasReturnType && !rollbackParamTypes[invokingMethodParamTypes.size].equals(invokingMethodReturnType))
            incompatibleRollbackException(rollback, transaction)
    }

    private fun incompatibleRollbackException(rollback: Method, invokingMethod: Method): Nothing {
        throw IllegalStateException("Rollback parameters of $rollback does not match with the ones of the transaction $invokingMethod")

    }
}
