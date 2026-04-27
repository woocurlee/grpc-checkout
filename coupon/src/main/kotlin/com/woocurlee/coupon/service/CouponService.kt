package com.woocurlee.coupon.service

import com.woocurlee.coupon.repository.CouponHistoryRepository
import com.woocurlee.coupon.repository.CouponRepository
import com.woocurlee.proto.coupon.IssueCouponRequest
import com.woocurlee.proto.coupon.IssueCouponResponse
import io.grpc.Status
import io.grpc.StatusException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CouponService(
    private val couponRepository: CouponRepository,
    private val couponHistoryRepository: CouponHistoryRepository,
) {
    fun issueCoupon(req: IssueCouponRequest): IssueCouponResponse {
        val result = IssueCouponResponse.newBuilder()
            .apply {
                this.discountAmount = 0
                this.finalPrice = 0
            }.build()

        if (req.couponId == 0L) return result

        val coupon = couponRepository.findCouponByCouponId(req.couponId)
            ?: throw StatusException(Status.NOT_FOUND.withDescription("Coupon code not found"))

        val now = LocalDateTime.now()
        if (now.isBefore(coupon.effectiveStartDate) || now.isAfter(coupon.effectiveEndDate)) {
            throw StatusException(Status.FAILED_PRECONDITION.withDescription("Coupon is expired"))
        }

        val isUsed = couponHistoryRepository.existsByCouponIdAndUserId(req.couponId, req.userId)

        if (isUsed) throw StatusException(Status.ALREADY_EXISTS.withDescription("Coupon is already used"))

        val discountAmount = coupon.discountType.calculate(req.totalPrice, coupon.discountAmount)

        return IssueCouponResponse.newBuilder()
            .apply {
                this.discountAmount = discountAmount
                this.finalPrice = req.totalPrice - discountAmount
            }.build()
    }
}