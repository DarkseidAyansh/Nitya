package com.example.nityaandroid.data.remote.models

data class LeaderboardEntryDto(
    val userId: String,
    val name: String,
    val points: Int,
    val currentStreak: Int,
    val longestStreak: Int
)

data class MyRankDto(
    val rank: Int,
    val points: Int,
    val currentStreak: Int,
    val longestStreak: Int
)