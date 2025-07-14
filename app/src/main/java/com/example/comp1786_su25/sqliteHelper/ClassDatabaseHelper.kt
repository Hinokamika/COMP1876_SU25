package com.example.comp1786_su25.sqliteHelper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.comp1786_su25.controllers.dataClasses.classModel
import com.example.comp1786_su25.sqliteHelper.dataAttributes.classAttributes

class ClassDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    classAttributes.DATABASE_NAME,
    null,
    classAttributes.DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE ${classAttributes.TABLE_NAME} (
                ${classAttributes.CLASS_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${classAttributes.FIREBASE_ID} TEXT UNIQUE,
                ${classAttributes.CLASS_NAME} TEXT NOT NULL,
                ${classAttributes.CLASS_DAY_OF_WEEK} TEXT NOT NULL,
                ${classAttributes.CLASS_TIME_OF_COURSE} TEXT NOT NULL,
                ${classAttributes.CLASS_CAPACITY} INTEGER NOT NULL,
                ${classAttributes.CLASS_DURATION} INTEGER NOT NULL,
                ${classAttributes.CLASS_PRICE} REAL NOT NULL,
                ${classAttributes.CLASS_TYPE} TEXT NOT NULL,
                ${classAttributes.CLASS_DESCRIPTION} TEXT,
                ${classAttributes.CLASS_INSTRUCTOR} TEXT NOT NULL,
                ${classAttributes.CLASS_CREATED_AT} TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                ${classAttributes.SYNCED} INTEGER DEFAULT 0
            )
        """.trimIndent()
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            try {
                // Attempt to add the new columns
                db?.execSQL("ALTER TABLE ${classAttributes.TABLE_NAME} ADD COLUMN ${classAttributes.FIREBASE_ID} TEXT DEFAULT NULL")
                db?.execSQL("ALTER TABLE ${classAttributes.TABLE_NAME} ADD COLUMN ${classAttributes.SYNCED} INTEGER DEFAULT 0")

                android.util.Log.d("ClassDatabaseHelper", "Successfully added new columns to the database")
            } catch (e: Exception) {
                // Log the specific error
                android.util.Log.e("ClassDatabaseHelper", "Error upgrading database: ${e.message}")

                // Create a backup of existing data
                db?.execSQL("ALTER TABLE ${classAttributes.TABLE_NAME} RENAME TO temp_${classAttributes.TABLE_NAME}")

                // Create new table with the updated schema
                onCreate(db)

                // Copy data from the old table to the new one
                try {
                    db?.execSQL("""
                        INSERT INTO ${classAttributes.TABLE_NAME} (
                            ${classAttributes.CLASS_ID},
                            ${classAttributes.CLASS_NAME},
                            ${classAttributes.CLASS_DAY_OF_WEEK},
                            ${classAttributes.CLASS_TIME_OF_COURSE},
                            ${classAttributes.CLASS_CAPACITY},
                            ${classAttributes.CLASS_DURATION},
                            ${classAttributes.CLASS_PRICE},
                            ${classAttributes.CLASS_TYPE},
                            ${classAttributes.CLASS_DESCRIPTION},
                            ${classAttributes.CLASS_INSTRUCTOR},
                            ${classAttributes.CLASS_CREATED_AT}
                        )
                        SELECT 
                            ${classAttributes.CLASS_ID},
                            ${classAttributes.CLASS_NAME},
                            ${classAttributes.CLASS_DAY_OF_WEEK},
                            ${classAttributes.CLASS_TIME_OF_COURSE},
                            ${classAttributes.CLASS_CAPACITY},
                            ${classAttributes.CLASS_DURATION},
                            ${classAttributes.CLASS_PRICE},
                            ${classAttributes.CLASS_TYPE},
                            ${classAttributes.CLASS_DESCRIPTION},
                            ${classAttributes.CLASS_INSTRUCTOR},
                            ${classAttributes.CLASS_CREATED_AT}
                        FROM temp_${classAttributes.TABLE_NAME}
                    """)
                    android.util.Log.d("ClassDatabaseHelper", "Successfully migrated existing data to new table schema")

                    // Drop the temporary table
                    db?.execSQL("DROP TABLE temp_${classAttributes.TABLE_NAME}")
                } catch (e: Exception) {
                    android.util.Log.e("ClassDatabaseHelper", "Error migrating data: ${e.message}")
                }
            }
        }
    }

    fun addClass(classModel: classModel): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            // Store the Firebase ID if available
            if (classModel.id.isNotEmpty()) {
                put(classAttributes.FIREBASE_ID, classModel.id)
            }
            put(classAttributes.CLASS_NAME, classModel.class_name)
            put(classAttributes.CLASS_DAY_OF_WEEK, classModel.day_of_week)
            put(classAttributes.CLASS_TIME_OF_COURSE, classModel.time_of_course)
            put(classAttributes.CLASS_CAPACITY, classModel.capacity)
            put(classAttributes.CLASS_DURATION, classModel.duration)
            put(classAttributes.CLASS_PRICE, classModel.price_per_class)
            put(classAttributes.CLASS_TYPE, classModel.type_of_class)
            put(classAttributes.CLASS_DESCRIPTION, classModel.description)
            put(classAttributes.CLASS_INSTRUCTOR, classModel.teacher)
            put(classAttributes.CLASS_CREATED_AT, classModel.createdTime)
            put(classAttributes.SYNCED, if (classModel.synced) 1 else 0)
        }

        // Check if this class already exists by Firebase ID
        if (classModel.id.isNotEmpty()) {
            val existingClass = getClassByFirebaseId(classModel.id)
            if (existingClass != null) {
                // Update instead of insert
                db.update(
                    classAttributes.TABLE_NAME,
                    values,
                    "${classAttributes.FIREBASE_ID} = ?",
                    arrayOf(classModel.id)
                )
                db.close()
                return existingClass.localId // Return existing local ID
            }
        }

        val id = db.insert(classAttributes.TABLE_NAME, null, values)
        db.close()
        return id
    }

    fun getAllClasses(): List<classModel> {
        val classList = mutableListOf<classModel>()
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM ${classAttributes.TABLE_NAME}", null)
        if (cursor.moveToFirst()) {
            do {
                val localId = cursor.getInt(cursor.getColumnIndexOrThrow(classAttributes.CLASS_ID))
                val firebaseId = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.FIREBASE_ID))
                val synced = cursor.getInt(cursor.getColumnIndexOrThrow(classAttributes.SYNCED)) == 1

                val classItem = classModel(
                    id = firebaseId ?: "", // Use Firebase ID as the primary ID
                    class_name = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_NAME)),
                    day_of_week = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_DAY_OF_WEEK)),
                    time_of_course = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_TIME_OF_COURSE)),
                    capacity = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_CAPACITY)),
                    duration = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_DURATION)),
                    price_per_class = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_PRICE)),
                    type_of_class = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_TYPE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_DESCRIPTION)),
                    teacher = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_INSTRUCTOR)),
                    createdTime = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_CREATED_AT)),
                    localId = localId.toLong(),
                    synced = synced
                )
                classList.add(classItem)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return classList
    }

    fun getClassById(classId: String): classModel? {
        val db = readableDatabase
        val cursor = db.query(
            classAttributes.TABLE_NAME,
            null,
            "${classAttributes.CLASS_ID} = ?",
            arrayOf(classId),
            null, null, null
        )
        var classItem: classModel? = null
        if (cursor.moveToFirst()) {
            val localId = cursor.getInt(cursor.getColumnIndexOrThrow(classAttributes.CLASS_ID))
            val firebaseId = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.FIREBASE_ID))
            val synced = cursor.getInt(cursor.getColumnIndexOrThrow(classAttributes.SYNCED)) == 1

            classItem = classModel(
                id = firebaseId ?: "", // Use Firebase ID as the primary ID
                class_name = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_NAME)),
                day_of_week = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_DAY_OF_WEEK)),
                time_of_course = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_TIME_OF_COURSE)),
                capacity = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_CAPACITY)),
                duration = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_DURATION)),
                price_per_class = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_PRICE)),
                type_of_class = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_TYPE)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_DESCRIPTION)),
                teacher = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_INSTRUCTOR)),
                createdTime = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_CREATED_AT)),
                localId = localId.toLong(),
                synced = synced
            )
        }
        cursor.close()
        return classItem
    }

    fun getClassByFirebaseId(firebaseId: String): classModel? {
        val db = readableDatabase
        val cursor = db.query(
            classAttributes.TABLE_NAME,
            null,
            "${classAttributes.FIREBASE_ID} = ?",
            arrayOf(firebaseId),
            null, null, null
        )
        var classItem: classModel? = null
        if (cursor.moveToFirst()) {
            val localId = cursor.getInt(cursor.getColumnIndexOrThrow(classAttributes.CLASS_ID))
            val synced = cursor.getInt(cursor.getColumnIndexOrThrow(classAttributes.SYNCED)) == 1

            classItem = classModel(
                id = firebaseId, // Use the provided Firebase ID
                class_name = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_NAME)),
                day_of_week = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_DAY_OF_WEEK)),
                time_of_course = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_TIME_OF_COURSE)),
                capacity = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_CAPACITY)),
                duration = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_DURATION)),
                price_per_class = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_PRICE)),
                type_of_class = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_TYPE)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_DESCRIPTION)),
                teacher = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_INSTRUCTOR)),
                createdTime = cursor.getString(cursor.getColumnIndexOrThrow(classAttributes.CLASS_CREATED_AT)),
                localId = localId.toLong(),
                synced = synced
            )
        }
        cursor.close()
        return classItem
    }

    fun updateClass(classModel: classModel): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            // Update Firebase ID if available
            if (classModel.id.isNotEmpty()) {
                put(classAttributes.FIREBASE_ID, classModel.id)
            }
            put(classAttributes.CLASS_NAME, classModel.class_name)
            put(classAttributes.CLASS_DAY_OF_WEEK, classModel.day_of_week)
            put(classAttributes.CLASS_TIME_OF_COURSE, classModel.time_of_course)
            put(classAttributes.CLASS_CAPACITY, classModel.capacity)
            put(classAttributes.CLASS_DURATION, classModel.duration)
            put(classAttributes.CLASS_PRICE, classModel.price_per_class)
            put(classAttributes.CLASS_TYPE, classModel.type_of_class)
            put(classAttributes.CLASS_DESCRIPTION, classModel.description)
            put(classAttributes.CLASS_INSTRUCTOR, classModel.teacher)
            put(classAttributes.CLASS_CREATED_AT, classModel.createdTime)
            put(classAttributes.SYNCED, if (classModel.synced) 1 else 0)
        }

        // If we have a local ID, update by local ID
        if (classModel.localId > 0) {
            val rows = db.update(
                classAttributes.TABLE_NAME,
                values,
                "${classAttributes.CLASS_ID} = ?",
                arrayOf(classModel.localId.toString())
            )
            db.close()
            return rows
        }
        // Otherwise, if we have a Firebase ID, update by Firebase ID
        else if (classModel.id.isNotEmpty()) {
            val rows = db.update(
                classAttributes.TABLE_NAME,
                values,
                "${classAttributes.FIREBASE_ID} = ?",
                arrayOf(classModel.id)
            )
            db.close()
            return rows
        }
        return 0
    }

    fun deleteClass(classId: String): Int {
        val db = writableDatabase
        val rows = db.delete(
            classAttributes.TABLE_NAME,
            "${classAttributes.CLASS_ID} = ?",
            arrayOf(classId)
        )
        return rows
    }

    fun deleteClassByFirebaseId(firebaseId: String): Int {
        val db = writableDatabase
        val rows = db.delete(
            classAttributes.TABLE_NAME,
            "${classAttributes.FIREBASE_ID} = ?",
            arrayOf(firebaseId)
        )
        db.close()
        return rows
    }
}
