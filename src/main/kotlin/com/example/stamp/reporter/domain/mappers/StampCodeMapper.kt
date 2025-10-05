package com.example.stamp.reporter.domain.mappers

import com.example.stamp.reporter.domain.StampCodeRequest
import com.example.stamp.reporter.domain.messages.StampCodeDTO
import com.example.stamp.reporter.workflows.domain.SendToExchangeInput1
import org.springframework.stereotype.Component

@Component
class StampCodeMapper {
    fun toRequest(stampCodeDTO: StampCodeDTO) =
        StampCodeRequest(
            readAt = stampCodeDTO.readAt,
            code = stampCodeDTO.code,
        )

    fun toRequest(sendToExchangeInput1: SendToExchangeInput1) =
        StampCodeRequest(
            readAt = sendToExchangeInput1.readAt,
            code = sendToExchangeInput1.code,
        )
}
