package it.polito.wa2.group17.common.transaction

enum class MultiserviceTransactionStatus {
    STARTED,
    COMPLETED,
    FAILED
}

data class MultiserviceTransactionMessage(
    val status: MultiserviceTransactionStatus,
    val serviceID: String,
    val transactionID: String
)
