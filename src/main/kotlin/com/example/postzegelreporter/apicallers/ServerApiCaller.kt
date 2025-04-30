package com.example.postzegelreporter.apicallers

import com.example.postzegelreporter.domain.PostzegelCode
import com.example.postzegelreporter.providers.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Service

@Service
class ServerApiCaller(
    private val timeProvider: TimeProvider,
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @ApplicationModuleListener
    fun post(postzegelCode: PostzegelCode) {
        logger.info("Sending out: ${postzegelCode.code} @ ${timeProvider.instantNow()}")
//        Thread.sleep(100000)
        throw Exception("OOPS")
    }
}
