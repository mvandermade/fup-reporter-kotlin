package com.example.stamp.reporter.readers

import balancerapi.BalancerSvcGrpc
import balancerapi.WorkAcknowledgement
import com.example.stamp.reporter.domain.kafka.KafkaSender
import com.example.stamp.reporter.domain.kafka.TOPIC_SERIAL_STAMP
import com.example.stamp.reporter.domain.messages.StampCodeDTO
import com.example.stamp.reporter.providers.TimeProvider
import com.example.stamp.reporter.websockets.domain.WebSocketSerialEventMessage
import com.example.stamp.reporter.websockets.handlers.TrackerWebsocketHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicBoolean

@Service
class IOBalancerReader(
    private val ioBalancerStub: BalancerSvcGrpc.BalancerSvcBlockingV2Stub,
    private val timeProvider: TimeProvider,
    private val kafkaSender: KafkaSender,
    private val trackerWebsocketHandler: TrackerWebsocketHandler,
    @param:Value("\${application.grpc.client.channels.io-balancer.enabled}") private val enabled: Boolean,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val isShuttingDown = AtomicBoolean(false)

    @EventListener(ContextClosedEvent::class)
    fun onContextClosedEvent(contextClosedEvent: ContextClosedEvent) {
        println("ContextClosedEvent occurred at millis: " + contextClosedEvent.getTimestamp())
        isShuttingDown.set(true)
    }

    @Scheduled(fixedDelay = 5000L)
    fun listenToBalancer() {
        if (!enabled) return
        val work = ioBalancerStub.work()
        while (!isShuttingDown.get()) {
            val assignment = work.read()

            val zdt = timeProvider.zonedDateTimeNowSystem()
            logger.info("Read IO balanced Event: ${assignment.postzegelCode} @ $zdt")

            val stampCodeDTO =
                StampCodeDTO(
                    readAt = zdt,
                    code = assignment.postzegelCode,
                    idempotencyKey = assignment.idempotencyId.toString(),
                    kafkaKey = assignment.postzegelCode,
                )

            logger.info("Read Serial Event: $stampCodeDTO")
            trackerWebsocketHandler.sendAll(WebSocketSerialEventMessage(code = stampCodeDTO.code))

            kafkaSender.sendMessage(
                TOPIC_SERIAL_STAMP,
                stampCodeDTO,
            )
            logger.info("Sent to kafka io balanced event: ${assignment.postzegelCode} @ $zdt")

            val ack =
                WorkAcknowledgement
                    .newBuilder()
                    .setTaskId(assignment.taskId)
                    .build()

            work.write(ack)
        }
    }
}
