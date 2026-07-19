package com.example.nityaandroid.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

    fun getLastSyncTime(): String? {
        return prefs.getString("LAST_SYNC_AT", null)
    }

    fun saveLastSyncTime(timestamp: String) {
        prefs.edit().putString("LAST_SYNC_AT", timestamp).apply()
    }
}