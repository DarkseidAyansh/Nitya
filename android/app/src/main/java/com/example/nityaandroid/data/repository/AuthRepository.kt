package com.example.nityaandroid.data.repository

import android.content.Context
import com.example.nityaandroid.data.local.dao.HabitDao
import com.example.nityaandroid.data.remote.AuthApi
import com.example.nityaandroid.data.remote.models.LoginRequest
import com.example.nityaandroid.data.remote.models.RegisterRequest
import com.example.nityaandroid.data.remote.models.UserDto
import com.example.nityaandroid.utils.Resource
import com.example.nityaandroid.utils.TokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val tokenManager: TokenManager,
    private val habitDao: HabitDao,
    @ApplicationContext private val context: Context
) {
    fun login(request: LoginRequest): Flow<Resource<UserDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.loginUser(request)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data
                if (data != null) {
                    tokenManager.saveTokens(data.accessToken, data.refreshToken)
                    emit(Resource.Success(data.user))
                } else {
                    emit(Resource.Error("Unexpected response format"))
                }
            } else {
                emit(Resource.Error(response.message() ?: "Login failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    fun register(request: RegisterRequest): Flow<Resource<UserDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.registerUser(request)
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!.data
                if (user != null) {
                    emit(Resource.Success(user))
                } else {
                    emit(Resource.Error("Unexpected response format"))
                }
            } else {
                emit(Resource.Error(response.message() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    fun logout(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            api.logoutUser()
            tokenManager.clearTokens()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            tokenManager.clearTokens()
            emit(Resource.Success(Unit))
        }
    }.flowOn(Dispatchers.IO)


    suspend fun hasUnsyncedData(): Boolean {
        return withContext(Dispatchers.IO) {
            val unsyncedHabits = habitDao.getUnsyncedHabits()
            unsyncedHabits.isNotEmpty()
        }
    }

    suspend fun clearLocalSyncData() {
        withContext(Dispatchers.IO) {
            habitDao.deleteAllHabits()
            val syncPrefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
            syncPrefs.edit().clear().apply()
        }
    }
}