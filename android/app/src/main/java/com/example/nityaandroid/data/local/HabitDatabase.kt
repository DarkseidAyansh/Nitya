package com.example.nityaandroid.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nityaandroid.data.local.dao.HabitDao
import com.example.nityaandroid.data.local.entity.HabitEntity

@Database(
    entities = [HabitEntity::class],
    version = 3,
    exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {
    abstract val habitDao: HabitDao
}