package com.github.loooris.zonetasker

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "mydatabase.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS geofence (idGeofence INTEGER PRIMARY KEY AUTOINCREMENT, latitude VARCHAR(255), longitude VARCHAR(255), radius INTEGER, entering BOOLEAN, exiting BOOLEAN, phoneNumber VARCHAR(255), message TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS geofence")
        onCreate(db)
    }

    fun addGeofence(
        latitude: String,
        longitude: String,
        radius: Int,
        entering: Boolean,
        exiting: Boolean,
        phoneNumber: String,
        message: String
    ) {

    }
}
