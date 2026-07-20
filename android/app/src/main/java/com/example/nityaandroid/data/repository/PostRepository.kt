package com.example.nityaandroid.data.repository

import com.example.nityaandroid.data.remote.PostApi
import com.example.nityaandroid.data.remote.models.CommentDto
import com.example.nityaandroid.data.remote.models.CommentRequest
import com.example.nityaandroid.data.remote.models.CreatePostRequest
import com.example.nityaandroid.data.remote.models.FeedResponseData
import com.example.nityaandroid.data.remote.models.PostDto
import com.example.nityaandroid.data.remote.models.ReactionResponseData
import com.example.nityaandroid.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val api: PostApi
) {
    fun getFeed(page: Int): Flow<Resource<FeedResponseData>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getFeed(page)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(Resource.Success(response.body()!!.data!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to fetch feed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    fun toggleReaction(postId: String): Flow<Resource<ReactionResponseData>> = flow {
        try {
            val response = api.toggleReaction(postId)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(Resource.Success(response.body()!!.data!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to toggle reaction"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    fun createPost(content: String): Flow<Resource<PostDto>> = flow {
        try {
            val response = api.createPost(CreatePostRequest(content))
            if (response.isSuccessful && response.body()?.data != null) {
                emit(Resource.Success(response.body()!!.data!!))
            } else {
                emit(Resource.Error("Failed to create post"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error"))
        }
    }.flowOn(Dispatchers.IO)

    fun addComment(postId: String, content: String): Flow<Resource<CommentDto>> = flow {
        try {
            val response = api.addComment(postId, CommentRequest(content))

            if (response.isSuccessful && response.body()?.data != null) {
                emit(Resource.Success(response.body()!!.data!!))
            } else {
                emit(Resource.Error("Failed to add comment"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error"))
        }
    }.flowOn(Dispatchers.IO)

    fun getComments(postId: String): Flow<Resource<List<CommentDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getComments(postId)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(Resource.Success(response.body()!!.data!!))
            } else {
                emit(Resource.Error("Failed to load comments"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)
}