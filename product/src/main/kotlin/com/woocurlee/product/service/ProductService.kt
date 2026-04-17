package com.woocurlee.product.service

import com.woocurlee.product.domain.Product
import com.woocurlee.product.repository.ProductRepository
import com.woocurlee.proto.product.CheckStockRequest
import com.woocurlee.proto.product.CheckStockResponse
import com.woocurlee.proto.product.DeductStockRequest
import com.woocurlee.proto.product.DeductStockResponse
import com.woocurlee.proto.product.StockProduct
import io.grpc.Status
import io.grpc.StatusException
import jakarta.transaction.Transactional
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val redissonClient: RedissonClient,
) {
    fun checkProductStock(req: CheckStockRequest): CheckStockResponse {
        val (_, totalPrice) = validateStock(req.productsList)

        return CheckStockResponse.newBuilder()
            .apply {
                this.isAvailable = true
                this.totalPrice = totalPrice
            }.build()
    }

    @Transactional
    fun deductProductStock(req: DeductStockRequest): DeductStockResponse {
        val multiLock = redissonClient.getMultiLock(
            *req.productsList.sortedBy { it.productId }.map { redissonClient.getLock("lock:product:${it.productId}") }
                .toTypedArray()
        )

        val acquired = multiLock.tryLock(0, 5, TimeUnit.SECONDS)
        if (!acquired) {
            throw StatusException(Status.RESOURCE_EXHAUSTED)
        }

        try {
            val (products, _) = validateStock(req.productsList)

            products.forEach { product ->
                product.stock -= req.productsList.find { it.productId == product.productId }!!.quantity
            }
        } finally {
            multiLock.unlock()
        }

        return DeductStockResponse.newBuilder()
            .apply {
                this.isSuccess = true
            }.build()
    }

    private fun validateStock(stockProducts: List<StockProduct>): Pair<List<Product>, Int> {
        val products = productRepository.findProductsByProductIdIn(stockProducts.map { it.productId })
        val productMap = products.associateBy { it.productId }

        var totalPrice = 0
        stockProducts.forEach { product ->
            if (product.quantity > productMap[product.productId]!!.stock) {
                throw StatusException(
                    Status.FAILED_PRECONDITION.withDescription("상품 ${product.productId}의 재고가 부족합니다.")
                )
            }
            totalPrice += productMap[product.productId]!!.price * product.quantity
        }

        return Pair(products, totalPrice)
    }
}