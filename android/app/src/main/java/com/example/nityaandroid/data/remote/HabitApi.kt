package com.example.nityaandroid.data.remote

import com.example.nityaandroid.data.remote.models.ApiResponse
import com.example.nityaandroid.data.remote.models.HabitSyncRequest
import com.example.nityaandroid.data.remote.models.HabitSyncResponseData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface HabitApi {
    @POST("habits/sync")
    suspend fun syncHabits(
        @Body request: HabitSyncRequest
    ): Response<ApiResponse<HabitSyncResponseData>>
}