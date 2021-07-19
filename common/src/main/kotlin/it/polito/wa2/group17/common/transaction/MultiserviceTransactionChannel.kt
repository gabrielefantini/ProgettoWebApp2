package it.polito.wa2.group17.common.transaction

import it.polito.wa2.group17.common.utils.AbstractSubscribable
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MultiserviceTransactionChannel : AbstractSubscribable<MultiserviceTransactionMessage>() {

    @Value("\${TODO}")
    private lateinit var topic: String
    private lateinit var kafkaProducer: KafkaProducer<String, MultiserviceTransactionMessage>
    private lateinit var kafkaConsumer: KafkaConsumer<String, MultiserviceTransactionMessage>

    @Value("\${spring.application.name}")
    private lateinit var serviceID: String

    init {
        initProducer()
        initConsumer()
    }

    private fun initConsumer() {
        TODO("Not yet implemented")
    }

    private fun initProducer() {
        TODO("Not yet implemented")
    }

    fun notifyTransactionStart(transactionID: String) {
        sendTransactionMessage(MultiserviceTransactionStatus.STARTED, transactionID)
    }

    fun notifyTransactionSuccess(transactionID: String) {
        sendTransactionMessage(MultiserviceTransactionStatus.COMPLETED, transactionID)
    }

    fun notifyTransactionFailure(transactionID: String) {
        sendTransactionMessage(MultiserviceTransactionStatus.FAILED, transactionID)
    }

    private fun sendTransactionMessage(transactionStatus: MultiserviceTransactionStatus, transactionID: String) {
        kafkaProducer.send(
            ProducerRecord(
                topic,
                MultiserviceTransactionMessage(
                    transactionStatus,
                    serviceID,
                    transactionID
                )
            )
        )
    }
}
