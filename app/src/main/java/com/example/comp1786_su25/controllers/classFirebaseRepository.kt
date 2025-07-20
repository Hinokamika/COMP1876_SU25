package com.example.comp1786_su25.controllers

import com.example.comp1786_su25.controllers.dataClasses.classModel
import com.example.comp1786_su25.controllers.dataClasses.classDetailsModel
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

object classFirebaseRepository {
    private val db = FirebaseDatabase.getInstance().getReference("classes")
    private val dbCourses = FirebaseDatabase.getInstance().getReference("courses")

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
            val classesList = mutableListOf<classModel>()

            for (classSnapshot in snapshot.children){
                val classId = classSnapshot.key ?: continue
                val className = classSnapshot.child("class_name").getValue(String::class.java) ?: ""
                val dayOfWeek = classSnapshot.child("day_of_week").getValue(String::class.java) ?: ""
                val timeOfCourse = classSnapshot.child("time_of_course").getValue(String::class.java) ?: ""
                val capacity = classSnapshot.child("capacity").getValue(String::class.java) ?: ""
                val duration = classSnapshot.child("duration").getValue(String::class.java) ?: ""
                val pricePerClass = classSnapshot.child("price_per_class").getValue(String::class.java) ?: ""
                val typeOfClass = classSnapshot.child("type_of_class").getValue(String::class.java) ?: ""
                val description = classSnapshot.child("description").getValue(String::class.java) ?: ""

                val coursesMap = mutableMapOf<String, classDetailsModel>()
                val coursesSnapshot = classSnapshot.child("courses")
                for (courseSnapshot in coursesSnapshot.children) {
                    val courseId = courseSnapshot.key ?: continue
                    val courseData = parseCourseSnapshot(courseSnapshot)
                    coursesMap[courseId] = courseData
                }

                val classs = classModel(
                    id = classId,
                    class_name = className,
                    day_of_week = dayOfWeek,
                    time_of_course = timeOfCourse,
                    capacity = capacity,
                    duration = duration,
                    price_per_class = pricePerClass,
                    type_of_class = typeOfClass,
                    description = description,
                    classes = coursesMap
                )

                classesList.add(classs)
            }
            callback(classesList)
        }.addOnFailureListener {
            callback(emptyList())
        }
    }

    private fun parseCourseSnapshot(snapshot: DataSnapshot): classDetailsModel {
        val courseId = snapshot.key ?: ""
        val date = snapshot.child("date").getValue(String::class.java) ?: ""
        val teacher = snapshot.child("teacher").getValue(String::class.java) ?: ""

        return classDetailsModel(
            id = courseId,
            date = date,
            teacher = teacher
        )
    }

    fun getClassById(classId: String, callback: (classModel?) -> Unit) {
        db.child(classId).get().addOnSuccessListener { snapshot ->
            val classData = snapshot.getValue(classModel::class.java)
            callback(classData)
        }.addOnFailureListener {
            callback(null)
        }
    }

    fun getClassesByTeacherId(teacherId: String, callback: (List<classDetailsModel>) -> Unit) {
        dbCourses.get().addOnSuccessListener { snapshot ->
            val allClasses = snapshot.children.mapNotNull { it.getValue(classDetailsModel::class.java) }
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

    fun createCourse(course: classDetailsModel): String {
        val courseId = dbCourses.push().key ?: ""
        dbCourses.child(courseId).setValue(course.copy(id = courseId))
        return courseId
    }

    fun getAllCourses(callback: (List<classDetailsModel>) -> Unit) {
        dbCourses.get().addOnSuccessListener { snapshot ->
            val courses = snapshot.children.mapNotNull { it.getValue(classDetailsModel::class.java) }
            callback(courses)
        }.addOnFailureListener {
            callback(emptyList())
        }
    }

    fun getCoursesTask(): Task<DataSnapshot> {
        return dbCourses.get()
    }

    fun getCourseById(courseId: String, callback: (classDetailsModel?) -> Unit) {
        dbCourses.child(courseId).get().addOnSuccessListener { snapshot ->
            val course = snapshot.getValue(classDetailsModel::class.java)
            callback(course)
        }.addOnFailureListener {
            callback(null)
        }
    }

    fun updateCourse(course: classDetailsModel): Task<Void> {
        return dbCourses.child(course.id).setValue(course)
    }

    fun deleteCourse(courseId: String): Task<Void> {
        return dbCourses.child(courseId).removeValue()
    }

    fun addClassToCourse(classId: String, courseId: String): Task<Void> {
        return dbCourses.child(courseId).child("classes").child(classId).setValue(true)
    }

    fun removeClassFromCourse(classId: String, courseId: String): Task<Void> {
        return dbCourses.child(courseId).child("classes").child(classId).removeValue()
    }

    fun getClassesByCourseId(courseId: String, callback: (List<classModel>) -> Unit) {
        dbCourses.child(courseId).child("classes").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Get class IDs from the course
                val classIds = snapshot.children.mapNotNull { it.key }

                if (classIds.isEmpty()) {
                    callback(emptyList())
                    return@addOnSuccessListener
                }

                // Fetch each class details
                getClasses { allClasses ->
                    val courseClasses = allClasses.filter { it.id in classIds }
                    callback(courseClasses)
                }
            } else {
                callback(emptyList())
            }
        }.addOnFailureListener {
            callback(emptyList())
        }
    }

    fun addClassDetail(classId: String, classDetail: classDetailsModel): Task<Void> {
        val detailId = db.child(classId).child("classes").push().key ?: ""
        return db.child(classId).child("classes").child(detailId).setValue(classDetail.copy(id = detailId))
    }

    fun removeClassDetail(classId: String, detailId: String): Task<Void> {
        return db.child(classId).child("classes").child(detailId).removeValue()
    }

    fun updateClassDetail(classId: String, classDetail: classDetailsModel): Task<Void> {
        return db.child(classId).child("classes").child(classDetail.id).setValue(classDetail)
    }

    fun getClassDetails(classId: String, callback: (Map<String, classDetailsModel>) -> Unit) {
        db.child(classId).child("classes").get().addOnSuccessListener { snapshot ->
            val details = snapshot.children.associate {
                it.key!! to (it.getValue(classDetailsModel::class.java) ?: classDetailsModel())
            }
            callback(details)
        }.addOnFailureListener {
            callback(emptyMap())
        }
    }
}