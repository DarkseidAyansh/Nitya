package com.example.nityaandroid.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nityaandroid.data.repository.HabitRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val habitRepository: HabitRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val success = habitRepository.synchronizeWithServer()
            if (success) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 401) {
                Result.failure()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}