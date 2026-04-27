package com.woocurlee.coupon.repository

import com.woocurlee.coupon.domain.CouponHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CouponHistoryRepository : JpaRepository<CouponHistory, Long> {
    fun existsByCouponIdAndUserId(couponId: Long, userId: Long): Boolean
}