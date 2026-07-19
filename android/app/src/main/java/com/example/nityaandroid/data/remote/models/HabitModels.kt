package com.example.nityaandroid.data.remote.models

data class HabitSyncRequest(
    val lastSyncAt: String?,
    val changes: List<HabitChangeDto>
)

data class HabitChangeDto(
    val syncId: String,
    val title: String,
    val description: String,
    val frequency: String,
    val isDeleted: Boolean,
    val clientUpdatedAt: String,
    val lastCompletedDate: String?,
    val streakCount: Int
)

data class HabitSyncResponseData(
    val serverChanges: List<HabitChangeDto>,
    val lastSyncAt: String
)
