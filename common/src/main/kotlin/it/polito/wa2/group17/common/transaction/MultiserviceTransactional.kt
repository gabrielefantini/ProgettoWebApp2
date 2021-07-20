package it.polito.wa2.group17.common.transaction

@Target(AnnotationTarget.FUNCTION)
annotation class MultiserviceTransactional(val transactionName: String)

@Target(AnnotationTarget.FUNCTION)
annotation class RollbackFor(val transactionName: String)