package com.woocurlee.coupon.grpc

import com.woocurlee.coupon.service.CouponService
import com.woocurlee.proto.coupon.CouponServiceGrpcKt
import com.woocurlee.proto.coupon.IssueCouponRequest
import com.woocurlee.proto.coupon.IssueCouponResponse
import org.springframework.grpc.server.service.GrpcService

@GrpcService
class CouponGrpcService(
    private val couponService: CouponService
) : CouponServiceGrpcKt.CouponServiceCoroutineImplBase() {
    override suspend fun issueCoupon(request: IssueCouponRequest): IssueCouponResponse {
        return couponService.issueCoupon(request)
    }
}