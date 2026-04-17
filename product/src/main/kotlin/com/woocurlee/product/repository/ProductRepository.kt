package com.woocurlee.product.repository

import com.woocurlee.product.domain.Product
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<Product, Long> {
    fun findProductsByProductIdIn(productIdList: List<Long>): List<Product>
}