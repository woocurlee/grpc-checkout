package com.woocurlee.user.domain

import com.woocurlee.user.repository.UserRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class UserDataInitializer(
    private val userRepository: UserRepository
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        userRepository.saveAll(
            listOf(
                User(userName = "박국자", status = "ACTIVE", deliveryAddress = "서울시 서초구"),
                User(userName = "김기덕", status = "BLOCK", deliveryAddress = "서울시 강남구"),
                User(userName = "홍길동", status = "ACTIVE", deliveryAddress = "서울시 성동구"),
            )
        )
    }
}