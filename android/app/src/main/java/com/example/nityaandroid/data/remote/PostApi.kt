package com.example.nityaandroid.data.remote

import com.example.nityaandroid.data.remote.models.ApiResponse
import com.example.nityaandroid.data.remote.models.CommentDto
import com.example.nityaandroid.data.remote.models.CommentRequest
import com.example.nityaandroid.data.remote.models.CreatePostRequest
import com.example.nityaandroid.data.remote.models.FeedResponseData
import com.example.nityaandroid.data.remote.models.PostDto
import com.example.nityaandroid.data.remote.models.ReactionResponseData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PostApi {

    @GET("posts")
    suspend fun getFeed(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 10
    ): Response<ApiResponse<FeedResponseData>>

    @POST("posts/{postId}/reaction")
    suspend fun toggleReaction(
        @Path("postId") postId: String
    ): Response<ApiResponse<ReactionResponseData>>

    @POST("posts")
    suspend fun createPost(
        @Body request: CreatePostRequest
    ): Response<ApiResponse<PostDto>>

    @POST("posts/{postId}/comments")
    suspend fun addComment(
        @Path("postId") postId: String,
        @Body request: CommentRequest
    ): Response<ApiResponse<CommentDto>>

    @GET("posts/{postId}/comments")
    suspend fun getComments(@Path("postId") postId: String): Response<ApiResponse<List<CommentDto>>>
}