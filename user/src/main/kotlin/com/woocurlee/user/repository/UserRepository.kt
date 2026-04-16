package com.woocurlee.user.repository

import com.woocurlee.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: JpaRepository<User, Long> {
    fun findByUserIdAndDeliveryAddress(userId: Long, deliveryAddress: String): User?
}