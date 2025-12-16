package com.samcod3.alldebrid.data.api

import com.samcod3.alldebrid.data.model.JackettSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface JackettApi {
    
    @GET
    suspend fun search(
        @Url baseUrl: String,
        @Query("apikey") apiKey: String,
        @Query("Query") query: String,
        @Query("Category[]") categories: List<Int>? = null
    ): Response<JackettSearchResponse>
}
