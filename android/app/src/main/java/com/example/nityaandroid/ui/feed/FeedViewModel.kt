package com.example.nityaandroid.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nityaandroid.data.remote.models.CommentDto
import com.example.nityaandroid.data.remote.models.PostDto
import com.example.nityaandroid.data.repository.PostRepository
import com.example.nityaandroid.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    private val _posts = MutableStateFlow<List<PostDto>>(emptyList())
    val posts: StateFlow<List<PostDto>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _comments = MutableStateFlow<List<CommentDto>>(emptyList())
    val comments: StateFlow<List<CommentDto>> = _comments.asStateFlow()

    private var currentPage = 1
    private var hasNextPage = true
    private var isRequestInProgress = false

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        if (isRequestInProgress || !hasNextPage) return

        isRequestInProgress = true
        _isLoading.value = true

        viewModelScope.launch {
            repository.getFeed(currentPage).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val newPosts = resource.data?.posts ?: emptyList()
                        _posts.update { currentList -> currentList + newPosts }

                        hasNextPage = resource.data?.metadata?.hasNextPage ?: false
                        if (hasNextPage) {
                            currentPage++
                        }

                        isRequestInProgress = false
                        _isLoading.value = false
                    }
                    is Resource.Error -> {
                        isRequestInProgress = false
                        _isLoading.value = false
                    }
                    is Resource.Loading -> {

                    }
                }
            }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            repository.toggleReaction(postId).collect { resource ->
                if (resource is Resource.Success) {
                    val updatedData = resource.data
                    if (updatedData != null) {
                        _posts.update { currentList ->
                            currentList.map { post ->
                                if (post._id == postId) {
                                    post.copy(
                                        isLikedByMe = updatedData.isLiked,
                                        likesCount = updatedData.likesCount
                                    )
                                } else {
                                    post
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun createNewPost(content: String) {
        viewModelScope.launch {
            repository.createPost(content).collect { resource ->
                if (resource is Resource.Success) {
                    val newPost = resource.data
                    if (newPost != null) {
                        _posts.update { currentList -> listOf(newPost) + currentList }
                    }
                }
            }
        }
    }

    fun submitComment(postId: String, content: String) {
        viewModelScope.launch {
            repository.addComment(postId, content).collect { resource ->
                if (resource is Resource.Success) {
                    val newComment = resource.data
                    if (newComment != null) {

                        _comments.update { currentList -> listOf(newComment) + currentList }

                        _posts.update { currentList ->
                            currentList.map { post ->
                                if (post._id == postId) {
                                    post.copy(commentsCount = post.commentsCount + 1)
                                } else {
                                    post
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun loadCommentsForPost(postId: String) {
        viewModelScope.launch {
            repository.getComments(postId).collect { resource ->
                if (resource is Resource.Success) {
                    _comments.value = resource.data ?: emptyList()
                }
            }
        }
    }


}