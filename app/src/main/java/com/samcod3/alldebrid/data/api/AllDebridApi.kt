package com.samcod3.alldebrid.data.api

import com.samcod3.alldebrid.data.model.BaseResponse
import com.samcod3.alldebrid.data.model.MagnetUploadResponse
import com.samcod3.alldebrid.data.model.MagnetsResponse
import com.samcod3.alldebrid.data.model.UnlockResponse
import com.samcod3.alldebrid.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AllDebridApi {
    
    companion object {
        const val BASE_URL = "https://api.alldebrid.com/v4/"
        const val AGENT = "AllDebridManager"
    }
    
    @GET("user")
    suspend fun getUser(
        @Query("agent") agent: String = AGENT,
        @Query("apikey") apiKey: String
    ): Response<UserResponse>
    
    @GET("magnet/status")
    suspend fun getMagnets(
        @Query("agent") agent: String = AGENT,
        @Query("apikey") apiKey: String
    ): Response<MagnetsResponse>
    
    @GET("magnet/upload")
    suspend fun uploadMagnet(
        @Query("agent") agent: String = AGENT,
        @Query("apikey") apiKey: String,
        @Query("magnets[]") magnet: String
    ): Response<MagnetUploadResponse>
    
    @GET("magnet/delete")
    suspend fun deleteMagnet(
        @Query("agent") agent: String = AGENT,
        @Query("apikey") apiKey: String,
        @Query("id") id: Long
    ): Response<BaseResponse>
    
    @GET("magnet/restart")
    suspend fun restartMagnet(
        @Query("agent") agent: String = AGENT,
        @Query("apikey") apiKey: String,
        @Query("id") id: Long
    ): Response<BaseResponse>
    
    @GET("link/unlock")
    suspend fun unlockLink(
        @Query("agent") agent: String = AGENT,
        @Query("apikey") apiKey: String,
        @Query("link") link: String
    ): Response<UnlockResponse>
}
