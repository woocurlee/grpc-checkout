package com.woocurlee.user.grpc

import com.woocurlee.proto.user.UserServiceGrpcKt
import com.woocurlee.proto.user.ValidateUserRequest
import com.woocurlee.proto.user.ValidateUserResponse
import com.woocurlee.user.service.UserService
import org.springframework.grpc.server.service.GrpcService

@GrpcService
class UserGrpcService(private val userService: UserService) : UserServiceGrpcKt.UserServiceCoroutineImplBase() {
    override suspend fun validateUser(request: ValidateUserRequest): ValidateUserResponse {
        return userService.findValidUser(request)
    }
}