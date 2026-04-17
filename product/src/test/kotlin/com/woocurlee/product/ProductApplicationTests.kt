package com.woocurlee.product

import com.woocurlee.product.domain.Product
import com.woocurlee.product.repository.ProductRepository
import com.woocurlee.product.service.ProductService
import com.woocurlee.proto.product.CheckStockRequest
import com.woocurlee.proto.product.StockProduct
import io.grpc.Status
import io.grpc.StatusException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class ProductApplicationTests : BehaviorSpec({
    val productRepository = mockk<ProductRepository>()
    val productService = ProductService(productRepository)

    given("재고 확인시") {
        `when`("요청 수량보다 재고가 부족하면") {
            every { productRepository.findProductsByProductIdIn(any()) } returns (listOf(
                Product(productId = 1L, productName = "상품1", price = 10000, stock = 5),
                Product(productId = 2L, productName = "상품2", price = 20000, stock = 2),
            ))

            then("FAILED_PRECONDITION 에러를 발생시킨다.") {
                val ex = shouldThrow<StatusException> {
                    val req = listOf(
                        StockProduct.newBuilder().apply {
                            productId = 1L
                            quantity = 5
                        }.build(),
                        StockProduct.newBuilder().apply {
                            productId = 2L
                            quantity = 10
                        }.build()
                    )

                    productService.checkProductStock(CheckStockRequest.newBuilder().apply {
                        addAllProducts(req)
                    }.build())
                }

                ex.status.code shouldBe Status.FAILED_PRECONDITION.code
            }
        }

        `when`("요청 수량이 재고보다 작거나 같으면") {
            every { productRepository.findProductsByProductIdIn(any()) } returns (listOf(
                Product(productId = 1L, productName = "상품1", price = 10000, stock = 5),
                Product(productId = 2L, productName = "상품2", price = 20000, stock = 2),
            ))

            then("재고 가용 여부와 totalPrice를 응답한다.") {
                val req = listOf(
                    StockProduct.newBuilder().apply {
                        productId = 1L
                        quantity = 4
                    }.build(),
                    StockProduct.newBuilder().apply {
                        productId = 2L
                        quantity = 1
                    }.build()
                )

                val result = productService.checkProductStock(CheckStockRequest.newBuilder().apply {
                    addAllProducts(req)
                }.build())

                result.isAvailable shouldBe true
                result.totalPrice shouldBe 60_000
            }
        }
    }
})