package com.example.postzegelreporter.domain.mappers

import com.example.postzegelreporter.domain.PostzegelCodeRequest
import com.example.postzegelreporter.domain.messages.PostzegelCodeDTO
import org.springframework.stereotype.Component

@Component
class PostzegelCodeMapper {
    fun toRequest(postzegelCodeDTO: PostzegelCodeDTO) =
        PostzegelCodeRequest(
            readAt = postzegelCodeDTO.readAt,
            code = postzegelCodeDTO.code,
        )
}
