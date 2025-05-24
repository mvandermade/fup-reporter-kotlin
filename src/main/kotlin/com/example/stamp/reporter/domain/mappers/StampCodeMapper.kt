package com.example.stamp.reporter.domain.mappers

import com.example.stamp.reporter.domain.StampCodeRequest
import com.example.stamp.reporter.domain.messages.StampCodeDTO
import org.springframework.stereotype.Component

@Component
class StampCodeMapper {
    fun toRequest(stampCodeDTO: StampCodeDTO) =
        StampCodeRequest(
            readAt = stampCodeDTO.readAt,
            code = stampCodeDTO.code,
        )
}
