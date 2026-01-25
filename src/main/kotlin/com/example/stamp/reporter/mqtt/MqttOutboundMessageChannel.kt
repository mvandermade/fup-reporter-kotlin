package com.example.stamp.reporter.mqtt

import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory
import org.springframework.integration.mqtt.core.MqttPahoClientFactory
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHandler

@Configuration
class MqttOutboundMessageChannel {
    @Value("\${application.mqtt.broker-url}")
    private lateinit var brokerUrl: String

    @Value("\${application.mqtt.client-id}")
    private lateinit var clientId: String

    @Value("\${application.mqtt.outbound-topic}")
    private lateinit var outboundTopic: String

    @Bean
    fun mqttClientFactory(): MqttPahoClientFactory {
        val factory = DefaultMqttPahoClientFactory()
        val options = MqttConnectOptions()
        options.setServerURIs(arrayOf(brokerUrl))
        factory.setConnectionOptions(options)
        return factory
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    fun mqttOutbound(): MessageHandler {
        val messageHandler = MqttPahoMessageHandler("$clientId-outbound", mqttClientFactory())
        messageHandler.setAsync(true)
        messageHandler.setDefaultTopic(outboundTopic)
        return messageHandler
    }

    @Bean
    fun mqttOutboundChannel(): MessageChannel = DirectChannel()
}
