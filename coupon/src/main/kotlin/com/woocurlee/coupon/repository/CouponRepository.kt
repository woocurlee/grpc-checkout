package com.woocurlee.coupon.repository

import com.woocurlee.coupon.domain.Coupon
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CouponRepository : JpaRepository<Coupon, Long> {
    fun findCouponByCouponId(couponId: Long): Coupon?
}