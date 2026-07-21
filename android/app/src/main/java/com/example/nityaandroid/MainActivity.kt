package com.example.nityaandroid

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.nityaandroid.databinding.ActivityMainBinding
import com.example.nityaandroid.receiver.ReminderReceiver
import com.example.nityaandroid.ui.AuthViewModel
import com.example.nityaandroid.utils.Resource
import com.example.nityaandroid.utils.TokenManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var tokenManager: TokenManager

    private val prefs: SharedPreferences by lazy {
        getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            scheduleDailyReminder()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupAutoLogin()
        setupNavigation()
        setupTopAppBar()
        observeLogoutState()
        checkNotificationPermissions()

        handleSessionExpiration(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSessionExpiration(intent)
    }

    private fun handleSessionExpiration(intent: Intent?) {
        if (intent?.getBooleanExtra("SESSION_EXPIRED", false) == true) {
            navigateToLoginSecurely()
            android.widget.Toast.makeText(this, "Session expired. Please log in again.", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun setupAutoLogin() {
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        if (tokenManager.getAccessToken() != null) {
            navGraph.setStartDestination(R.id.dashboardFragment)
        } else {
            navGraph.setStartDestination(R.id.loginFragment)
        }

        navController.graph = navGraph
    }

    private fun setupNavigation() {
        binding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment -> {
                    binding.bottomNavigationView.visibility = View.GONE
                    binding.appBarLayout.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavigationView.visibility = View.VISIBLE
                    binding.appBarLayout.visibility = View.VISIBLE
                    binding.topAppBar.title = destination.label
                }
            }
        }
    }

    private fun setupTopAppBar() {
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.dashboardFragment, R.id.feedFragment, R.id.leaderboardFragment)
        )
        binding.topAppBar.setupWithNavController(navController, appBarConfiguration)

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    showLogoutConfirmationDialog()
                    true
                }
                R.id.action_set_reminder -> {
                    showTimePickerDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun showTimePickerDialog() {
        val currentHour = prefs.getInt("reminder_hour", 20)
        val currentMinute = prefs.getInt("reminder_minute", 0)

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(currentHour)
            .setMinute(currentMinute)
            .setTitleText("Select Reminder Time")
            .setTheme(R.style.MyTimePickerTheme)
            .build()

        picker.show(supportFragmentManager, "REMINDER_TIME_PICKER")

        picker.addOnPositiveButtonClickListener {
            prefs.edit()
                .putInt("reminder_hour", picker.hour)
                .putInt("reminder_minute", picker.minute)
                .apply()

            scheduleDailyReminder()

            val amPm = if (picker.hour >= 12) "PM" else "AM"
            val displayHour = if (picker.hour % 12 == 0) 12 else picker.hour % 12
            val displayMinute = String.format("%02d", picker.minute)
            android.widget.Toast.makeText(this, "Reminder set for $displayHour:$displayMinute $amPm", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLogoutConfirmationDialog() {
        lifecycleScope.launch {
            val hasOfflineData = authViewModel.checkUnsyncedData()

            val message = if (hasOfflineData) {
                "⚠️ You have offline changes that haven't been saved to the cloud. If you log out now, they will be permanently lost. Are you sure?"
            } else {
                "Are you sure you want to log out?"
            }

            MaterialAlertDialogBuilder(this@MainActivity)
                .setTitle("Logout")
                .setMessage(message)
                .setPositiveButton(if (hasOfflineData) "Logout & Lose Data" else "Logout") { _, _ ->

                    tokenManager.clearTokens()

                    val syncPrefs = getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
                    syncPrefs.edit().clear().apply()

                    prefs.edit().clear().apply()

                    authViewModel.logout()

                    navigateToLoginSecurely()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun observeLogoutState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.logoutState.collect { resource ->
                    if (resource is Resource.Success) {
                        navigateToLoginSecurely()
                    }
                }
            }
        }
    }

    private fun navigateToLoginSecurely() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph, true)
            .build()

        navController.navigate(R.id.loginFragment, null, navOptions)
    }

    private fun checkNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    scheduleDailyReminder()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            scheduleDailyReminder()
        }
    }

    private fun scheduleDailyReminder() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val savedHour = prefs.getInt("reminder_hour", 20)
        val savedMinute = prefs.getInt("reminder_minute", 0)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()

            set(Calendar.HOUR_OF_DAY, savedHour)
            set(Calendar.MINUTE, savedMinute)
            set(Calendar.SECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}