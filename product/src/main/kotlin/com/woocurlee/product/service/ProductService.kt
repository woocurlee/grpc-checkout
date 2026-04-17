package com.woocurlee.product.service

import com.woocurlee.product.repository.ProductRepository
import com.woocurlee.proto.product.CheckStockRequest
import com.woocurlee.proto.product.CheckStockResponse
import io.grpc.Status
import io.grpc.StatusException
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository
) {
    fun checkProductStock(req: CheckStockRequest): CheckStockResponse {
        val products = productRepository.findProductsByProductIdIn(req.productsList.map { it.productId })

        val productMap = products.associateBy { it.productId }

        var totalPrice = 0
        req.productsList.forEach { product ->
            if (product.quantity > productMap[product.productId]!!.stock) {
                throw StatusException(Status.FAILED_PRECONDITION)
            }

            totalPrice += productMap[product.productId]!!.price * product.quantity
        }

        return CheckStockResponse.newBuilder()
            .apply {
                this.isAvailable = true
                this.totalPrice = totalPrice
            }.build()
    }
}