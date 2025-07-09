package com.example.comp1786_su25.controllers.dataClasses

data class classModel(
    var id: String = "",
    val class_name: String = "",
    val day_of_week: String = "",
    val time_of_course: String = "",
    val capacity: String = "",
    val duration: String = "",
    val price_per_class: String = "",
    val type_of_class: String = "",
    val description: String = "",
    val teacher: String = "",
    val createdTime: String = "",
    var localId: Long = -1, // Added local SQLite database ID
    var synced: Boolean = false // Flag to track if item is synced with Firebase
)
