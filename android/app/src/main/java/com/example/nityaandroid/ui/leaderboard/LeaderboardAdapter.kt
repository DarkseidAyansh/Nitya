package com.example.nityaandroid.ui.leaderboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nityaandroid.data.remote.models.LeaderboardEntryDto
import com.example.nityaandroid.databinding.ItemLeaderboardRankBinding

class LeaderboardAdapter : ListAdapter<LeaderboardEntryDto, LeaderboardAdapter.RankViewHolder>(RankDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankViewHolder {
        val binding = ItemLeaderboardRankBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RankViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RankViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    inner class RankViewHolder(private val binding: ItemLeaderboardRankBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: LeaderboardEntryDto, rank: Int) {
            binding.tvRankNumber.text = "#$rank"
            binding.tvPlayerName.text = entry.name
            binding.tvPoints.text = "${entry.points} pts"
            binding.tvStreak.text = "🔥 ${entry.currentStreak} Day Streak"
        }
    }

    class RankDiffCallback : DiffUtil.ItemCallback<LeaderboardEntryDto>() {
        override fun areItemsTheSame(oldItem: LeaderboardEntryDto, newItem: LeaderboardEntryDto): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: LeaderboardEntryDto, newItem: LeaderboardEntryDto): Boolean {
            return oldItem == newItem
        }
    }
}