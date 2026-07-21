package com.example.nityaandroid.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nityaandroid.data.remote.models.PostDto
import com.example.nityaandroid.databinding.ItemPostBinding

class PostAdapter(
    private val onLikeClicked: (String) -> Unit,
    private val onCommentClicked: (String) -> Unit
) : ListAdapter<PostDto, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: PostDto) {
            binding.tvAuthorName.text = post.author?.name
            binding.tvPostContent.text = post.content
            binding.btnComment.text = "${post.commentsCount} Comments"

            binding.btnLike.text = post.likesCount.toString()
            binding.btnLike.alpha = if (post.isLikedByMe) 1.0f else 0.5f

            binding.btnComment.text = "${post.commentsCount} Comments"

            binding.btnLike.setOnClickListener {
                onLikeClicked(post._id)
            }
            binding.btnComment.setOnClickListener {
                onCommentClicked(post._id)
            }
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<PostDto>() {
        override fun areItemsTheSame(oldItem: PostDto, newItem: PostDto): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: PostDto, newItem: PostDto): Boolean {
            return oldItem == newItem
        }
    }
}