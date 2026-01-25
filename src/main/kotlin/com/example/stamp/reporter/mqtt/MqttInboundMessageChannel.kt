package com.example.stamp.reporter.mqtt

import com.example.stamp.reporter.websockets.handlers.TrackerWebsocketHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.core.MessageProducer
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHandler

@Configuration
class MqttInboundMessageChannel(
    private val trackerWebsocketHandler: TrackerWebsocketHandler,
) {
    @Value("\${application.mqtt.broker-url}")
    private lateinit var brokerUrl: String

    @Value("\${application.mqtt.client-id}")
    private lateinit var clientId: String

    @Value("\${application.mqtt.inbound-topic}")
    private lateinit var inboundTopic: String

    @Bean
    fun mqttInputChannel(): MessageChannel = DirectChannel()

    @Bean
    fun inbound(): MessageProducer {
        val adapter =
            MqttPahoMessageDrivenChannelAdapter(
                brokerUrl,
                "$clientId-inbound",
                inboundTopic,
            )
        adapter.setCompletionTimeout(5000)
        adapter.setConverter(DefaultPahoMessageConverter())
        adapter.setQos(1)
        adapter.setOutputChannel(mqttInputChannel())
        return adapter
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    fun handler(): MessageHandler =
        MessageHandler { message ->
            sendMessage(message)
        }

    private fun sendMessage(message: Message<*>) {
        trackerWebsocketHandler.sendAll(
            message.payload.toString(),
        )
    }
}
