package com.example.postzegelreporter.apicallers

import com.example.postzegelreporter.apicallers.feign.PostzegelServerApi
import com.example.postzegelreporter.domain.kafka.TOPIC_SERIAL_POSTZEGEL
import com.example.postzegelreporter.domain.mappers.PostzegelCodeMapper
import com.example.postzegelreporter.domain.messages.PostzegelCodeDTO
import com.example.postzegelreporter.providers.TimeProvider
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import feign.RetryableException
import org.slf4j.LoggerFactory
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
class ServerApiCaller(
    private val timeProvider: TimeProvider,
    private val postzegelServerApi: PostzegelServerApi,
    private val postzegelCodeMapper: PostzegelCodeMapper,
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val objectMapper =
        with(jacksonObjectMapper()) {
            this.registerModule(JavaTimeModule())
            this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    private var isShuttingDown = false

    @KafkaListener(
        id = "serverApiCaller",
        topics = [TOPIC_SERIAL_POSTZEGEL],
//        containerFactory = "kafkaListenerContainerFactoryDeadLetter"
    )
    fun receive(
        @Payload message: String,
        @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) ts: Long,
        @Header(KafkaHeaders.GROUP_ID) groupId: String,
    ) {
        throw Exception("Test DLT")
        val postzegelCodeDTO = objectMapper.readValue(message, PostzegelCodeDTO::class.java)

        logger.info("Api calling: ${postzegelCodeDTO.code} ${logDetails(key, partition, topic, ts, groupId)}")
        var isRetryable = true
        var retries = 0
        while (isRetryable) {
            if (isShuttingDown) {
                throw IllegalStateException("PreDestroy called")
            }
            try {
                postzegelServerApi.postStampCode(
                    postzegelCodeMapper.toRequest(postzegelCodeDTO),
                    postzegelCodeDTO.idempotencyKey,
                )
                isRetryable = false
            } catch (e: RetryableException) {
                retries++
                logger.warn(
                    "${logDetails(key, partition, topic, ts, groupId)} Caught retryable exception $retries times, retrying infinitely",
                )
                isRetryable = true
                Thread.sleep(1000)
            }
        }
        logger.info("Api call OK: ${postzegelCodeDTO.code} ${logDetails(key, partition, topic, ts, groupId)}")
    }

    @EventListener(ContextClosedEvent::class)
    fun onApplicationEvent(event: ContextClosedEvent?) {
        logger.info("Server Api caller going to shutdown...")
        isShuttingDown = true
    }

    fun logDetails(
        key: String?,
        partition: Int,
        topic: String,
        ts: Long,
        groupId: String,
    ) = "topic=$topic,partition=$partition,key=$key,epoch=$ts,groupId=$groupId"
}
