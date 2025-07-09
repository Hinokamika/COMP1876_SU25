package com.example.comp1786_su25.controllers

import com.example.comp1786_su25.controllers.dataClasses.teacherModel
import com.google.firebase.database.FirebaseDatabase

object teacherFirebaseRepository {

    private val db = FirebaseDatabase.getInstance().getReference("teachers")

    fun addTeacher(teacher: teacherModel) {
        val teacherId = db.push().key ?: return
        db.child(teacherId).setValue(teacher.copy(id = teacherId))
    }

    fun getTeachers(callback: (List<teacherModel>) -> Unit) {
        db.get().addOnSuccessListener { snapshot ->
            val teachers = snapshot.children.mapNotNull { it.getValue(teacherModel::class.java) }
            callback(teachers)
        }.addOnFailureListener {
            callback(emptyList())
        }
    }

    fun getTeacherById(teacherId: String, callback: (teacherModel?) -> Unit) {
        db.child(teacherId).get().addOnSuccessListener { snapshot ->
            val teacherData = snapshot.getValue(teacherModel::class.java)
            callback(teacherData)
        }.addOnFailureListener {
            callback(null)
        }
    }

    fun updateTeacher(teacher: teacherModel) {
        teacher.id?.let { id ->
            db.child(id).setValue(teacher)
        }
    }

    fun deleteTeacher(teacherId: String) {
        db.child(teacherId).removeValue()
    }
}