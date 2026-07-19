package com.example.nityaandroid.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey
    val syncId: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val frequency: String = "DAILY",
    val isDeleted: Boolean = false,
    val clientUpdatedAt: String,
    val isPendingSync: Boolean = true,
    val lastCompletedDate: String? = null,
    val streakCount: Int = 0
)