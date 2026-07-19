package com.example.nityaandroid.utils

import android.content.Context
import android.content.Intent
import com.example.nityaandroid.MainActivity
import com.example.nityaandroid.data.local.HabitDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Provider

class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val habitDatabase: HabitDatabase,
    @ApplicationContext private val context: Context
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.priorResponse != null) {
            return null
        }

        val refreshToken = tokenManager.getRefreshToken()

        if (refreshToken == null) {
            forceLogout()
            return null
        }

        synchronized(this) {
            val currentAccessToken = tokenManager.getAccessToken()
            val requestAccessToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            if (currentAccessToken != null && currentAccessToken != requestAccessToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccessToken")
                    .build()
            }

            val client = OkHttpClient()
            val jsonPayload = """{"refreshToken": "$refreshToken"}"""
            val requestBody = jsonPayload.toRequestBody("application/json".toMediaType())

            val refreshRequest = Request.Builder()
                .url("${Constants.BASE_URL}users/refresh")
                .post(requestBody)
                .build()

            try {
                val refreshResponse = client.newCall(refreshRequest).execute()

                if (refreshResponse.isSuccessful) {
                    val responseBodyString = refreshResponse.body?.string()

                    if (responseBodyString != null) {
                        val jsonObject = JSONObject(responseBodyString)
                        val dataObject = jsonObject.getJSONObject("data")
                        val newAccessToken = dataObject.getString("accessToken")
                        val newRefreshToken = dataObject.getString("refreshToken")

                        tokenManager.saveTokens(newAccessToken, newRefreshToken)

                        return response.request.newBuilder()
                            .header("Authorization", "Bearer $newAccessToken")
                            .build()
                    }
                } else {
                    forceLogout()
                    return null
                }
            } catch (e: Exception) {
                return null
            }
        }
        return null
    }

    private fun forceLogout() {
        tokenManager.clearTokens()
        habitDatabase.clearAllTables()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("SESSION_EXPIRED", true)
        }
        context.startActivity(intent)
    }
}
