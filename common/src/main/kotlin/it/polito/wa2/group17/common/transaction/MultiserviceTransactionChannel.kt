package it.polito.wa2.group17.common.transaction

import it.polito.wa2.group17.common.utils.AbstractSubscribable
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Component
class MultiserviceTransactionChannel : AbstractSubscribable<MultiserviceTransactionMessage>() {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(MultiserviceTransactionChannel::class.java)
    }

    private lateinit var kafkaProducer: KafkaProducer<String, MultiserviceTransactionMessage>
    private lateinit var kafkaConsumer: KafkaConsumer<String, MultiserviceTransactionMessage>

    @Value("\${spring.application.name}")
    private lateinit var serviceID: String

    @Value("\${transaction.kafka.bootstrapServer}")
    private lateinit var bootstrapServer: String

    @Value("\${transaction.kafka.topic:multiservice_transactions}")
    private lateinit var topic: String

    @Value("\${transaction.kafka.pollTimeout:1}")
    private var pollTimeout: Long = 1L

    @Value("\${transaction.kafka.pollInterval:1}")
    private var pollInterval: Long = 1L

    @Value("\${transaction.debug.auto-rollback.enabled:false}")
    private var autoRollbackEnabled: Boolean = false

    @Value("\${transaction.debug.auto-rollback.timeout:10}")
    private var autoRollbackTimeout: Long = 10L

    private lateinit var rollbackExecutor: ScheduledExecutorService

    @PostConstruct
    private fun init() {
        initProducer()
        initConsumer()
        if (autoRollbackEnabled)
            rollbackExecutor = Executors.newScheduledThreadPool(10)
    }

    private fun initConsumer() {
        val props = Properties()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServer
        props[ConsumerConfig.GROUP_ID_CONFIG] = serviceID
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
        props[JsonDeserializer.TRUSTED_PACKAGES] = MultiserviceTransactionMessage::class.java.packageName
        kafkaConsumer = KafkaConsumer(props)
        kafkaConsumer.subscribe(listOf(topic))
        Executors.newScheduledThreadPool(1)
        { r -> Thread(r).apply { name = javaClass.simpleName } }
            .scheduleAtFixedRate(this::pollMessageFromKafka, pollInterval, pollInterval, TimeUnit.SECONDS)
    }

    private fun initProducer() {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServer
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        kafkaProducer = KafkaProducer(props)
    }

    fun notifyTransactionStart(transactionID: String) {
        sendTransactionMessage(MultiserviceTransactionStatus.STARTED, transactionID)
        if (autoRollbackEnabled)
            rollbackExecutor.schedule(
                { mockTransactionFailure(transactionID) }, autoRollbackTimeout, TimeUnit.SECONDS
            )
    }

    private fun mockTransactionFailure(transactionID: String) {
        sendToAllListeners(
            MultiserviceTransactionMessage(
                MultiserviceTransactionStatus.FAILED,
                "rollback test auto failure",
                transactionID
            )
        )
    }

    fun notifyTransactionSuccess(transactionID: String) {
        sendTransactionMessage(MultiserviceTransactionStatus.COMPLETED, transactionID)
    }

    fun notifyTransactionFailure(transactionID: String) {
        sendTransactionMessage(MultiserviceTransactionStatus.FAILED, transactionID)
    }

    private fun sendTransactionMessage(transactionStatus: MultiserviceTransactionStatus, transactionID: String) {
        val message = MultiserviceTransactionMessage(
            transactionStatus,
            serviceID,
            transactionID
        )
        logger.debug("Sending message to kafka: {}", message)

        kafkaProducer.send(
            ProducerRecord(
                topic,
                message
            )
        )
    }

    private fun pollMessageFromKafka() {
        val records = kafkaConsumer.poll(Duration.ofSeconds(pollTimeout))
        logger.debug("Polled from kafka {} records : {}", records.count(), records)
        sendToAllListeners(records.filter { it.value().serviceID != serviceID }) { it.value() }
    }
}
