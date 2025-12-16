package com.samcod3.alldebrid.data.model

import com.google.gson.annotations.SerializedName

data class Magnet(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("filename")
    val filename: String,
    
    @SerializedName("size")
    val size: Long,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("statusCode")
    val statusCode: Int,
    
    @SerializedName("downloaded")
    val downloaded: Long = 0,
    
    @SerializedName("uploaded")
    val uploaded: Long = 0,
    
    @SerializedName("seeders")
    val seeders: Int = 0,
    
    @SerializedName("downloadSpeed")
    val downloadSpeed: Long = 0,
    
    @SerializedName("uploadSpeed")
    val uploadSpeed: Long = 0,
    
    @SerializedName("uploadDate")
    val uploadDate: Long = 0,
    
    @SerializedName("completionDate")
    val completionDate: Long? = null,
    
    @SerializedName("links")
    val links: List<MagnetLink> = emptyList()
)

data class MagnetLink(
    @SerializedName("link")
    val link: String,
    
    @SerializedName("filename")
    val filename: String,
    
    @SerializedName("size")
    val size: Long = 0
)

data class MagnetsResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("data")
    val data: MagnetsData?
)

data class MagnetsData(
    @SerializedName("magnets")
    val magnets: List<Magnet>
)

data class MagnetUploadResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("data")
    val data: MagnetUploadData?,
    
    @SerializedName("error")
    val error: ApiError? = null
)

data class MagnetUploadData(
    @SerializedName("magnets")
    val magnets: List<UploadedMagnet>
)

data class UploadedMagnet(
    @SerializedName("magnet")
    val magnet: String,
    
    @SerializedName("hash")
    val hash: String? = null,
    
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("id")
    val id: Long? = null,
    
    @SerializedName("ready")
    val ready: Boolean = false,
    
    @SerializedName("error")
    val error: ApiError? = null
)
