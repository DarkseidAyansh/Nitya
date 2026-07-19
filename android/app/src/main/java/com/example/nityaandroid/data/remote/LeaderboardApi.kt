package com.example.nityaandroid.data.remote

import com.example.nityaandroid.data.remote.models.ApiResponse
import com.example.nityaandroid.data.remote.models.LeaderboardEntryDto
import com.example.nityaandroid.data.remote.models.MyRankDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface LeaderboardApi {
    @GET("leaderboards/global")
    suspend fun getGlobalLeaderboard(): Response<ApiResponse<List<LeaderboardEntryDto>>>

    @GET("leaderboards/my-rank")
    suspend fun getMyRank(): Response<ApiResponse<MyRankDto>>

    @POST("leaderboards/award")
    suspend fun awardPoints(
        @Body requestBody: Map<String, @JvmSuppressWildcards Any>
    ): Response<Any>
}
