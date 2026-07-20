package com.example.nityaandroid.data.repository

import com.example.nityaandroid.data.remote.LeaderboardApi
import com.example.nityaandroid.data.remote.models.LeaderboardEntryDto
import com.example.nityaandroid.data.remote.models.MyRankDto
import com.example.nityaandroid.utils.Resource
import javax.inject.Inject

class LeaderboardRepository @Inject constructor(
    private val api: LeaderboardApi
) {
    suspend fun getGlobalLeaderboard(): Resource<List<LeaderboardEntryDto>> {
        return try {
            val response = api.getGlobalLeaderboard()
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.message() ?: "Failed to fetch leaderboard")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun getMyRank(): Resource<MyRankDto> {
        return try {
            val response = api.getMyRank()
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.message() ?: "Failed to fetch rank")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun awardActivityPoints(points: Int, maintainStreak: Boolean): Boolean {
        return try {
            val requestBody = mapOf(
                "pointsToAdd" to points,
                "maintainStreak" to maintainStreak
            )

            val response = api.awardPoints(requestBody)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}