package com.woocurlee.product.grpc

import com.woocurlee.product.service.ProductService
import com.woocurlee.proto.product.CheckStockRequest
import com.woocurlee.proto.product.CheckStockResponse
import com.woocurlee.proto.product.DeductStockRequest
import com.woocurlee.proto.product.DeductStockResponse
import com.woocurlee.proto.product.ProductServiceGrpcKt
import org.springframework.grpc.server.service.GrpcService

@GrpcService
class ProductGrpcService(private val productService: ProductService) :
    ProductServiceGrpcKt.ProductServiceCoroutineImplBase() {
    override suspend fun checkStock(request: CheckStockRequest): CheckStockResponse {
        return productService.checkProductStock(request)
    }

    override suspend fun deductStock(request: DeductStockRequest): DeductStockResponse {
        return productService.deductProductStock(request)
    }
}