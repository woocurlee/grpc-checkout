package com.woocurlee.coupon.domain

import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

class CouponHistory(
    @Id @GeneratedValue
    var couponHistoryId: Long? = null,
    var couponId: Long = 0L,
    var userId: Long? = null,
)