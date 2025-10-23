package com.example.stamp.reporter.workflows.mappers

import com.example.stamp.reporter.domain.messages.ReadStampCode
import com.example.stamp.reporter.domain.messages.StampCodeDTO
import com.example.stamp.reporter.domain.requests.StampCodeRequest
import org.springframework.stereotype.Component

@Component
class StampCodeMapper {
    fun toRequest(stampCodeDTO: StampCodeDTO) =
        StampCodeRequest(
            readAt = stampCodeDTO.readAt,
            code = stampCodeDTO.code,
        )

    fun toRequest(readStampCode: ReadStampCode) =
        StampCodeRequest(
            readAt = readStampCode.readAt,
            code = readStampCode.code,
        )
}
