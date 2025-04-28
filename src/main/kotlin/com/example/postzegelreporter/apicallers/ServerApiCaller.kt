package com.example.postzegelreporter.apicallers

import com.example.postzegelreporter.domain.PostzegelCode
import org.slf4j.LoggerFactory
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Service

@Service
class ServerApiCaller() {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @ApplicationModuleListener
    fun post(postzegelCode: PostzegelCode) {
        logger.info("Received ${postzegelCode.code}")
//        Thread.sleep(100000)
    }
}
