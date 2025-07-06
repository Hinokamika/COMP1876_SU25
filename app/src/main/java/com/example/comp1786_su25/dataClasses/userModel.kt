package com.example.comp1786_su25.dataClasses

data class userModel(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val age: Int = 0,
    val uid: String = "",
    val createdAt: String = "",
    val carts: Map<String, CartModel> = emptyMap()
)

data class CartModel(
    val timestamp: String = "",
    val total_price: Double = 0.0,
    val total_items: Int = 0,
    val items: Map<String, CartItemModel> = emptyMap()
)

data class CartItemModel(
    val class_id: String = "",
    val class_name: String = "",
    val price: Double = 0.0,
    val teacher: String = "",
    val duration: String = "",
    val type: String = "",
    val quantity: Int = 0
)
