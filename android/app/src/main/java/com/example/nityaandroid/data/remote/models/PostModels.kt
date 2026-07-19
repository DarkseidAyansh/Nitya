package com.example.nityaandroid.data.remote.models

data class AuthorDto(
    val _id: String,
    val name: String
)

data class PostDto(
    val _id: String,
    val content: String,
    val createdAt: String,
    val author: AuthorDto?,
    val likesCount: Int,
    val commentsCount: Int,
    val isLikedByMe: Boolean,
    val habitContext: String?
)

data class FeedMetadata(
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val hasNextPage: Boolean
)

data class FeedResponseData(
    val posts: List<PostDto>,
    val metadata: FeedMetadata
)

data class ReactionResponseData(
    val isLiked: Boolean,
    val likesCount: Int
)

data class CreatePostRequest(
    val content: String,
    val habitContext: String? = null
)

data class CommentRequest(
    val content: String
)

data class CommentDto(
    val _id: String,
    val content: String,
    val user: UserDto,
    val createdAt: String
)
