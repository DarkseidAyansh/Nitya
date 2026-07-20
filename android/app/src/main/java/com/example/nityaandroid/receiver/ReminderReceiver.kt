package com.example.nityaandroid.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.nityaandroid.data.local.dao.HabitDao
import com.example.nityaandroid.utils.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var habitDao: HabitDao

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habits = habitDao.getActiveHabitsList()
                val today = LocalDate.now().toString()

                val hasPendingHabits = habits.any { it.lastCompletedDate != today }

                if (hasPendingHabits) {
                    val notificationHelper = NotificationHelper(context)
                    notificationHelper.showReminderNotification()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}