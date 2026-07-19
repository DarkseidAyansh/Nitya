package com.example.nityaandroid.data.remote

import com.example.nityaandroid.data.remote.models.ApiResponse
import com.example.nityaandroid.data.remote.models.LoginRequest
import com.example.nityaandroid.data.remote.models.LoginResponseData
import com.example.nityaandroid.data.remote.models.RegisterRequest
import com.example.nityaandroid.data.remote.models.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("users/register")
    suspend fun registerUser(
        @Body request: RegisterRequest
    ): Response<ApiResponse<UserDto>>

    @POST("users/login")
    suspend fun loginUser(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResponseData>>

    @POST("users/logout")
    suspend fun logoutUser(): Response<ApiResponse<Any>>
}