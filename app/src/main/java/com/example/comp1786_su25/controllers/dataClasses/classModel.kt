package com.example.comp1786_su25.controllers.dataClasses

data class classModel(
    var id: String = "",
    val day_of_week: String = "",
    val time_of_course: String = "",
    val capacity: String = "",
    val duration: String = "",
    val price_per_class: String = "",
    val type_of_class: String = "",
    val description: String = "",
    val createdTime: String = "",
    var localId: Long = -1, // Added local SQLite database ID
    var synced: Boolean = false, // Flag to track if item is synced with Firebase
    val classes: Map<String, classListModel> = emptyMap()
)

data class classListModel(
    val classes: Map<String, classDetailsModel> = emptyMap(),
    val classType : String = "",
    val classCapacity: String = "",
    val totalClasses: Int = 0,
    val totalPrice: Double = 0.0
)

data class classDetailsModel(
    var id : String = "",
    val class_name: String = "",
    val date : String = "",
    val teacher: String = "",
    val price: String = "",
    val type_of_class: String = "",
    val duration: String = "",
    val capacity: String = "",
    val description: String = "",
    val createdTime: String = "",
    var localId: Long = -1, // Added local SQLite database ID
    var synced: Boolean = false, // Flag to track if item is synced with Firebase
)