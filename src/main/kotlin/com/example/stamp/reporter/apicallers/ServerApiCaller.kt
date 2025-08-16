package com.example.stamp.reporter.apicallers

import com.example.stamp.reporter.apicallers.feign.StampServerApi
import com.example.stamp.reporter.domain.kafka.TOPIC_SERIAL_STAMP
import com.example.stamp.reporter.domain.mappers.StampCodeMapper
import com.example.stamp.reporter.domain.messages.StampCodeDTO
import com.example.stamp.reporter.websockets.domain.WebSocketAckExchangeMessage
import com.example.stamp.reporter.websockets.domain.WebSocketPostExchangeMessage
import com.example.stamp.reporter.websockets.handlers.TrackerWebsocketHandler
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
class ServerApiCaller(
    private val stampServerApi: StampServerApi,
    private val stampCodeMapper: StampCodeMapper,
    private val trackerWebsocketHandler: TrackerWebsocketHandler,
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val objectMapper =
        with(jacksonObjectMapper()) {
            this.registerModule(JavaTimeModule())
            this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

    @KafkaListener(
        id = "serverApiCaller",
        topics = [TOPIC_SERIAL_STAMP],
    )
    fun receive(
        @Payload message: String,
        @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) ts: Long,
        @Header(KafkaHeaders.GROUP_ID) groupId: String,
    ) {
        val stampCodeDTO = objectMapper.readValue(message, StampCodeDTO::class.java)

        logger.info("Api calling: ${stampCodeDTO.code} ${logDetails(key, partition, topic, ts, groupId)}")

        trackerWebsocketHandler.sendAll(WebSocketPostExchangeMessage(stampCodeDTO.code))
        Thread.sleep(150)

        stampServerApi.postStampCode(
            stampCodeMapper.toRequest(stampCodeDTO),
            stampCodeDTO.idempotencyKey,
        )

        Thread.sleep(150)
        trackerWebsocketHandler.sendAll(WebSocketAckExchangeMessage(stampCodeDTO.code))

        logger.info("Api call OK: ${stampCodeDTO.code} ${logDetails(key, partition, topic, ts, groupId)}")
    }

    fun logDetails(
        key: String?,
        partition: Int,
        topic: String,
        ts: Long,
        groupId: String,
    ) = "topic=$topic,partition=$partition,key=$key,epoch=$ts,groupId=$groupId"
}
