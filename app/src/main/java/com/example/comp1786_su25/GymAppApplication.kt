package com.example.comp1786_su25

import android.app.Application
import com.example.comp1786_su25.sqliteHelper.ClassDatabaseHelper
import com.example.comp1786_su25.sqliteHelper.TeacherDatabaseHelper
import com.example.comp1786_su25.sqliteHelper.UserDatabaseHelper
import com.example.comp1786_su25.sync.SyncManager

class GymAppApplication : Application() {

    // Lazy initialization of database helpers
    val classDatabaseHelper: ClassDatabaseHelper by lazy {
        ClassDatabaseHelper(applicationContext)
    }

    val teacherDatabaseHelper: TeacherDatabaseHelper by lazy {
        TeacherDatabaseHelper(applicationContext)
    }

    val userDatabaseHelper: UserDatabaseHelper by lazy {
        UserDatabaseHelper(applicationContext)
    }

    // Initialize SyncManager
    val syncManager: SyncManager by lazy {
        SyncManager(applicationContext)
    }

    companion object {
        private lateinit var instance: GymAppApplication

        fun getInstance(): GymAppApplication {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
