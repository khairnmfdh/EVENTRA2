package com.example.eventra1.database

import androidx.room.Database
import com.example.eventra1.model.ModelDatabase
import androidx.room.RoomDatabase
import com.example.eventra1.database.dao.DatabaseDao

@Database(entities = [ModelDatabase::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun databaseDao(): DatabaseDao?
}