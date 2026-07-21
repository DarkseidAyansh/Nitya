    package com.example.nityaandroid.ui.dashboard

    import android.content.Context
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import androidx.work.Constraints
    import androidx.work.ExistingWorkPolicy
    import androidx.work.NetworkType
    import androidx.work.OneTimeWorkRequestBuilder
    import androidx.work.WorkManager
    import com.example.nityaandroid.data.local.entity.HabitEntity
    import com.example.nityaandroid.data.repository.HabitRepository
    import com.example.nityaandroid.data.repository.LeaderboardRepository
    import com.example.nityaandroid.worker.SyncWorker
    import dagger.hilt.android.lifecycle.HiltViewModel
    import dagger.hilt.android.qualifiers.ApplicationContext
    import kotlinx.coroutines.flow.SharingStarted
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.flow.stateIn
    import kotlinx.coroutines.launch
    import javax.inject.Inject

    @HiltViewModel
    class HabitViewModel @Inject constructor(
        private val repository: HabitRepository,
        private val leaderboardRepository: LeaderboardRepository,
        @ApplicationContext private val context: Context
    ) : ViewModel() {

        init {
            triggerSyncWorker()
        }

        val habits: StateFlow<List<HabitEntity>> = repository.getActiveHabits()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        fun createHabit(title: String, description: String, frequency: String) {
            viewModelScope.launch {
                repository.createHabitLocal(title, description, frequency)
                triggerSyncWorker()
            }
        }

        fun triggerSyncWorker() {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "SyncHabitsWork",
                ExistingWorkPolicy.REPLACE,
                syncWorkRequest
            )
        }

        fun markHabitCompleted(syncId: String, previousCompletionDate: String?) {
            viewModelScope.launch {
                val todayString = java.time.LocalDate.now().toString()
                val yesterday = java.time.LocalDate.now().minusDays(1).toString()

                if (previousCompletionDate == todayString) return@launch

                val currentHabit = repository.getActiveHabitsList().find { it.syncId == syncId }
                    ?: return@launch

                val isStreakContinued = previousCompletionDate == yesterday
                val newStreak = if (isStreakContinued) currentHabit.streakCount + 1 else 1

                repository.markHabitCompletedLocal(syncId, todayString, newStreak)

                launch { leaderboardRepository.awardActivityPoints(50, isStreakContinued) }
                launch { triggerSyncWorker() }
            }
        }

        fun deleteHabit(syncId: String) {
            viewModelScope.launch {
                repository.deleteHabitLocal(syncId)
                triggerSyncWorker()
            }
        }
    }