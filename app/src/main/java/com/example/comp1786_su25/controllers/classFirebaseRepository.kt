package com.example.comp1786_su25.controllers

import com.example.comp1786_su25.controllers.dataClasses.classModel
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

object classFirebaseRepository {
    private val db = FirebaseDatabase.getInstance().getReference("classes")

    fun addClass(classModel: classModel): String {
        val classId = db.push().key ?: ""
        db.child(classId).setValue(classModel.copy(id = classId))
        return classId
    }

    // Add this method to return a Task that can be awaited in coroutines
    fun getClassesTask(): Task<DataSnapshot> {
        return db.get()
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
        db.get().addOnSuccessListener { snapshot ->
            val allClasses = snapshot.children.mapNotNull { it.getValue(classModel::class.java) }
            val filteredClasses = allClasses.filter { it.teacher == teacherId }
            callback(filteredClasses)
        }.addOnFailureListener { error ->
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