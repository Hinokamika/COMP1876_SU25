package com.example.comp1786_su25.controllers.dataClasses

data class teacherModel(
    var id : String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val age : String = "",
    val specialization: String = "",
//    val classList: List<String> = emptyList(),
    val createdAt: String = "",
    var localId: Long = -1, // Added local SQLite database ID
    var synced: Boolean = false // Flag to track if item is synced with Firebase
)
