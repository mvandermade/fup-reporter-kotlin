package com.example.postzegelreporter.apicallers.feign

import com.example.postzegelreporter.domain.PostzegelCodeRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@FeignClient(name = "PostzegelServerApi", url = "\${application.apicallers.postzegelserver.url}")
interface PostzegelServerApi {
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/v1/reports/stamp-code"],
        consumes = ["application/json"],
    )
    fun postStampCode(
        @RequestBody stampCode: PostzegelCodeRequest,
        @RequestHeader(value = "idempotency-key", required = true) idempotencyKey: String,
    )
}
