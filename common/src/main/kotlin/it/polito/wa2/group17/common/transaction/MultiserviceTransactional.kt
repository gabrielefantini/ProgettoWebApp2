package it.polito.wa2.group17.common.transaction

import java.lang.reflect.Method

@Target(AnnotationTarget.FUNCTION)
annotation class MultiserviceTransactional(val transactionName: String = "") {
    companion object {
        fun extractTransactionName(method: Method): String {
            var transactionName = method.getAnnotation(MultiserviceTransactional::class.java).transactionName
            if (transactionName.isBlank())
                transactionName = method.name
            return transactionName
        }
    }
}

@Target(AnnotationTarget.FUNCTION)
annotation class Rollback(val transactionName: String = "") {
    companion object {
        fun extractTransactionName(method: Method): String {
            var transactionName = method.getAnnotation(Rollback::class.java).transactionName
            if (transactionName.isBlank())
                transactionName = method.name.replace("rollbackFor", "")
                    .replaceFirstChar { it.lowercaseChar() }
            return transactionName
        }
    }
}

