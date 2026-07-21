package com.example.nityaandroid.ui.dashboard


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nityaandroid.data.local.entity.HabitEntity
import com.example.nityaandroid.databinding.ItemHabitBinding

class HabitAdapter(
    private val onHabitCompleted: (String, String?) -> Unit,
    private val onHabitLongPressed: (HabitEntity) -> Unit
) : ListAdapter<HabitEntity, HabitAdapter.HabitViewHolder>(HabitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HabitViewHolder(private val binding: ItemHabitBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(habit: HabitEntity) {
            binding.tvHabitTitle.text = habit.title
            binding.tvHabitDescription.text = habit.description
            binding.root.setOnLongClickListener {
                onHabitLongPressed(habit)
                true
            }

            val today = java.time.LocalDate.now().toString()
            val yesterday = java.time.LocalDate.now().minusDays(1).toString()
            val isCompletedToday = habit.lastCompletedDate == today

            val displayStreak = if (habit.lastCompletedDate == today || habit.lastCompletedDate == yesterday) {
                habit.streakCount
            } else {
                0
            }

            binding.tvHabitStreak.text = "Streak: $displayStreak days"

            binding.cbComplete.setOnCheckedChangeListener(null)
            binding.cbComplete.isChecked = isCompletedToday
            binding.cbComplete.isEnabled = !isCompletedToday

            if (!isCompletedToday) {
                binding.cbComplete.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        binding.cbComplete.isEnabled = false
                        binding.cbComplete.isChecked = true
                        onHabitCompleted(habit.syncId, habit.lastCompletedDate)
                    }
                }
            }

            binding.ivSyncStatus.visibility = if (habit.isPendingSync) View.VISIBLE else View.GONE
        }
    }
    class HabitDiffCallback : DiffUtil.ItemCallback<HabitEntity>() {
        override fun areItemsTheSame(oldItem: HabitEntity, newItem: HabitEntity): Boolean {
            return oldItem.syncId == newItem.syncId
        }

        override fun areContentsTheSame(oldItem: HabitEntity, newItem: HabitEntity): Boolean {
            return oldItem == newItem
        }
    }
}