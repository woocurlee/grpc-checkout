package com.woocurlee.user.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Entity(name = "users")
class User(
    @Id @GeneratedValue
    var userId: Long? = null,
    var deliveryAddress: String = "",
    var userName: String = "",
    var status: String = "",
)