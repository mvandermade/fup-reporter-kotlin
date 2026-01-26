package com.example.stamp.reporter.testutils

import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.MountableFile

fun startMqttContainer(): GenericContainer<Nothing> =
    GenericContainer<Nothing>("eclipse-mosquitto:2.0.22").apply {
        withExposedPorts(1883)
        withCopyToContainer(
            MountableFile.forClasspathResource("mosquitto/config/mosquitto.conf"),
            "/mosquitto/config/mosquitto.conf",
        )
        waitingFor(Wait.forLogMessage(".*mosquitto version 2.0.22 running.*", 1))
        start()
    }

fun addMqttToRegistry(
    registry: DynamicPropertyRegistry,
    mqttContainer: GenericContainer<*>,
) {
    registry.add("application.mqtt.broker-url") { "tcp://" + mqttContainer.host + ":" + mqttContainer.firstMappedPort }
}
