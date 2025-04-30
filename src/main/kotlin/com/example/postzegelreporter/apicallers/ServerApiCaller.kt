package com.example.postzegelreporter.apicallers

import com.example.postzegelreporter.apicallers.feign.PostzegelServerApi
import com.example.postzegelreporter.domain.events.PostzegelCodeDTO
import com.example.postzegelreporter.domain.mappers.PostzegelCodeMapper
import com.example.postzegelreporter.providers.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Service

@Service
class ServerApiCaller(
    private val timeProvider: TimeProvider,
    private val postzegelServerApi: PostzegelServerApi,
    private val postzegelCodeMapper: PostzegelCodeMapper,
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @ApplicationModuleListener
    fun post(postzegelCodeDTO: PostzegelCodeDTO) {
        logger.info("Sending out: ${postzegelCodeDTO.code} @ ${timeProvider.instantNow()}")
        postzegelServerApi.postStampCode(
            postzegelCodeMapper.toRequest(postzegelCodeDTO),
            postzegelCodeDTO.idempotencyKey,
        )
        logger.info("Sending out successful: ${postzegelCodeDTO.code} @ ${timeProvider.instantNow()}")
    }
}
