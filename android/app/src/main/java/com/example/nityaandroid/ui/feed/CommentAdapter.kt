package com.example.nityaandroid.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nityaandroid.data.remote.models.CommentDto
import com.example.nityaandroid.databinding.ItemCommentBinding

class CommentAdapter : ListAdapter<CommentDto, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CommentViewHolder(private val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: CommentDto) {
            binding.tvCommentAuthor.text = comment.user.name
            binding.tvCommentContent.text = comment.content
        }
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<CommentDto>() {
        override fun areItemsTheSame(oldItem: CommentDto, newItem: CommentDto): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: CommentDto, newItem: CommentDto): Boolean {
            return oldItem == newItem
        }
    }
}