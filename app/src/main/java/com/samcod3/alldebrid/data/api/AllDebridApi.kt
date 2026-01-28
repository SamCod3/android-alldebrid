package com.samcod3.alldebrid.data.api

import com.samcod3.alldebrid.data.model.BaseResponse
import com.samcod3.alldebrid.data.model.MagnetFilesResponse
import com.samcod3.alldebrid.data.model.MagnetUploadResponse
import com.samcod3.alldebrid.data.model.MagnetsResponse
import com.samcod3.alldebrid.data.model.UnlockResponse
import com.samcod3.alldebrid.data.model.UserResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Url

interface AllDebridApi {
    
    companion object {
        const val BASE_URL = "https://api.alldebrid.com/v4/"
        const val BASE_URL_V41 = "https://api.alldebrid.com/v4.1/"
        const val AGENT = "AllDebridManager"
    }
    
    // User endpoint - still GET (no change)
    @GET("user")
    suspend fun getUser(
        @Query("agent") agent: String = AGENT,
        @Query("apikey") apiKey: String
    ): Response<UserResponse>
    
    // Magnet status - POST with v4.1 URL
    @FormUrlEncoded
    @POST
    suspend fun getMagnets(
        @Url url: String = "${BASE_URL_V41}magnet/status",
        @Field("agent") agent: String = AGENT,
        @Field("apikey") apiKey: String
    ): Response<MagnetsResponse>
    
    // NEW: Get magnet files - POST (files no longer in magnet/status response)
    @FormUrlEncoded
    @POST("magnet/files")
    suspend fun getMagnetFiles(
        @Field("agent") agent: String = AGENT,
        @Field("apikey") apiKey: String,
        @Field("id[]") id: Long
    ): Response<MagnetFilesResponse>
    
    // Upload magnet - changed from GET to POST
    @FormUrlEncoded
    @POST("magnet/upload")
    suspend fun uploadMagnet(
        @Field("agent") agent: String = AGENT,
        @Field("apikey") apiKey: String,
        @Field("magnets[]") magnet: String
    ): Response<MagnetUploadResponse>
    
    // Upload torrent file - multipart POST (no change)
    @Multipart
    @POST("magnet/upload/file")
    suspend fun uploadTorrentFile(
        @Query("agent") agent: String = AGENT,
        @Query("apikey") apiKey: String,
        @Part file: MultipartBody.Part
    ): Response<MagnetUploadResponse>
    
    // Delete magnet - changed from GET to POST
    @FormUrlEncoded
    @POST("magnet/delete")
    suspend fun deleteMagnet(
        @Field("agent") agent: String = AGENT,
        @Field("apikey") apiKey: String,
        @Field("id") id: Long
    ): Response<BaseResponse>
    
    // Restart magnet - changed from GET to POST
    @FormUrlEncoded
    @POST("magnet/restart")
    suspend fun restartMagnet(
        @Field("agent") agent: String = AGENT,
        @Field("apikey") apiKey: String,
        @Field("id") id: Long
    ): Response<BaseResponse>
    
    // Unlock link - changed from GET to POST
    @FormUrlEncoded
    @POST("link/unlock")
    suspend fun unlockLink(
        @Field("agent") agent: String = AGENT,
        @Field("apikey") apiKey: String,
        @Field("link") link: String
    ): Response<UnlockResponse>
}
