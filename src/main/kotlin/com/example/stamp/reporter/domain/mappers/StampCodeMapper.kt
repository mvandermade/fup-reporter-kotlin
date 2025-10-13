package com.example.stamp.reporter.domain.mappers

import com.example.stamp.reporter.domain.StampCodeRequest
import com.example.stamp.reporter.domain.messages.StampCodeDTO
import com.example.stamp.reporter.workflows.domain.ReadStampCode
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
