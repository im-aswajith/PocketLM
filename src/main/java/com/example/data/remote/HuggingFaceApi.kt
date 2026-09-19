package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HuggingFaceApi {
    @GET("api/models")
    suspend fun searchModels(
        @Query("search") search: String,
        @Query("filter") filter: String? = "text-generation",
        @Query("limit") limit: Int = 25,
        @Query("full") full: Boolean = true
    ): List<HfSearchItem>

    @GET("api/models/{modelId}")
    suspend fun getModelDetail(
        @Path(value = "modelId", encoded = true) modelId: String
    ): HfModelDetail
}
