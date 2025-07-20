package com.example.comp1786_su25.sqliteHelper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.comp1786_su25.controllers.dataClasses.classModel
import com.example.comp1786_su25.controllers.dataClasses.teacherModel
import com.example.comp1786_su25.sqliteHelper.dataAttributes.classAttributes
import com.example.comp1786_su25.sqliteHelper.dataAttributes.teacherAttributes

class TeacherDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    teacherAttributes.DATABASE_NAME,
    null,
    teacherAttributes.DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE ${teacherAttributes.TABLE_NAME} (
                ${teacherAttributes.TEACHER_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${teacherAttributes.FIREBASE_ID} TEXT UNIQUE,
                ${teacherAttributes.TEACHER_NAME} TEXT NOT NULL,
                ${teacherAttributes.TEACHER_EMAIL} TEXT NOT NULL,
                ${teacherAttributes.TEACHER_PHONE} TEXT,
                ${teacherAttributes.TEACHER_SPECIALTY} TEXT,
                ${teacherAttributes.TEACHER_CREATED_AT} TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                ${teacherAttributes.SYNCED} INTEGER DEFAULT 0
            )
        """.trimIndent()
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Add the SYNCED column if upgrading from version 1 to 2
            try {
                db?.execSQL("ALTER TABLE ${teacherAttributes.TABLE_NAME} ADD COLUMN ${teacherAttributes.FIREBASE_ID} TEXT DEFAULT NULL")
                db?.execSQL("ALTER TABLE ${teacherAttributes.TABLE_NAME} ADD COLUMN ${teacherAttributes.SYNCED} INTEGER DEFAULT 0")
            } catch (e: Exception) {
                // If the column already exists or there's another issue, drop and recreate the table
                db?.execSQL("DROP TABLE IF EXISTS ${teacherAttributes.TABLE_NAME}")
                onCreate(db)
            }
        }
    }

    fun addTeacher(teacherModel: teacherModel): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            if (teacherModel.id.isNotEmpty()) {
                put(teacherAttributes.FIREBASE_ID, teacherModel.id)
            }
            put(teacherAttributes.TEACHER_NAME, teacherModel.name)
            put(teacherAttributes.TEACHER_EMAIL, teacherModel.email)
            put(teacherAttributes.TEACHER_PHONE, teacherModel.phone)
            put(teacherAttributes.TEACHER_SPECIALTY, teacherModel.specialization)
            put(teacherAttributes.TEACHER_CREATED_AT, teacherModel.createdAt)
            put(teacherAttributes.SYNCED, if (teacherModel.synced) 1 else 0)
        }

        if (teacherModel.id.isNotEmpty()) {
            val existingTeacher = getTeacherByFirebaseId(teacherModel.id)
            if (existingTeacher != null) {
                // Update instead of insert
                db.update(
                    teacherAttributes.TABLE_NAME,
                    values,
                    "${teacherAttributes.FIREBASE_ID} = ?",
                    arrayOf(teacherModel.id)
                )
                return existingTeacher.localId // Return existing local ID
            }
        }
        val id = db.insert(teacherAttributes.TABLE_NAME, null, values)
        return id
    }

    fun getAllTeachersBySpecialization(specialization: String): List<teacherModel> {
        val teacherList = mutableListOf<teacherModel>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            teacherAttributes.TABLE_NAME,
            null,
            "${teacherAttributes.TEACHER_SPECIALTY} = ?",
            arrayOf(specialization),
            null, null, null
        )
        if (cursor.moveToFirst()) {
            do {
                val localId = cursor.getInt(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_ID))
                val firebaseId = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.FIREBASE_ID))
                val synced = cursor.getInt(cursor.getColumnIndexOrThrow(teacherAttributes.SYNCED)) == 1

                val teacher = teacherModel(
                    id = firebaseId ?: "",
                    name = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_NAME)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_EMAIL)),
                    phone = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_PHONE)),
                    specialization = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_SPECIALTY)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_CREATED_AT)),
                    localId = localId.toLong(),
                    synced = synced
                )
                teacherList.add(teacher)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return teacherList
    }

    fun getAllTeachers(): List<teacherModel> {
        val teacherList = mutableListOf<teacherModel>()
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM ${teacherAttributes.TABLE_NAME}", null)
        if (cursor.moveToFirst()) {
            do {
                val localId = cursor.getInt(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_ID))
                val firebaseId = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.FIREBASE_ID))
                val synced = cursor.getInt(cursor.getColumnIndexOrThrow(teacherAttributes.SYNCED)) == 1

                val teacher = teacherModel(
                    id = firebaseId ?: "",
                    name = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_NAME)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_EMAIL)),
                    phone = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_PHONE)),
                    specialization = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_SPECIALTY)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_CREATED_AT)),
                    localId = localId.toLong(),
                    synced = synced
                )
                teacherList.add(teacher)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return teacherList
    }

    fun getTeacherByFirebaseId(firebaseId: String): teacherModel? {
        val db = readableDatabase
        val cursor = db.query(
            teacherAttributes.TABLE_NAME,
            null,
            "${teacherAttributes.FIREBASE_ID} = ?",
            arrayOf(firebaseId),
            null, null, null
        )
        var teacherItem: teacherModel? = null
        if (cursor.moveToFirst()) {
            val localId = cursor.getInt(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_ID))
            val synced = cursor.getInt(cursor.getColumnIndexOrThrow(teacherAttributes.SYNCED)) == 1

            teacherItem = teacherModel(
                id = firebaseId,
                name = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_EMAIL)),
                phone = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_PHONE)),
                specialization = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_SPECIALTY)),
                createdAt = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_CREATED_AT)),
                localId = localId.toLong(),
                synced = synced
            )
        }
        cursor.close()
        return teacherItem
    }

    fun getTeacherById(teacherId: String): teacherModel? {
        val db = readableDatabase
        val cursor = db.query(
            teacherAttributes.TABLE_NAME,
            null,
            "${teacherAttributes.TEACHER_ID} = ?",
            arrayOf(teacherId),
            null, null, null
        )
        var teacherItem: teacherModel? = null
        if (cursor.moveToFirst()) {
            val localId = cursor.getInt(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_ID))
            val firebaseId = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.FIREBASE_ID))
            val synced = cursor.getInt(cursor.getColumnIndexOrThrow(teacherAttributes.SYNCED)) == 1

            teacherItem = teacherModel(
                id = firebaseId,
                name = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_EMAIL)),
                phone = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_PHONE)),
                specialization = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_SPECIALTY)),
                createdAt = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_CREATED_AT)),
                localId = localId.toLong(),
                synced = synced
            )
        }
        cursor.close()
        return teacherItem
    }

    fun updateTeacher(teacher: teacherModel): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(teacherAttributes.TEACHER_NAME, teacher.name)
            put(teacherAttributes.TEACHER_EMAIL, teacher.email)
            put(teacherAttributes.TEACHER_PHONE, teacher.phone)
            put(teacherAttributes.TEACHER_SPECIALTY, teacher.specialization)
            put(teacherAttributes.TEACHER_CREATED_AT, teacher.createdAt)
            put(teacherAttributes.SYNCED, if (teacher.synced) 1 else 0)
        }
        if (teacher.localId > 0) {
            val rows = db.update(
                teacherAttributes.TABLE_NAME,
                values,
                "${teacherAttributes.TEACHER_ID} = ?",
                arrayOf(teacher.localId.toString())
            )
            db.close()
            return rows
        }
        // Otherwise, if we have a Firebase ID, update by Firebase ID
        else if (teacher.id.isNotEmpty()) {
            val rows = db.update(
                teacherAttributes.TABLE_NAME,
                values,
                "${teacherAttributes.FIREBASE_ID} = ?",
                arrayOf(teacher.id)
            )
            return rows
        }
        return 0
    }

    fun deleteTeacher(teacherId: String): Int {
        val db = writableDatabase
        val rows = db.delete(
            teacherAttributes.TABLE_NAME,
            "${teacherAttributes.TEACHER_ID} = ?",
            arrayOf(teacherId)
        )
        return rows
    }

    fun getTeacherIdAndNameBySpecialization(specialization: String): List<Pair<String, String>> {
        val teacherList = mutableListOf<Pair<String, String>>()
        val db = readableDatabase
        val columns = arrayOf(teacherAttributes.TEACHER_ID, teacherAttributes.TEACHER_NAME)

        val cursor: Cursor = db.query(
            teacherAttributes.TABLE_NAME,
            columns,
            "${teacherAttributes.TEACHER_SPECIALTY} = ?",
            arrayOf(specialization),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_ID)).toString()
                val name = cursor.getString(cursor.getColumnIndexOrThrow(teacherAttributes.TEACHER_NAME))
                teacherList.add(Pair(id, name))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return teacherList
    }
}
