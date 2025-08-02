package com.example.stamp.reporter.grpc

import balancerapi.BalancerSvcGrpc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.grpc.client.GrpcChannelFactory

@Configuration
class GrpcStub {
    @Bean
    fun stub(channels: GrpcChannelFactory): BalancerSvcGrpc.BalancerSvcBlockingV2Stub =
        BalancerSvcGrpc.newBlockingV2Stub(channels.createChannel("io-balancer"))
}
