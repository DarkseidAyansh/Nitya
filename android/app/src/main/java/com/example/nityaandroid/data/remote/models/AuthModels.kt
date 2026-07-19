package com.example.nityaandroid.data.remote.models

data class ApiResponse<T>(
    val statusCode: Int,
    val data: T?,
    val message: String,
    val success: Boolean
)

data class UserDto(
    val _id: String,
    val name: String,
    val email: String
)

data class LoginResponseData(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)