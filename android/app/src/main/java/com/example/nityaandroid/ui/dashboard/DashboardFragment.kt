package com.example.nityaandroid.ui.dashboard


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.nityaandroid.data.local.entity.HabitEntity
import com.example.nityaandroid.databinding.FragmentDashboardBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HabitViewModel by viewModels()
    private lateinit var habitAdapter: HabitAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        viewModel.triggerSyncWorker()
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(
            onHabitCompleted = { syncId, date ->
                viewModel.markHabitCompleted(syncId, date)
            },
            onHabitLongPressed = { habit ->
                showDeleteHabitDialog(habit)
            }
        )

        binding.rvHabits.adapter = habitAdapter
        binding.rvHabits.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
    }

    private fun setupClickListeners() {
        binding.fabAddHabit.setOnClickListener {
            showCreateHabitDialog()
        }
    }

    private fun showCreateHabitDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val titleInput = EditText(context).apply { hint = "Habit Title" }
        val descInput = EditText(context).apply { hint = "Description" }

        layout.addView(titleInput)
        layout.addView(descInput)

        MaterialAlertDialogBuilder(context)
            .setTitle("Create New Habit")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val title = titleInput.text.toString().trim()
                val desc = descInput.text.toString().trim()
                if (title.isNotEmpty()) {
                    viewModel.createHabit(title, desc, "DAILY")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.habits.collect { habitsList ->
                    habitAdapter.submitList(habitsList)
                }
            }
        }
    }

    private fun showDeleteHabitDialog(habit: HabitEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.title}'? This will erase your streak.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteHabit(habit.syncId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}