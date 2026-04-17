package com.woocurlee.product.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Entity(name = "products")
class Product(
    @Id @GeneratedValue
    var productId: Long? = null,
    var productName: String = "",
    var stock: Int = 0,
    var price: Int = 0,
)