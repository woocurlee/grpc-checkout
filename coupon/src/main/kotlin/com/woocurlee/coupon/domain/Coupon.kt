package com.woocurlee.coupon.domain

import com.woocurlee.coupon.enums.DiscountType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity(name = "coupons")
class Coupon(
    @Id
    @GeneratedValue
    var couponId: Long? = null,
    var couponName: String = "",
    @Enumerated(EnumType.STRING)
    var discountType: DiscountType = DiscountType.FIXED,
    var discountAmount: Int = 0,
    var effectiveStartDate: LocalDateTime = LocalDateTime.now(),
    var effectiveEndDate: LocalDateTime = LocalDateTime.now(),
)