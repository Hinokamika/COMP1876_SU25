package com.example.comp1786_su25.controllers

import com.example.comp1786_su25.controllers.dataClasses.CartItemModel
import com.example.comp1786_su25.controllers.dataClasses.CartModel
import com.example.comp1786_su25.controllers.dataClasses.userModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object userFirebaseRepository {

    private val db = FirebaseDatabase.getInstance().getReference("users")

    fun addUser(user: userModel) {
        val userId = db.push().key ?: return
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        db.child(userId).setValue(user.copy(id = userId, createdAt = currentTime))
    }

    fun getUsers(callback: (List<userModel>) -> Unit) {
        db.get().addOnSuccessListener { snapshot ->
            val usersList = mutableListOf<userModel>()

            // Process each user
            for (userSnapshot in snapshot.children) {
                val userId = userSnapshot.key ?: continue
                val name = userSnapshot.child("name").getValue(String::class.java) ?: ""
                val email = userSnapshot.child("email").getValue(String::class.java) ?: ""
                val phone = userSnapshot.child("phone").getValue(String::class.java) ?: ""
                val ageValue = userSnapshot.child("age").getValue(Int::class.java) ?: 0
                val uid = userSnapshot.child("uid").getValue(String::class.java) ?: ""
                val createdAt = userSnapshot.child("createdAt").getValue(String::class.java) ?: ""

                // Process carts
                val cartsMap = mutableMapOf<String, CartModel>()
                val cartsSnapshot = userSnapshot.child("carts")
                for (cartSnapshot in cartsSnapshot.children) {
                    val cartId = cartSnapshot.key ?: continue
                    val cartModel = parseCartSnapshot(cartSnapshot)
                    cartsMap[cartId] = cartModel
                }

                val user = userModel(
                    id = userId,
                    name = name,
                    email = email,
                    phone = phone,
                    age = ageValue,
                    uid = uid,
                    createdAt = createdAt,
                    carts = cartsMap
                )

                usersList.add(user)
            }

            callback(usersList)
        }.addOnFailureListener {
            callback(emptyList())
        }
    }

    private fun parseCartSnapshot(cartSnapshot: DataSnapshot): CartModel {
        val timestamp = cartSnapshot.child("timestamp").getValue(String::class.java) ?: ""
        val totalPrice = cartSnapshot.child("total_price").getValue(Double::class.java) ?: 0.0
        val totalItems = cartSnapshot.child("total_items").getValue(Int::class.java) ?: 0

        // Process cart items
        val itemsMap = mutableMapOf<String, CartItemModel>()
        val itemsSnapshot = cartSnapshot.child("items")
        for (itemSnapshot in itemsSnapshot.children) {
            val itemId = itemSnapshot.key ?: continue
            val classId = itemSnapshot.child("class_id").getValue(String::class.java) ?: ""
            val className = itemSnapshot.child("class_name").getValue(String::class.java) ?: ""
            val price = itemSnapshot.child("price").getValue(Double::class.java) ?: 0.0
            val teacher = itemSnapshot.child("teacher").getValue(String::class.java) ?: ""
            val duration = itemSnapshot.child("duration").getValue(String::class.java) ?: ""
            val type = itemSnapshot.child("type").getValue(String::class.java) ?: ""
            val quantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 0

            val cartItem = CartItemModel(
                class_id = classId,
                class_name = className,
                price = price,
                teacher = teacher,
                duration = duration,
                type = type,
                quantity = quantity
            )

            itemsMap[itemId] = cartItem
        }

        return CartModel(
            timestamp = timestamp,
            total_price = totalPrice,
            total_items = totalItems,
            items = itemsMap
        )
    }

    fun getUserById(userId: String, callback: (userModel?) -> Unit) {
        db.child(userId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                val email = snapshot.child("email").getValue(String::class.java) ?: ""
                val phone = snapshot.child("phone").getValue(String::class.java) ?: ""
                val ageValue = snapshot.child("age").getValue(Int::class.java) ?: 0
                val uid = snapshot.child("uid").getValue(String::class.java) ?: ""
                val createdAt = snapshot.child("createdAt").getValue(String::class.java) ?: ""

                // Process carts
                val cartsMap = mutableMapOf<String, CartModel>()
                val cartsSnapshot = snapshot.child("carts")
                for (cartSnapshot in cartsSnapshot.children) {
                    val cartId = cartSnapshot.key ?: continue
                    val cartModel = parseCartSnapshot(cartSnapshot)
                    cartsMap[cartId] = cartModel
                }

                val user = userModel(
                    id = userId,
                    name = name,
                    email = email,
                    phone = phone,
                    age = ageValue,
                    uid = uid,
                    createdAt = createdAt,
                    carts = cartsMap
                )

                callback(user)
            } else {
                callback(null)
            }
        }.addOnFailureListener {
            callback(null)
        }
    }

    fun updateUser(user: userModel) {
        user.id.let { id ->
            if (id.isNotEmpty()) {
                db.child(id).setValue(user)
            }
        }
    }

    fun deleteUser(userId: String) {
        db.child(userId).removeValue()
    }
}