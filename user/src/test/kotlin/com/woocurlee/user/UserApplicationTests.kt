package com.woocurlee.user

import com.woocurlee.proto.user.ValidateUserRequest
import com.woocurlee.user.domain.User
import com.woocurlee.user.repository.UserRepository
import com.woocurlee.user.service.UserService
import io.grpc.Status
import io.grpc.StatusException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class UserApplicationTests : BehaviorSpec({
    val userRepository = mockk<UserRepository>()
    val userService = UserService(userRepository)

    given("유저 유효성 검증 요청시") {
        `when`("존재하지 않는 유저라면") {
            every { userRepository.findByUserIdAndDeliveryAddress(any(), any()) } returns null

            then("NOT_FOUND 에러를 발생시킨다.") {
                val ex = shouldThrow<StatusException> {
                    userService.findValidUser(ValidateUserRequest.newBuilder().apply {
                        userId = 1
                        deliveryAddress = "서울시 관악구"
                    }.build())
                }

                ex.status.code shouldBe Status.NOT_FOUND.code
            }
        }

        `when`("정지된 유저라면") {
            every { userRepository.findByUserIdAndDeliveryAddress(any(), any()) } returns User(status = "BLOCK")

            then("PERMISSION_DENIED 에러를 발생시킨다.") {
                val ex = shouldThrow<StatusException> {
                    userService.findValidUser(ValidateUserRequest.newBuilder().apply {
                        userId = 2
                        deliveryAddress = "서울시 강남구"
                    }.build())
                }

                ex.status.code shouldBe Status.PERMISSION_DENIED.code
            }
        }

        `when`("정상적인 유저라면") {
            every { userRepository.findByUserIdAndDeliveryAddress(any(), any()) } returns User(
                userName = "홍길동",
                status = "ACTIVE"
            )

            then("검증 결과를 반환한다.") {
                val user = userService.findValidUser(ValidateUserRequest.newBuilder().apply {
                    userId = 3
                    deliveryAddress = "서울시 성동구"
                }.build())

                user.isValid shouldBe true
                user.userName shouldBe "홍길동"
            }
        }
    }
})
