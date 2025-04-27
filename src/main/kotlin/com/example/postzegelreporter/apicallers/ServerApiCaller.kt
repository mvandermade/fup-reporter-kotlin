package com.example.postzegelreporter.apicallers

import com.example.postzegelreporter.domain.PostzegelCode
import org.slf4j.LoggerFactory
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Service
import kotlin.random.Random
import kotlin.system.exitProcess

@Service
class ServerApiCaller() {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @ApplicationModuleListener
    fun post(postzegelCode: PostzegelCode) {
        logger.info("Received ${postzegelCode.code}")
        if (Random.nextBoolean()) {
            exitProcess(0)
        }
    }
}
