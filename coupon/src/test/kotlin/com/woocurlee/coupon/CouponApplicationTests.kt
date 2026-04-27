package com.woocurlee.coupon

import com.woocurlee.coupon.domain.Coupon
import com.woocurlee.coupon.repository.CouponHistoryRepository
import com.woocurlee.coupon.repository.CouponRepository
import com.woocurlee.coupon.service.CouponService
import com.woocurlee.proto.coupon.IssueCouponRequest
import io.grpc.Status
import io.grpc.StatusException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime


class CouponApplicationTests : BehaviorSpec({
    val couponRepository = mockk<CouponRepository>()
    val couponHistoryRepository = mockk<CouponHistoryRepository>()
    val couponService = CouponService(couponRepository, couponHistoryRepository)

    given("쿠폰 존재 여부 확인시") {
        `when`("쿠폰 코드가 없으면") {
            then("discountAmount 0을 반환한다.") {
                val result = couponService.issueCoupon(
                    IssueCouponRequest.newBuilder()
                        .apply {
                            couponId = 0
                        }.build()
                )

                result.discountAmount shouldBe 0
            }
        }

        `when`("쿠폰이 존재하지 않으면") {
            every { couponRepository.findCouponByCouponId(any()) } returns null

            then("NOT_FOUND 에러를 발생시킨다.") {
                val ex = shouldThrow<StatusException> {
                    val req = IssueCouponRequest.newBuilder().apply {
                        couponId = 1L
                    }.build()

                    couponService.issueCoupon(req)
                }

                ex.status.code shouldBe Status.NOT_FOUND.code
            }
        }
    }

    given("쿠폰 만료 여부 확인시") {
        every { couponRepository.findCouponByCouponId(any()) } returns Coupon(
            couponId = 1L,
            effectiveStartDate = LocalDateTime.now().plusDays(1),
        )

        `when`("쿠폰이 만료되었으면") {
            then("FAILED_PRECONDITION 에러를 발생시킨다.") {
                val ex = shouldThrow<StatusException> {
                    val req = IssueCouponRequest.newBuilder().apply {
                        couponId = 1L
                    }.build()

                    couponService.issueCoupon(req)
                }

                ex.status.code shouldBe Status.FAILED_PRECONDITION.code
            }
        }
    }

    given("쿠폰 사용 이력 확인시") {
        every { couponRepository.findCouponByCouponId(any()) } returns Coupon(
            couponId = 1L,
            effectiveStartDate = LocalDateTime.now().minusDays(1),
            effectiveEndDate = LocalDateTime.now().plusDays(1),
        )

        every { couponHistoryRepository.existsByCouponIdAndUserId(any(), any()) } returns true

        `when`("쿠폰 사용 이력이 있다면") {
            then("ALREADY_EXISTS 에러를 발생시킨다.") {
                val ex = shouldThrow<StatusException> {
                    val req = IssueCouponRequest.newBuilder().apply {
                        couponId = 1L
                        userId = 1L
                    }.build()

                    couponService.issueCoupon(req)
                }

                ex.status.code shouldBe Status.ALREADY_EXISTS.code
            }
        }

        `when`("쿠폰 사용 이력이 없다면") {
            every { couponRepository.findCouponByCouponId(any()) } returns Coupon(
                couponId = 1L,
                effectiveStartDate = LocalDateTime.now().minusDays(1),
                effectiveEndDate = LocalDateTime.now().plusDays(1),
                discountAmount = 1000
            )

            every { couponHistoryRepository.existsByCouponIdAndUserId(any(), any()) } returns false

            then("쿠폰 사용에 성공한다.") {
                val result = couponService.issueCoupon(
                    IssueCouponRequest.newBuilder().apply {
                        couponId = 1L
                        userId = 1L
                        totalPrice = 10000
                    }.build()
                )

                result.discountAmount shouldBe 1000
                result.finalPrice shouldBe 9000
            }
        }
    }
})
