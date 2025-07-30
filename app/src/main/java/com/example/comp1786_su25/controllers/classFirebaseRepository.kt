package com.example.comp1786_su25.controllers

import com.example.comp1786_su25.controllers.dataClasses.classModel
import com.example.comp1786_su25.controllers.dataClasses.classDetailsModel
import com.example.comp1786_su25.controllers.dataClasses.classListModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

object classFirebaseRepository {
    private val db = FirebaseDatabase.getInstance().getReference("classes")
    private val dbCourses = FirebaseDatabase.getInstance().getReference("courses")
    // Remove the separate classDetails reference since we're storing them nested

    // Class Model CRUD Operations (Parent)
    fun addClass(classModel: classModel): String {
        val classId = db.push().key ?: ""
        db.child(classId).setValue(classModel.copy(id = classId))
        return classId
    }

    fun getClassesTask(): Task<DataSnapshot> {
        return db.get()
    }

    fun getClasses(callback: (List<classModel>) -> Unit) {
        db.get().addOnSuccessListener { snapshot ->
            val classesList = mutableListOf<classModel>()

            for (classSnapshot in snapshot.children){
                val classId = classSnapshot.key ?: continue
                val dayOfWeek = classSnapshot.child("day_of_week").getValue(String::class.java) ?: ""
                val timeOfCourse = classSnapshot.child("time_of_course").getValue(String::class.java) ?: ""
                val capacity = classSnapshot.child("capacity").getValue(String::class.java) ?: ""
                val duration = classSnapshot.child("duration").getValue(String::class.java) ?: ""
                val pricePerClass = classSnapshot.child("price_per_class").getValue(String::class.java) ?: ""
                val typeOfClass = classSnapshot.child("type_of_class").getValue(String::class.java) ?: ""
                val description = classSnapshot.child("description").getValue(String::class.java) ?: ""
                val createdTime = classSnapshot.child("createdTime").getValue(String::class.java) ?: ""

                val coursesMap = mutableMapOf<String, classListModel>()
                val coursesSnapshot = classSnapshot.child("courses")
                for (courseSnapshot in coursesSnapshot.children) {
                    val courseId = courseSnapshot.key ?: continue
                    val courseData = parseCourseSnapshot(courseSnapshot)
                    coursesMap[courseId] = courseData
                }

                val classs = classModel(
                    id = classId,
                    day_of_week = dayOfWeek,
                    time_of_course = timeOfCourse,
                    capacity = capacity,
                    duration = duration,
                    price_per_class = pricePerClass,
                    type_of_class = typeOfClass,
                    description = description,
                    createdTime = createdTime,
                    classes = coursesMap
                )

                classesList.add(classs)
            }
            callback(classesList)
        }.addOnFailureListener {
            callback(emptyList())
        }
    }

    fun getClassById(classId: String, callback: (classModel?) -> Unit) {
        db.child(classId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val dayOfWeek = snapshot.child("day_of_week").getValue(String::class.java) ?: ""
                val timeOfCourse = snapshot.child("time_of_course").getValue(String::class.java) ?: ""
                val capacity = snapshot.child("capacity").getValue(String::class.java) ?: ""
                val duration = snapshot.child("duration").getValue(String::class.java) ?: ""
                val pricePerClass = snapshot.child("price_per_class").getValue(String::class.java) ?: ""
                val typeOfClass = snapshot.child("type_of_class").getValue(String::class.java) ?: ""
                val description = snapshot.child("description").getValue(String::class.java) ?: ""
                val createdTime = snapshot.child("createdTime").getValue(String::class.java) ?: ""

                val coursesMap = mutableMapOf<String, classListModel>()
                val coursesSnapshot = snapshot.child("courses")
                for (courseSnapshot in coursesSnapshot.children) {
                    val courseId = courseSnapshot.key ?: continue
                    val courseData = parseCourseSnapshot(courseSnapshot)
                    coursesMap[courseId] = courseData
                }

                val classData = classModel(
                    id = classId,
                    day_of_week = dayOfWeek,
                    time_of_course = timeOfCourse,
                    capacity = capacity,
                    duration = duration,
                    price_per_class = pricePerClass,
                    type_of_class = typeOfClass,
                    description = description,
                    createdTime = createdTime,
                    classes = coursesMap
                )
                callback(classData)
            } else {
                callback(null)
            }
        }.addOnFailureListener {
            callback(null)
        }
    }

    fun updateClass(classModel: classModel): Task<Void> {
        return classModel.id.let { id ->
            db.child(id).setValue(classModel)
        }
    }

    fun deleteClass(classId: String): Task<Void> {
        // Simply delete the entire class node - this will cascade delete all nested data
        return db.child(classId).removeValue()
    }

    // Course Model CRUD Operations (classListModel)
    fun addCourse(classId: String, course: classListModel): Task<Void> {
        // First check if adding this course would exceed the class capacity
        return db.child(classId).get().continueWithTask { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                val classCapacity = snapshot.child("capacity").getValue(String::class.java)?.toIntOrNull() ?: 0

                // Get current courses to calculate total used capacity
                val coursesSnapshot = snapshot.child("courses")
                var totalUsedCapacity = 0
                for (courseSnapshot in coursesSnapshot.children) {
                    val capacity = courseSnapshot.child("classCapacity").getValue(String::class.java)?.toIntOrNull() ?: 0
                    totalUsedCapacity += capacity
                }

                // Check if adding this course would exceed capacity
                val newCourseCapacity = course.classCapacity.toIntOrNull() ?: 0
                if (totalUsedCapacity + newCourseCapacity > classCapacity) {
                    throw Exception("Adding this course would exceed the class capacity limit of $classCapacity")
                }

                // If within capacity, proceed with adding the course
                val courseId = db.child(classId).child("courses").push().key ?: ""
                return@continueWithTask db.child(classId).child("courses").child(courseId).setValue(course)
            } else {
                throw task.exception ?: Exception("Failed to retrieve class data")
            }
        }
    }

    fun getCoursesByClassId(classId: String, callback: (List<classListModel>) -> Unit) {
        db.child(classId).child("courses").get().addOnSuccessListener { snapshot ->
            val courses = mutableListOf<classListModel>()

            for (courseSnapshot in snapshot.children) {
                val courseId = courseSnapshot.key ?: continue
                val course = parseCourseSnapshot(courseSnapshot)
                courses.add(course)
            }

            callback(courses)
        }.addOnFailureListener {
            callback(emptyList())
        }
    }

    fun getCourseById(classId: String, courseId: String, callback: (classListModel?) -> Unit) {
        db.child(classId).child("courses").child(courseId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val course = parseCourseSnapshot(snapshot)
                callback(course)
            } else {
                callback(null)
            }
        }.addOnFailureListener {
            callback(null)
        }
    }

    fun updateCourse(classId: String, courseId: String, course: classListModel): Task<Void> {
        // Check if updating this course would exceed class capacity
        return db.child(classId).get().continueWithTask { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                val classCapacity = snapshot.child("capacity").getValue(String::class.java)?.toIntOrNull() ?: 0

                // Get current courses to calculate total used capacity
                val coursesSnapshot = snapshot.child("courses")
                var totalUsedCapacity = 0
                for (courseSnap in coursesSnapshot.children) {
                    // Skip the current course being updated when calculating total
                    if (courseSnap.key == courseId) continue

                    val capacity = courseSnap.child("classCapacity").getValue(String::class.java)?.toIntOrNull() ?: 0
                    totalUsedCapacity += capacity
                }

                // Check if updating this course would exceed capacity
                val newCourseCapacity = course.classCapacity.toIntOrNull() ?: 0
                if (totalUsedCapacity + newCourseCapacity > classCapacity) {
                    throw Exception("Updating this course would exceed the class capacity limit of $classCapacity")
                }

                // If within capacity, proceed with updating the course
                return@continueWithTask db.child(classId).child("courses").child(courseId).setValue(course)
            } else {
                throw task.exception ?: Exception("Failed to retrieve class data")
            }
        }
    }

    fun deleteCourse(classId: String, courseId: String): Task<Void> {
        // Simply delete the course node - this will cascade delete all nested classDetails
        return db.child(classId).child("courses").child(courseId).removeValue()
    }

    // Class Details CRUD Operations (classDetailsModel)
    fun addClassDetail(classId: String, courseId: String, classDetail: classDetailsModel): Task<Void> {
        val detailId = db.child(classId).child("courses").child(courseId).child("classDetails").push().key ?: ""
        val detailWithId = classDetail.copy(id = detailId)

        // Add the class detail directly to the course's classDetails collection
        return db.child(classId).child("courses").child(courseId)
            .child("classDetails").child(detailId).setValue(detailWithId).continueWithTask { task ->
                if (task.isSuccessful) {
                    // Update course totals after adding
                    return@continueWithTask updateCourseTotals(classId, courseId)
                } else {
                    throw task.exception ?: Exception("Failed to add class detail")
                }
            }
    }

    private fun updateCourseTotals(classId: String, courseId: String): Task<Void> {
        // Get all class details in the course
        return getClassDetailsByCourseIdTask(classId, courseId).continueWithTask { task ->
            if (task.isSuccessful) {
                val classDetails = task.result

                // Calculate totals
                val totalClasses = classDetails.size
                val totalPrice = classDetails.sumOf { it.price.toDoubleOrNull() ?: 0.0 }
                val classType = if (classDetails.isNotEmpty()) classDetails[0].type_of_class else ""

                // Update the course with new totals
                val updates = HashMap<String, Any>()
                updates["totalClasses"] = totalClasses
                updates["totalPrice"] = totalPrice
                updates["classType"] = classType

                return@continueWithTask db.child(classId).child("courses").child(courseId).updateChildren(updates)
            } else {
                // If there was an error, just return a completed task
                return@continueWithTask Tasks.forResult<Void>(null)
            }
        }
    }

    private fun getClassDetailsByCourseIdTask(classId: String, courseId: String): Task<List<classDetailsModel>> {
        // Create a task source that will be completed when all class details are fetched
        val taskCompletionSource = TaskCompletionSource<List<classDetailsModel>>()

        // Get class details directly from the course's classDetails collection
        db.child(classId).child("courses").child(courseId).child("classDetails").get().addOnSuccessListener { snapshot ->
            val classDetails = mutableListOf<classDetailsModel>()

            for (detailSnapshot in snapshot.children) {
                val detailId = detailSnapshot.key ?: continue
                try {
                    // Manual parsing instead of automatic deserialization
                    val classDetail = classDetailsModel(
                        id = detailId,
                        class_name = detailSnapshot.child("class_name").getValue(String::class.java) ?: "",
                        date = detailSnapshot.child("date").getValue(String::class.java) ?: "",
                        teacher = detailSnapshot.child("teacher").getValue(String::class.java) ?: "",
                        price = detailSnapshot.child("price").getValue(String::class.java) ?: "",
                        type_of_class = detailSnapshot.child("type_of_class").getValue(String::class.java) ?: "",
                        duration = detailSnapshot.child("duration").getValue(String::class.java) ?: "",
                        capacity = detailSnapshot.child("capacity").getValue(String::class.java) ?: "",
                        description = detailSnapshot.child("description").getValue(String::class.java) ?: "",
                        createdTime = detailSnapshot.child("createdTime").getValue(String::class.java) ?: "",
                        synced = detailSnapshot.child("synced").getValue(Boolean::class.java) ?: true
                    )
                    classDetails.add(classDetail)
                } catch (e: Exception) {
                    println("Error parsing class detail in getClassDetailsByCourseIdTask: ${e.message}")
                    // Skip this detail if parsing fails
                }
            }

            taskCompletionSource.setResult(classDetails)
        }.addOnFailureListener {
            taskCompletionSource.setException(it)
        }

        return taskCompletionSource.task
    }

    // Helper methods
    private fun parseCourseSnapshot(snapshot: DataSnapshot): classListModel {
        val totalClasses = snapshot.child("totalClasses").getValue(Int::class.java) ?: 0
        val totalPrice = snapshot.child("totalPrice").getValue(Double::class.java) ?: 0.0
        val classType = snapshot.child("classType").getValue(String::class.java) ?: ""
        val classCapacity = snapshot.child("classCapacity").getValue(String::class.java) ?: ""

        val classesMap = mutableMapOf<String, classDetailsModel>()
        val classesSnapshot = snapshot.child("classDetails")

        // Parse each class detail manually to avoid Firebase type conversion errors
        for (detailSnapshot in classesSnapshot.children) {
            val detailId = detailSnapshot.key ?: continue
            try {
                val classDetail = classDetailsModel(
                    id = detailId,
                    class_name = detailSnapshot.child("class_name").getValue(String::class.java) ?: "",
                    date = detailSnapshot.child("date").getValue(String::class.java) ?: "",
                    teacher = detailSnapshot.child("teacher").getValue(String::class.java) ?: "",
                    price = detailSnapshot.child("price").getValue(String::class.java) ?: "",
                    type_of_class = detailSnapshot.child("type_of_class").getValue(String::class.java) ?: "",
                    duration = detailSnapshot.child("duration").getValue(String::class.java) ?: "",
                    capacity = detailSnapshot.child("capacity").getValue(String::class.java) ?: "",
                    description = detailSnapshot.child("description").getValue(String::class.java) ?: "",
                    createdTime = detailSnapshot.child("createdTime").getValue(String::class.java) ?: "",
                    synced = detailSnapshot.child("synced").getValue(Boolean::class.java) ?: true
                )
                classesMap[detailId] = classDetail
            } catch (e: Exception) {
                println("Error parsing class detail in course: ${e.message}")
                // Skip this detail if parsing fails
            }
        }

        return classListModel(
            classes = classesMap,
            classType = classType,
            classCapacity = classCapacity,
            totalClasses = totalClasses,
            totalPrice = totalPrice
        )
    }

    // Sync helper methods
    suspend fun syncClassWithFirebase(classModel: classModel): String {
        return if (classModel.id.isNotEmpty()) {
            // Update existing class
            updateClass(classModel).await()
            classModel.id
        } else {
            // Add new class
            addClass(classModel)
        }
    }

    suspend fun syncCourseWithFirebase(classId: String, course: classListModel, courseId: String? = null): String {
        return if (courseId != null && courseId.isNotEmpty()) {
            // Update existing course
            updateCourse(classId, courseId, course).await()
            courseId
        } else {
            // Add new course
            val task = addCourse(classId, course)
            task.await()
            // Return the new course ID (this is a simplification - you may need to adjust)
            courseId ?: ""
        }
    }

    suspend fun syncClassDetailWithFirebase(classId: String, courseId: String, classDetail: classDetailsModel): String {
        return if (classDetail.id.isNotEmpty()) {
            // Update existing class detail
            updateClassDetail(classId, courseId, classDetail).await()
            classDetail.id
        } else {
            // Add new class detail
            val task = addClassDetail(classId, courseId, classDetail)
            task.await()
            classDetail.id
        }
    }

    // Updated updateClassDetail method to work with nested structure
    fun updateClassDetail(classId: String, courseId: String, classDetail: classDetailsModel): Task<Void> {
        // Update the class detail in the nested structure
        return db.child(classId).child("courses").child(courseId)
            .child("classDetails").child(classDetail.id).setValue(classDetail).continueWithTask { task ->
                if (task.isSuccessful) {
                    // Update course totals after updating
                    return@continueWithTask updateCourseTotals(classId, courseId)
                } else {
                    throw task.exception ?: Exception("Failed to update class detail")
                }
            }
    }

    fun getClassDetailById(classId: String, courseId: String, detailId: String, callback: (classDetailsModel?) -> Unit) {
        db.child(classId).child("courses").child(courseId).child("classDetails").child(detailId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                try {
                    // Manual parsing instead of automatic deserialization
                    val classDetail = classDetailsModel(
                        id = detailId,
                        class_name = snapshot.child("class_name").getValue(String::class.java) ?: "",
                        date = snapshot.child("date").getValue(String::class.java) ?: "",
                        teacher = snapshot.child("teacher").getValue(String::class.java) ?: "",
                        price = snapshot.child("price").getValue(String::class.java) ?: "",
                        type_of_class = snapshot.child("type_of_class").getValue(String::class.java) ?: "",
                        duration = snapshot.child("duration").getValue(String::class.java) ?: "",
                        capacity = snapshot.child("capacity").getValue(String::class.java) ?: "",
                        description = snapshot.child("description").getValue(String::class.java) ?: "",
                        createdTime = snapshot.child("createdTime").getValue(String::class.java) ?: "",
                        synced = snapshot.child("synced").getValue(Boolean::class.java) ?: true
                    )
                    callback(classDetail)
                } catch (e: Exception) {
                    println("Error parsing class detail: ${e.message}")
                    callback(null)
                }
            } else {
                callback(null)
            }
        }.addOnFailureListener { exception ->
            println("Error getting class detail: ${exception.message}")
            callback(null)
        }
    }

    fun deleteClassDetail(classId: String, courseId: String, detailId: String): Task<Void> {
        // Remove the class detail from the nested structure
        return db.child(classId).child("courses").child(courseId)
            .child("classDetails").child(detailId).removeValue().continueWithTask { task ->
                if (task.isSuccessful) {
                    // Update course totals after removing the detail
                    return@continueWithTask updateCourseTotals(classId, courseId)
                } else {
                    throw task.exception ?: Exception("Failed to delete class detail")
                }
            }
    }

    fun getClassDetailsForCourse(classId: String, courseId: String, callback: (List<classDetailsModel>) -> Unit) {
        // Get class details directly from the course's classDetails collection
        db.child(classId).child("courses").child(courseId).child("classDetails").get().addOnSuccessListener { snapshot ->
            val classDetails = mutableListOf<classDetailsModel>()

            for (detailSnapshot in snapshot.children) {
                val detailId = detailSnapshot.key ?: continue
                try {
                    // Manual parsing instead of automatic deserialization
                    val classDetail = classDetailsModel(
                        id = detailId,
                        class_name = detailSnapshot.child("class_name").getValue(String::class.java) ?: "",
                        date = detailSnapshot.child("date").getValue(String::class.java) ?: "",
                        teacher = detailSnapshot.child("teacher").getValue(String::class.java) ?: "",
                        price = detailSnapshot.child("price").getValue(String::class.java) ?: "",
                        type_of_class = detailSnapshot.child("type_of_class").getValue(String::class.java) ?: "",
                        duration = detailSnapshot.child("duration").getValue(String::class.java) ?: "",
                        capacity = detailSnapshot.child("capacity").getValue(String::class.java) ?: "",
                        description = detailSnapshot.child("description").getValue(String::class.java) ?: "",
                        createdTime = detailSnapshot.child("createdTime").getValue(String::class.java) ?: "",
                        synced = detailSnapshot.child("synced").getValue(Boolean::class.java) ?: true
                    )
                    classDetails.add(classDetail)
                } catch (e: Exception) {
                    println("Error parsing class detail in getClassDetailsForCourse: ${e.message}")
                    // Skip this detail if parsing fails
                }
            }

            callback(classDetails.sortedBy { it.date }) // Sort by date for better UX
        }.addOnFailureListener {
            callback(emptyList())
        }
    }

    // Enhanced method to get classes with full class details populated
    fun getClassesWithDetails(callback: (List<classModel>) -> Unit) {
        db.get().addOnSuccessListener { snapshot ->
            val classesList = mutableListOf<classModel>()
            val totalClasses = snapshot.children.count()
            var processedClasses = 0

            if (totalClasses == 0) {
                callback(emptyList())
                return@addOnSuccessListener
            }

            for (classSnapshot in snapshot.children) {
                val classId = classSnapshot.key ?: continue
                val dayOfWeek = classSnapshot.child("day_of_week").getValue(String::class.java) ?: ""
                val timeOfCourse = classSnapshot.child("time_of_course").getValue(String::class.java) ?: ""
                val capacity = classSnapshot.child("capacity").getValue(String::class.java) ?: ""
                val duration = classSnapshot.child("duration").getValue(String::class.java) ?: ""
                val pricePerClass = classSnapshot.child("price_per_class").getValue(String::class.java) ?: ""
                val typeOfClass = classSnapshot.child("type_of_class").getValue(String::class.java) ?: ""
                val description = classSnapshot.child("description").getValue(String::class.java) ?: ""
                val createdTime = classSnapshot.child("createdTime").getValue(String::class.java) ?: ""

                val coursesMap = mutableMapOf<String, classListModel>()
                val coursesSnapshot = classSnapshot.child("courses")
                val totalCourses = coursesSnapshot.children.count()

                if (totalCourses == 0) {
                    // No courses for this class
                    val classModel = classModel(
                        id = classId,
                        day_of_week = dayOfWeek,
                        time_of_course = timeOfCourse,
                        capacity = capacity,
                        duration = duration,
                        price_per_class = pricePerClass,
                        type_of_class = typeOfClass,
                        description = description,
                        createdTime = createdTime,
                        classes = coursesMap
                    )
                    classesList.add(classModel)

                    processedClasses++
                    if (processedClasses == totalClasses) {
                        callback(classesList)
                    }
                    continue
                }

                var processedCourses = 0

                for (courseSnapshot in coursesSnapshot.children) {
                    val courseId = courseSnapshot.key ?: continue

                    // Parse course data directly from snapshot using the helper method
                    val courseData = parseCourseSnapshot(courseSnapshot)
                    coursesMap[courseId] = courseData

                    processedCourses++
                    if (processedCourses == totalCourses) {
                        // All courses processed for this class
                        val classModel = classModel(
                            id = classId,
                            day_of_week = dayOfWeek,
                            time_of_course = timeOfCourse,
                            capacity = capacity,
                            duration = duration,
                            price_per_class = pricePerClass,
                            type_of_class = typeOfClass,
                            description = description,
                            createdTime = createdTime,
                            classes = coursesMap
                        )
                        classesList.add(classModel)

                        processedClasses++
                        if (processedClasses == totalClasses) {
                            callback(classesList)
                        }
                    }
                }
            }
        }.addOnFailureListener {
            callback(emptyList())
        }
    }

    // Migration function to clean up existing duplicate classDetails
    fun cleanupDuplicateClassDetails(): Task<Void> {
        return db.get().continueWithTask { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                val updates = mutableMapOf<String, Any?>()

                // For each class, remove the root-level classDetails if it exists
                for (classSnapshot in snapshot.children) {
                    val classId = classSnapshot.key ?: continue
                    if (classSnapshot.hasChild("classDetails")) {
                        // Mark root-level classDetails for deletion
                        updates["$classId/classDetails"] = null
                    }
                }

                // Apply all updates in a batch
                return@continueWithTask if (updates.isNotEmpty()) {
                    db.updateChildren(updates)
                } else {
                    Tasks.forResult<Void>(null)
                }
            } else {
                throw task.exception ?: Exception("Failed to retrieve classes for cleanup")
            }
        }
    }

    // Function to fetch all courses data from Firebase Realtime Database
    fun fetchAllCoursesFromDatabase(callback: (List<Pair<String, classListModel>>) -> Unit) {
        db.get().addOnSuccessListener { snapshot ->
            val allCourses = mutableListOf<Pair<String, classListModel>>()

            for (classSnapshot in snapshot.children) {
                val classId = classSnapshot.key ?: continue
                val coursesSnapshot = classSnapshot.child("courses")

                for (courseSnapshot in coursesSnapshot.children) {
                    val courseId = courseSnapshot.key ?: continue
                    try {
                        val course = parseCourseSnapshot(courseSnapshot)
                        // Store as Pair of (classId, course) to maintain relationship
                        allCourses.add(Pair(classId, course))
                    } catch (e: Exception) {
                        println("Error parsing course $courseId in class $classId: ${e.message}")
                    }
                }
            }

            callback(allCourses)
        }.addOnFailureListener { exception ->
            println("Error fetching courses from database: ${exception.message}")
            callback(emptyList())
        }
    }

    // Function to fetch courses for a specific class in real-time
    fun fetchCoursesForClassRealtime(classId: String, callback: (List<classListModel>) -> Unit) {
        val coursesRef = db.child(classId).child("courses")

        coursesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val courses = mutableListOf<classListModel>()

                for (courseSnapshot in snapshot.children) {
                    val courseId = courseSnapshot.key ?: continue
                    try {
                        val course = parseCourseSnapshot(courseSnapshot)
                        courses.add(course)
                    } catch (e: Exception) {
                        println("Error parsing course $courseId: ${e.message}")
                    }
                }

                callback(courses)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error fetching courses in real-time: ${error.message}")
                callback(emptyList())
            }
        })
    }

    // Function to fetch all courses from all classes in real-time
    fun fetchAllCoursesRealtime(callback: (Map<String, List<classListModel>>) -> Unit) {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val classesWithCourses = mutableMapOf<String, List<classListModel>>()

                for (classSnapshot in snapshot.children) {
                    val classId = classSnapshot.key ?: continue
                    val courses = mutableListOf<classListModel>()
                    val coursesSnapshot = classSnapshot.child("courses")

                    for (courseSnapshot in coursesSnapshot.children) {
                        val courseId = courseSnapshot.key ?: continue
                        try {
                            val course = parseCourseSnapshot(courseSnapshot)
                            courses.add(course)
                        } catch (e: Exception) {
                            println("Error parsing course $courseId in class $classId: ${e.message}")
                        }
                    }

                    classesWithCourses[classId] = courses
                }

                callback(classesWithCourses)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error fetching all courses in real-time: ${error.message}")
                callback(emptyMap())
            }
        })
    }

    // Function to fetch courses with their class details
    fun fetchCoursesWithClassInfo(callback: (List<Triple<String, classModel, classListModel>>) -> Unit) {
        db.get().addOnSuccessListener { snapshot ->
            val coursesWithClassInfo = mutableListOf<Triple<String, classModel, classListModel>>()

            for (classSnapshot in snapshot.children) {
                val classId = classSnapshot.key ?: continue

                // Parse class information
                val dayOfWeek = classSnapshot.child("day_of_week").getValue(String::class.java) ?: ""
                val timeOfCourse = classSnapshot.child("time_of_course").getValue(String::class.java) ?: ""
                val capacity = classSnapshot.child("capacity").getValue(String::class.java) ?: ""
                val duration = classSnapshot.child("duration").getValue(String::class.java) ?: ""
                val pricePerClass = classSnapshot.child("price_per_class").getValue(String::class.java) ?: ""
                val typeOfClass = classSnapshot.child("type_of_class").getValue(String::class.java) ?: ""
                val description = classSnapshot.child("description").getValue(String::class.java) ?: ""
                val createdTime = classSnapshot.child("createdTime").getValue(String::class.java) ?: ""

                val classModel = classModel(
                    id = classId,
                    day_of_week = dayOfWeek,
                    time_of_course = timeOfCourse,
                    capacity = capacity,
                    duration = duration,
                    price_per_class = pricePerClass,
                    type_of_class = typeOfClass,
                    description = description,
                    createdTime = createdTime
                )

                // Parse courses for this class
                val coursesSnapshot = classSnapshot.child("courses")
                for (courseSnapshot in coursesSnapshot.children) {
                    val courseId = courseSnapshot.key ?: continue
                    try {
                        val course = parseCourseSnapshot(courseSnapshot)
                        // Store as Triple of (courseId, classModel, course)
                        coursesWithClassInfo.add(Triple(courseId, classModel, course))
                    } catch (e: Exception) {
                        println("Error parsing course $courseId in class $classId: ${e.message}")
                    }
                }
            }

            callback(coursesWithClassInfo)
        }.addOnFailureListener { exception ->
            println("Error fetching courses with class info: ${exception.message}")
            callback(emptyList())
        }
    }

    // Function to search courses by criteria
    fun searchCourses(
        classType: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        callback: (List<Pair<String, classListModel>>) -> Unit
    ) {
        fetchAllCoursesFromDatabase { allCourses ->
            val filteredCourses = allCourses.filter { (classId, course) ->
                var matches = true

                if (classType != null && !course.classType.contains(classType, ignoreCase = true)) {
                    matches = false
                }

                if (minPrice != null && course.totalPrice < minPrice) {
                    matches = false
                }

                if (maxPrice != null && course.totalPrice > maxPrice) {
                    matches = false
                }

                matches
            }

            callback(filteredCourses)
        }
    }
}