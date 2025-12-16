package com.samcod3.alldebrid.data.model

import com.google.gson.annotations.SerializedName

data class Link(
    @SerializedName("link")
    val link: String,
    
    @SerializedName("host")
    val host: String,
    
    @SerializedName("filename")
    val filename: String,
    
    @SerializedName("streaming")
    val streaming: List<StreamingLink>? = null,
    
    @SerializedName("paws")
    val paws: Boolean = false,
    
    @SerializedName("filesize")
    val filesize: Long = 0,
    
    @SerializedName("id")
    val id: String? = null
)

data class StreamingLink(
    @SerializedName("quality")
    val quality: String,
    
    @SerializedName("ext")
    val ext: String,
    
    @SerializedName("filesize")
    val filesize: Long,
    
    @SerializedName("link")
    val link: String
)

data class UnlockResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("data")
    val data: Link?,
    
    @SerializedName("error")
    val error: ApiError? = null
)

data class BaseResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("error")
    val error: ApiError? = null
)

data class ApiError(
    @SerializedName("code")
    val code: String,
    
    @SerializedName("message")
    val message: String
)
