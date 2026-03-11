package com.woocurlee.grpccheckout

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GrpcCheckoutApplication

fun main(args: Array<String>) {
    runApplication<GrpcCheckoutApplication>(*args)
}
