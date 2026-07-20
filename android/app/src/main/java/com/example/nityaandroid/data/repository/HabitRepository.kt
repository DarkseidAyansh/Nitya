package com.example.nityaandroid.data.repository

import com.example.nityaandroid.data.local.dao.HabitDao
import com.example.nityaandroid.data.local.entity.HabitEntity
import com.example.nityaandroid.data.remote.HabitApi
import com.example.nityaandroid.data.remote.models.HabitChangeDto
import com.example.nityaandroid.data.remote.models.HabitSyncRequest
import com.example.nityaandroid.utils.SyncPreferences
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.Instant

class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val habitApi: HabitApi,
    private val syncPreferences: SyncPreferences
) {

    fun getActiveHabits(): Flow<List<HabitEntity>> {
        return habitDao.getActiveHabits()
    }

    suspend fun createHabitLocal(title: String, description: String, frequency: String) {
        val entity = HabitEntity(
            title = title,
            description = description,
            frequency = frequency,
            clientUpdatedAt = Instant.now().toString(),
            isPendingSync = true
        )
        habitDao.insertHabit(entity)
    }

    suspend fun deleteHabitLocal(syncId: String) {
        habitDao.softDeleteHabit(syncId, Instant.now().toString())
    }

    suspend fun synchronizeWithServer(): Boolean {
        return try {
            val unsyncedLocalHabits = habitDao.getUnsyncedHabits()

            val changesToPush = unsyncedLocalHabits.map {
                HabitChangeDto(
                    syncId = it.syncId,
                    title = it.title,
                    description = it.description,
                    frequency = it.frequency,
                    isDeleted = it.isDeleted,
                    clientUpdatedAt = it.clientUpdatedAt,
                    lastCompletedDate = it.lastCompletedDate,
                    streakCount = it.streakCount
                )
            }

            val lastSyncTime = syncPreferences.getLastSyncTime()
            val request = HabitSyncRequest(lastSyncAt = lastSyncTime, changes = changesToPush)

            val response = habitApi.syncHabits(request)

            if (response.isSuccessful && response.body()?.data != null) {
                val responseData = response.body()!!.data!!

                val currentLocalHabits = habitDao.getActiveHabitsList()

                val serverEntities = responseData.serverChanges.map { serverHabit ->

                    val existingLocal = currentLocalHabits.find { it.syncId == serverHabit.syncId }

                    HabitEntity(
                        syncId = serverHabit.syncId,
                        title = serverHabit.title,
                        description = serverHabit.description,
                        frequency = serverHabit.frequency,
                        isDeleted = serverHabit.isDeleted,
                        clientUpdatedAt = serverHabit.clientUpdatedAt,
                        isPendingSync = false,

                        lastCompletedDate = existingLocal?.lastCompletedDate ?: serverHabit.lastCompletedDate,

                        streakCount = existingLocal?.streakCount ?: serverHabit.streakCount ?: 0
                    )
                }

                habitDao.insertHabits(serverEntities)

                val syncedIds = unsyncedLocalHabits.map { it.syncId }
                if (syncedIds.isNotEmpty()) {
                    habitDao.markAsSynced(syncedIds)
                }

                syncPreferences.saveLastSyncTime(responseData.lastSyncAt)
                return true
            } else {
                if (response.code() == 401) {
                    throw retrofit2.HttpException(response)
                }
                return false
            }
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 401) throw e
            return false
        } catch (e: Exception) {
            return false
        }
    }
    suspend fun getActiveHabitsList(): List<HabitEntity> {
        return habitDao.getActiveHabitsList()
    }

    suspend fun markHabitCompletedLocal(syncId: String, date: String, newStreak: Int) {
        habitDao.markHabitCompleted(
            syncId = syncId,
            date = date,
            streak = newStreak,
            clientUpdatedAt = java.time.Instant.now().toString()
        )
    }

}