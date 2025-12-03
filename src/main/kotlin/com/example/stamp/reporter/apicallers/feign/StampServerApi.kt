package com.example.stamp.reporter.apicallers.feign

import com.example.stamp.reporter.domain.requests.StampCodeRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@FeignClient(name = "StampServerApi", url = "\${application.api-callers.stamp-server.url}")
interface StampServerApi {
    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/v1/reports/stamp-code"],
        consumes = ["application/json"],
    )
    fun postStampCode(
        @RequestBody stampCode: StampCodeRequest,
        @RequestHeader(value = "idempotency-key", required = true) idempotencyKey: String,
    )
}
