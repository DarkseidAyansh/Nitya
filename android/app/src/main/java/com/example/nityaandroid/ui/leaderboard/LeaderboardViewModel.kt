package com.example.nityaandroid.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nityaandroid.data.remote.models.LeaderboardEntryDto
import com.example.nityaandroid.data.remote.models.MyRankDto
import com.example.nityaandroid.data.repository.LeaderboardRepository
import com.example.nityaandroid.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val repository: LeaderboardRepository
) : ViewModel() {

    private val _globalLeaderboard = MutableStateFlow<Resource<List<LeaderboardEntryDto>>>(Resource.Loading())
    val globalLeaderboard = _globalLeaderboard.asStateFlow()

    private val _myRank = MutableStateFlow<Resource<MyRankDto>>(Resource.Loading())
    val myRank = _myRank.asStateFlow()

    init {
        fetchLeaderboardData()
    }

    fun fetchLeaderboardData() {
        _globalLeaderboard.value = Resource.Loading()
        _myRank.value = Resource.Loading()

        viewModelScope.launch(Dispatchers.IO) {
            val globalDeferred = async { repository.getGlobalLeaderboard() }
            val myRankDeferred = async { repository.getMyRank() }

            _globalLeaderboard.value = globalDeferred.await()
            _myRank.value = myRankDeferred.await()
        }
    }
}