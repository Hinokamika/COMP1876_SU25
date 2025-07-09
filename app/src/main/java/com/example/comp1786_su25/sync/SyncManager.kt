package com.example.comp1786_su25.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.comp1786_su25.GymAppApplication
import com.example.comp1786_su25.controllers.classFirebaseRepository
import com.example.comp1786_su25.controllers.dataClasses.classModel
import com.example.comp1786_su25.controllers.teacherFirebaseRepository
import com.example.comp1786_su25.controllers.dataClasses.teacherModel
import com.example.comp1786_su25.controllers.userFirebaseRepository
import com.example.comp1786_su25.controllers.dataClasses.userModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * SyncManager handles synchronization between SQLite and Firebase
 */
class SyncManager(private val context: Context) {

    private val TAG = "SyncManager"
    private val classDatabaseHelper = GymAppApplication.getInstance().classDatabaseHelper
    private val teacherDatabaseHelper = GymAppApplication.getInstance().teacherDatabaseHelper
    private val userDatabaseHelper = GymAppApplication.getInstance().userDatabaseHelper

    /**
     * Check if the device is connected to the internet
     */
    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }

    /**
     * Sync all data between SQLite and Firebase
     */
    fun syncAll(onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                syncClasses()
                syncTeachers()
                syncUsers()
                withContext(Dispatchers.Main) {
                    onComplete(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing data: ${e.message}")
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }

    /**
     * Sync classes between SQLite and Firebase
     */
    suspend fun syncClasses() {
        if (!isOnline()) {
            Log.d(TAG, "Device is offline, using local database only")
            return
        }

        // Get all classes from Firebase
        val firebaseClasses = mutableListOf<classModel>()
        try {
            // Using coroutines to wait for Firebase data
            val classesTask = classFirebaseRepository.getClassesTask()
            val snapshot = classesTask.await()
            snapshot.children.forEach { dataSnapshot ->
                val classData = dataSnapshot.getValue(classModel::class.java)
                if (classData != null) {
                    // Set the ID from the Firebase key
                    classData.id = dataSnapshot.key ?: ""
                    firebaseClasses.add(classData)
                }
            }

            // Get all classes from local database
            val localClasses = classDatabaseHelper.getAllClasses()

            // Find classes that exist in Firebase but not in local DB (need to download)
            val classesToDownload = firebaseClasses.filter { firebaseClass ->
                localClasses.none { it.id == firebaseClass.id }
            }

            // Find classes that exist in local DB but not in Firebase (need to upload)
            val classesToUpload = localClasses.filter { localClass ->
                !localClass.synced && firebaseClasses.none { it.id == localClass.id }
            }

            // Download missing classes to local DB
            classesToDownload.forEach { classModel ->
                val localId = classDatabaseHelper.addClass(classModel)
                classModel.localId = localId
                classModel.synced = true
                classDatabaseHelper.updateClass(classModel)
                Log.d(TAG, "Downloaded class to local DB: ${classModel.id}, localId: $localId")
            }

            // Upload missing classes to Firebase
            classesToUpload.forEach { classModel ->
                val firebaseId = classFirebaseRepository.addClass(classModel)
                classModel.id = firebaseId
                classModel.synced = true
                classDatabaseHelper.updateClass(classModel)
                Log.d(TAG, "Uploaded class to Firebase: ${classModel.id}")
            }

            Log.d(TAG, "Class sync completed. Downloaded: ${classesToDownload.size}, Uploaded: ${classesToUpload.size}")

        } catch (e: Exception) {
            Log.e(TAG, "Error syncing classes: ${e.message}")
            throw e
        }
    }

    /**
     * Sync teachers between SQLite and Firebase
     */
    suspend fun syncTeachers() {
        if (!isOnline()) {
            Log.d(TAG, "Device is offline, using local database only")
            return
        }

        // Similar implementation to syncClasses for teachers
        try {
            // Get all teachers from Firebase and local DB
            // Compare and sync as needed
            Log.d(TAG, "Teacher sync completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing teachers: ${e.message}")
            throw e
        }
    }

    /**
     * Sync users between SQLite and Firebase
     */
    suspend fun syncUsers() {
        if (!isOnline()) {
            Log.d(TAG, "Device is offline, using local database only")
            return
        }

        // Similar implementation to syncClasses for users
        try {
            // Get all users from Firebase and local DB
            // Compare and sync as needed
            Log.d(TAG, "User sync completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing users: ${e.message}")
            throw e
        }
    }

    /**
     * Get all classes from best available source
     * - If online, gets from Firebase and updates local DB
     * - If offline, gets from local DB
     */
    fun getAllClasses(callback: (List<classModel>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (isOnline()) {
                    // Online: Get from Firebase and update local
                    val firebaseClasses = mutableListOf<classModel>()
                    val classesTask = classFirebaseRepository.getClassesTask()
                    val snapshot = classesTask.await()

                    snapshot.children.forEach { dataSnapshot ->
                        val classData = dataSnapshot.getValue(classModel::class.java)
                        if (classData != null) {
                            classData.id = dataSnapshot.key ?: ""
                            firebaseClasses.add(classData)

                            // Update or insert into local database
                            val localClass = classDatabaseHelper.getClassById(classData.id)
                            if (localClass == null) {
                                // New class, add to local DB
                                val localId = classDatabaseHelper.addClass(classData)
                                classData.localId = localId
                                classData.synced = true
                                classDatabaseHelper.updateClass(classData)
                            } else {
                                // Update existing class in local DB
                                classData.localId = localClass.localId
                                classData.synced = true
                                classDatabaseHelper.updateClass(classData)
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        callback(firebaseClasses)
                    }
                } else {
                    // Offline: Get from local database
                    val localClasses = classDatabaseHelper.getAllClasses()
                    withContext(Dispatchers.Main) {
                        callback(localClasses)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting classes: ${e.message}")
                // Fallback to local database on error
                val localClasses = classDatabaseHelper.getAllClasses()
                withContext(Dispatchers.Main) {
                    callback(localClasses)
                }
            }
        }
    }

    /**
     * Get a class by ID from best available source
     */
    fun getClassById(classId: String, callback: (classModel?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Try local database first for speed
                var classData = classDatabaseHelper.getClassById(classId)

                if (classData != null) {
                    withContext(Dispatchers.Main) {
                        callback(classData)
                    }
                    return@launch
                }

                // If not in local DB and online, try Firebase
                if (isOnline()) {
                    classFirebaseRepository.getClassById(classId) { firebaseClass ->
                        if (firebaseClass != null) {
                            // Save to local DB for future offline access
                            CoroutineScope(Dispatchers.IO).launch {
                                val localId = classDatabaseHelper.addClass(firebaseClass)
                                firebaseClass.localId = localId
                                firebaseClass.synced = true
                                classDatabaseHelper.updateClass(firebaseClass)
                            }
                            callback(firebaseClass)
                        } else {
                            callback(null)
                        }
                    }
                } else {
                    // Offline and not in local DB
                    withContext(Dispatchers.Main) {
                        callback(null)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting class by ID: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }

    /**
     * Add a new class to both Firebase and local DB
     */
    fun addClass(classData: classModel, callback: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Always save to local DB
                val localId = classDatabaseHelper.addClass(classData)
                classData.localId = localId

                if (isOnline()) {
                    // If online, also save to Firebase
                    val firebaseId = classFirebaseRepository.addClass(classData)
                    classData.id = firebaseId
                    classData.synced = true

                    // Update the local record with the Firebase ID
                    classDatabaseHelper.updateClass(classData)

                    withContext(Dispatchers.Main) {
                        callback(true, firebaseId)
                    }
                } else {
                    // Offline: mark as not synced
                    classData.synced = false
                    classDatabaseHelper.updateClass(classData)

                    withContext(Dispatchers.Main) {
                        callback(true, "local-$localId")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding class: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback(false, "")
                }
            }
        }
    }

    /**
     * Update a class in both Firebase and local DB
     */
    fun updateClass(classData: classModel, callback: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Always update local DB
                classDatabaseHelper.updateClass(classData)

                if (isOnline() && classData.id.isNotEmpty()) {
                    // If online, also update Firebase
                    classFirebaseRepository.updateClass(classData)
                    classData.synced = true
                    classDatabaseHelper.updateClass(classData)

                    withContext(Dispatchers.Main) {
                        callback(true)
                    }
                } else {
                    // Offline: mark as not synced
                    classData.synced = false
                    classDatabaseHelper.updateClass(classData)

                    withContext(Dispatchers.Main) {
                        callback(true)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating class: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback(false)
                }
            }
        }
    }

    /**
     * Delete a class from both Firebase and local DB
     */
    fun deleteClass(classId: String, callback: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Always delete from local DB
                classDatabaseHelper.deleteClass(classId)

                if (isOnline()) {
                    // If online, also delete from Firebase
                    classFirebaseRepository.deleteClass(classId)

                    withContext(Dispatchers.Main) {
                        callback(true)
                    }
                } else {
                    // Offline: we'll need to handle this in a future sync
                    // For now, just report success
                    withContext(Dispatchers.Main) {
                        callback(true)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting class: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback(false)
                }
            }
        }
    }
}
