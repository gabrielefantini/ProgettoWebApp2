package it.polito.wa2.group17.common.transaction

import java.time.Instant

enum class MultiserviceTransactionStatus {
    STARTED,
    COMPLETED,
    FAILED
}

data class MultiserviceTransactionMessage(
    var status: MultiserviceTransactionStatus,
    var serviceID: String,
    var transactionID: String,
    var timestamp: Instant = Instant.now()
)
