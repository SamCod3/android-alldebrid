package com.samcod3.alldebrid.data.model

import com.google.gson.annotations.SerializedName

/**
 * Magnet data from /v4.1/magnet/status
 * Note: In v4.1, links are no longer included in this response.
 * Use /v4/magnet/files to get file links.
 */
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
    val completionDate: Long? = null
    
    // Note: 'links' field removed in v4.1 - use getMagnetFiles() instead
)

/**
 * Legacy MagnetLink - kept for compatibility with existing UI code
 * Will be replaced by FlatFile in the new flow
 */
data class MagnetLink(
    @SerializedName("link")
    val link: String,
    
    @SerializedName("filename")
    val filename: String,
    
    @SerializedName("size")
    val size: Long = 0
)

// Response from POST /v4.1/magnet/status
data class MagnetsResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("data")
    val data: MagnetsData?,
    
    @SerializedName("error")
    val error: ApiError? = null
)

data class MagnetsData(
    @SerializedName("magnets")
    val magnets: List<Magnet>
)

// Response from POST /v4/magnet/files
data class MagnetFilesResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("data")
    val data: MagnetFilesData?,
    
    @SerializedName("error")
    val error: ApiError? = null
)

data class MagnetFilesData(
    @SerializedName("magnets")
    val magnets: List<MagnetFiles>
)

data class MagnetFiles(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("files")
    val files: List<FileNode>
)

/**
 * Recursive file/folder structure from API.
 * - n: name of file or folder
 * - s: size in bytes (only for files)
 * - l: link to unlock (only for files)
 * - e: child elements (only for folders)
 */
data class FileNode(
    @SerializedName("n")
    val name: String,
    
    @SerializedName("s")
    val size: Long? = null,
    
    @SerializedName("l")
    val link: String? = null,
    
    @SerializedName("e")
    val children: List<FileNode>? = null
)

/**
 * Flattened file for UI display.
 * Created by flattenFiles() from the nested FileNode structure.
 */
data class FlatFile(
    val filename: String,
    val path: String,
    val size: Long,
    val link: String
)

// Upload response
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
    val magnets: List<UploadedMagnet>? = null,
    
    @SerializedName("files")
    val files: List<UploadedMagnet>? = null
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
