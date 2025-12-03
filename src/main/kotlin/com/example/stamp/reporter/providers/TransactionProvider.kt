package com.example.stamp.reporter.providers

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class TransactionProvider {
    // Because transactional uses proxies of Spring use it to enforce new transactions
    // https://stackoverflow.com/questions/29857418
    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRES_NEW)
    fun <T> newReadWrite(callable: () -> T): T = callable.invoke()
}
