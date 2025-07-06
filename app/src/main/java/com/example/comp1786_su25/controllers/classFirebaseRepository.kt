package com.example.comp1786_su25.controllers

import com.example.comp1786_su25.dataClasses.classModel
import com.google.firebase.database.FirebaseDatabase

object classFirebaseRepository {
    private val db = FirebaseDatabase.getInstance().getReference("classes")

    fun addClass(classModel: classModel) {
        val classId = db.push().key ?: return
        db.child(classId).setValue(classModel.copy(id = classId))
    }

    fun getClasses(callback: (List<classModel>) -> Unit) {
        db.get().addOnSuccessListener { snapshot ->
            val classes = snapshot.children.mapNotNull { it.getValue(classModel::class.java) }
            callback(classes)
        }.addOnFailureListener {
            callback(emptyList())
        }
    }

    fun getClassById(classId: String, callback: (classModel?) -> Unit) {
        db.child(classId).get().addOnSuccessListener { snapshot ->
            val classData = snapshot.getValue(classModel::class.java)
            callback(classData)
        }.addOnFailureListener {
            callback(null)
        }
    }

    fun getClassesByTeacherId(teacherId: String, callback: (List<classModel>) -> Unit) {
        println("DEBUG: Querying Firebase with teacherId: $teacherId")

        // Using orderByChild requires an index in Firebase rules
        // Temporary workaround: Get all classes and filter client-side
        db.get().addOnSuccessListener { snapshot ->
            val allClasses = snapshot.children.mapNotNull { it.getValue(classModel::class.java) }
            // Filter classes by teacher ID on the client side
            val filteredClasses = allClasses.filter { it.teacher == teacherId }
            println("DEBUG: Firebase returned ${filteredClasses.size} classes after client-side filtering")
            callback(filteredClasses)
        }.addOnFailureListener { error ->
            println("DEBUG: Firebase query failed: ${error.message}")
            callback(emptyList())
        }
    }

    fun updateClass(classModel: classModel) {
        classModel.id?.let { id ->
            db.child(id).setValue(classModel)
        }
    }

    fun deleteClass(classId: String) {
        db.child(classId).removeValue()
    }
}