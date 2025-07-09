package com.example.comp1786_su25.sqliteHelper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.comp1786_su25.controllers.dataClasses.userModel
import com.example.comp1786_su25.sqliteHelper.dataAttributes.userAttributes

class UserDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    userAttributes.DATABASE_NAME,
    null,
    userAttributes.DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE ${userAttributes.TABLE_NAME} (
                ${userAttributes.USER_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${userAttributes.USER_NAME} TEXT NOT NULL,
                ${userAttributes.USER_EMAIL} TEXT NOT NULL,
                ${userAttributes.USER_PASSWORD} TEXT NOT NULL,
                ${userAttributes.USER_CREATED_AT} TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS ${userAttributes.TABLE_NAME}")
        onCreate(db)
    }

    fun addUser(user: userModel): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(userAttributes.USER_NAME, user.name)
            put(userAttributes.USER_EMAIL, user.email)
            put(userAttributes.USER_PASSWORD, user.uid) // Using uid as password placeholder
            put(userAttributes.USER_CREATED_AT, user.createdAt)
        }
        val id = db.insert(userAttributes.TABLE_NAME, null, values)
        db.close()
        return id
    }

    fun getAllUsers(): List<userModel> {
        val userList = mutableListOf<userModel>()
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM ${userAttributes.TABLE_NAME}", null)
        if (cursor.moveToFirst()) {
            do {
                val user = userModel(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(userAttributes.USER_ID)).toString(),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(userAttributes.USER_NAME)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(userAttributes.USER_EMAIL)),
                    uid = cursor.getString(cursor.getColumnIndexOrThrow(userAttributes.USER_PASSWORD)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow(userAttributes.USER_CREATED_AT))
                )
                userList.add(user)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return userList
    }

    fun getUserById(userId: String): userModel? {
        val db = readableDatabase
        val cursor = db.query(
            userAttributes.TABLE_NAME,
            null,
            "${userAttributes.USER_ID} = ?",
            arrayOf(userId),
            null, null, null
        )
        var user: userModel? = null
        if (cursor.moveToFirst()) {
            user = userModel(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(userAttributes.USER_ID)).toString(),
                name = cursor.getString(cursor.getColumnIndexOrThrow(userAttributes.USER_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(userAttributes.USER_EMAIL)),
                uid = cursor.getString(cursor.getColumnIndexOrThrow(userAttributes.USER_PASSWORD)),
                createdAt = cursor.getString(cursor.getColumnIndexOrThrow(userAttributes.USER_CREATED_AT))
            )
        }
        cursor.close()
        db.close()
        return user
    }

    fun updateUser(user: userModel): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(userAttributes.USER_NAME, user.name)
            put(userAttributes.USER_EMAIL, user.email)
            put(userAttributes.USER_PASSWORD, user.uid)
            put(userAttributes.USER_CREATED_AT, user.createdAt)
        }
        val rows = db.update(
            userAttributes.TABLE_NAME,
            values,
            "${userAttributes.USER_ID} = ?",
            arrayOf(user.id)
        )
        db.close()
        return rows
    }

    fun deleteUser(userId: String): Int {
        val db = writableDatabase
        val rows = db.delete(
            userAttributes.TABLE_NAME,
            "${userAttributes.USER_ID} = ?",
            arrayOf(userId)
        )
        db.close()
        return rows
    }
}
