package com.woocurlee.coupon.enums

enum class DiscountType {
    RATE {
        override fun calculate(totalPrice: Int, discountAmount: Int): Int {
            return (totalPrice * discountAmount / 100).coerceAtLeast(0)
        }
    },
    FIXED {
        override fun calculate(totalPrice: Int, discountAmount: Int): Int {
            return discountAmount
        }
    };

    abstract fun calculate(totalPrice: Int, discountAmount: Int): Int
}