package com.woocurlee.product.domain

import com.woocurlee.product.repository.ProductRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class ProductDataInitializer(
    private val productRepository: ProductRepository
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        productRepository.saveAll(
            listOf(
                Product(productName = "상품1", stock = 10, price = 4000),
                Product(productName = "상품2", stock = 5, price = 10000),
                Product(productName = "상품3", stock = 1, price = 500)
            )
        )
    }
}