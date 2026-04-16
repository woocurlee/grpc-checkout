package com.woocurlee.user.service

import com.woocurlee.proto.user.ValidateUserRequest
import com.woocurlee.proto.user.ValidateUserResponse
import com.woocurlee.user.repository.UserRepository
import io.grpc.Status
import io.grpc.StatusException
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun findValidUser(req: ValidateUserRequest): ValidateUserResponse {
        val user = userRepository.findByUserIdAndDeliveryAddress(req.userId, req.deliveryAddress)
            ?: throw StatusException(Status.NOT_FOUND)

        if (user.status != "ACTIVE") {
            throw StatusException(Status.PERMISSION_DENIED)
        }

        return ValidateUserResponse.newBuilder().apply {
            isValid = true
            userName = user.userName
        }.build()
    }
}