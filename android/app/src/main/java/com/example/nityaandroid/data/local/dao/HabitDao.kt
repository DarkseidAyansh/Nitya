    package com.example.nityaandroid.data.local.dao

    import androidx.room.Dao
    import androidx.room.Insert
    import androidx.room.OnConflictStrategy
    import androidx.room.Query
    import androidx.room.Update
    import com.example.nityaandroid.data.local.entity.HabitEntity
    import kotlinx.coroutines.flow.Flow

    @Dao
    interface HabitDao {

        @Query("SELECT * FROM habits WHERE isDeleted = 0 ORDER BY clientUpdatedAt DESC")
        fun getActiveHabits(): Flow<List<HabitEntity>>

        @Query("SELECT * FROM habits WHERE isPendingSync = 1")
        suspend fun getUnsyncedHabits(): List<HabitEntity>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertHabit(habit: HabitEntity)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertHabits(habits: List<HabitEntity>)

        @Update
        suspend fun updateHabit(habit: HabitEntity)

        @Query("UPDATE habits SET isDeleted = 1, clientUpdatedAt = :timestamp, isPendingSync = 1 WHERE syncId = :syncId")
        suspend fun softDeleteHabit(syncId: String, timestamp: String)

        @Query("UPDATE habits SET isPendingSync = 0 WHERE syncId IN (:syncIds)")
        suspend fun markAsSynced(syncIds: List<String>)

        @Query("UPDATE habits SET lastCompletedDate = :date, streakCount = :streak, isPendingSync = 1, clientUpdatedAt = :clientUpdatedAt WHERE syncId = :syncId")
        suspend fun markHabitCompleted(syncId: String, date: String, streak: Int, clientUpdatedAt: String)

        // Add this right below your existing getActiveHabits() Flow function
        @Query("SELECT * FROM habits WHERE isDeleted = 0")
        suspend fun getActiveHabitsList(): List<HabitEntity>

        @Query("DELETE FROM habits")
        suspend fun deleteAllHabits()
    }