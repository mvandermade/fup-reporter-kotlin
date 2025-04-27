package com.example.postzegelreporter.providers

import org.springframework.stereotype.Component

@Component
class RandomProvider {
    private val charPool: List<Char> = ('A'..'B') + ('0'..'1')

    fun randomString(length: Int): String {
        val randomString =
            (1..length)
                .map { _ -> kotlin.random.Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")

        return randomString
    }
}
