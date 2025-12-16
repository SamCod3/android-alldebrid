package com.samcod3.alldebrid.data.repository

import android.util.Log
import com.samcod3.alldebrid.data.api.AllDebridApi
import com.samcod3.alldebrid.data.datastore.SettingsDataStore
import com.samcod3.alldebrid.data.model.AllDebridError
import com.samcod3.alldebrid.data.model.Link
import com.samcod3.alldebrid.data.model.Magnet
import com.samcod3.alldebrid.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Custom exception for IP authorization required errors
 */
class IpAuthorizationRequiredException(
    val errorCode: String,
    override val message: String = "IP authorization required. Please authorize this IP address."
) : Exception(message)

@Singleton
class AllDebridRepository @Inject constructor(
    private val api: AllDebridApi,
    private val settingsDataStore: SettingsDataStore
) {
    
    companion object {
        private const val TAG = "AllDebridRepository"
    }
    
    private suspend fun getApiKey(): String {
        return settingsDataStore.apiKey.first()
    }
    
    /**
     * Check if the error requires IP authorization
     */
    private fun checkForIpError(errorCode: String?, errorMessage: String?): Nothing? {
        if (errorCode != null && AllDebridError.requiresIpAuthorization(errorCode)) {
            throw IpAuthorizationRequiredException(
                errorCode = errorCode,
                message = errorMessage ?: "IP authorization required"
            )
        }
        return null
    }
    
    /**
     * Check if the URL is a local address (Jackett usually gives these)
     */
    private fun isLocalUrl(url: String): Boolean {
        return url.contains("127.0.0.1") ||
               url.contains("localhost") ||
               url.contains("192.168.") ||
               url.contains("10.0.") ||
               url.contains("172.16.")
    }
    
    suspend fun validateApiKey(apiKey: String): Result<User> {
        return try {
            val response = api.getUser(apiKey = apiKey)
            val body = response.body()
            
            if (response.isSuccessful && body?.status == "success") {
                body.data?.user?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Invalid response"))
            } else {
                // Check for IP authorization errors
                val error = body?.error
                checkForIpError(error?.code, error?.message)
                Result.failure(Exception("API error: ${error?.message ?: response.code()}"))
            }
        } catch (e: IpAuthorizationRequiredException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMagnets(): Result<List<Magnet>> {
        return try {
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("No API key configured"))
            }
            
            val response = api.getMagnets(apiKey = apiKey)
            val body = response.body()
            
            if (response.isSuccessful && body?.status == "success") {
                val magnets = body.data?.magnets ?: emptyList()
                Result.success(magnets)
            } else {
                val error = body?.error
                checkForIpError(error?.code, error?.message)
                Result.failure(Exception("API error: ${error?.message ?: response.code()}"))
            }
        } catch (e: IpAuthorizationRequiredException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Upload a link to AllDebrid - handles magnets, remote URLs, and local torrent files
     */
    suspend fun uploadLink(link: String): Result<Unit> {
        return try {
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("No API key configured"))
            }
            
            // CASE A: Magnet link - send directly
            if (link.startsWith("magnet:")) {
                Log.d(TAG, "Uploading magnet link directly")
                return uploadMagnetDirect(apiKey, link)
            }
            
            // CASE B: HTTP/HTTPS URL (torrent file)
            if (link.startsWith("http://") || link.startsWith("https://")) {
                // Check if local URL
                if (isLocalUrl(link)) {
                    Log.d(TAG, "Local URL detected, downloading torrent file first")
                    return downloadAndUploadTorrent(apiKey, link)
                } else {
                    // Try sending URL directly first
                    Log.d(TAG, "Remote URL, trying direct upload first")
                    val result = uploadMagnetDirect(apiKey, link)
                    if (result.isSuccess) {
                        return result
                    }
                    // If direct upload fails, try downloading and uploading
                    Log.d(TAG, "Direct upload failed, trying download and upload")
                    return downloadAndUploadTorrent(apiKey, link)
                }
            }
            
            // Unknown format, try direct upload
            uploadMagnetDirect(apiKey, link)
            
        } catch (e: IpAuthorizationRequiredException) {
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed", e)
            Result.failure(e)
        }
    }
    
    private suspend fun uploadMagnetDirect(apiKey: String, magnet: String): Result<Unit> {
        val response = api.uploadMagnet(apiKey = apiKey, magnet = magnet)
        val body = response.body()
        
        return if (response.isSuccessful && body?.status == "success") {
            Result.success(Unit)
        } else {
            val error = body?.error
            checkForIpError(error?.code, error?.message)
            Result.failure(Exception(error?.message ?: "Upload failed"))
        }
    }
    
    private suspend fun downloadAndUploadTorrent(apiKey: String, torrentUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Download the torrent file
            val client = OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
            
            val request = Request.Builder()
                .url(torrentUrl)
                .header("User-Agent", "Mozilla/5.0 (Android; AllDebridManager)")
                .build()
            
            val downloadResponse = client.newCall(request).execute()
            
            if (!downloadResponse.isSuccessful) {
                Log.e(TAG, "Failed to download torrent: HTTP ${downloadResponse.code}")
                return@withContext Result.failure(Exception("Failed to download torrent: ${downloadResponse.code}"))
            }
            
            val contentType = downloadResponse.header("Content-Type") ?: ""
            Log.d(TAG, "Downloaded content type: $contentType")
            
            val torrentBytes = downloadResponse.body?.bytes()
                ?: return@withContext Result.failure(Exception("Empty torrent file"))
            
            Log.d(TAG, "Downloaded torrent file: ${torrentBytes.size} bytes")
            
            // Validate that this is actually a torrent file (bencoded format starts with 'd')
            if (torrentBytes.isEmpty() || torrentBytes[0] != 'd'.code.toByte()) {
                val preview = String(torrentBytes.take(100).toByteArray())
                Log.e(TAG, "Invalid torrent content (not bencoded): ${preview.take(50)}")
                
                // Check if it's HTML (common error response)
                if (preview.contains("<html", ignoreCase = true) || 
                    preview.contains("<!DOCTYPE", ignoreCase = true)) {
                    return@withContext Result.failure(Exception("Tracker returned HTML instead of torrent file"))
                }
                return@withContext Result.failure(Exception("Invalid torrent file format"))
            }
            
            // Upload the torrent file to AllDebrid
            val requestBody = torrentBytes.toRequestBody("application/x-bittorrent".toMediaType())
            val filePart = MultipartBody.Part.createFormData(
                "files[]",
                "upload.torrent",
                requestBody
            )
            
            val uploadResponse = api.uploadTorrentFile(apiKey = apiKey, file = filePart)
            val body = uploadResponse.body()
            
            if (uploadResponse.isSuccessful && body?.status == "success") {
                Log.d(TAG, "Torrent file uploaded successfully")
                Result.success(Unit)
            } else {
                val error = body?.error
                checkForIpError(error?.code, error?.message)
                Result.failure(Exception(error?.message ?: "Upload failed"))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Timeout downloading torrent", e)
            Result.failure(Exception("Timeout downloading torrent file"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download/upload torrent", e)
            Result.failure(e)
        }
    }
    
    // Keep legacy name for compatibility
    suspend fun uploadMagnet(magnet: String): Result<Unit> = uploadLink(magnet)
    
    suspend fun deleteMagnet(id: Long): Result<Unit> {
        return try {
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("No API key configured"))
            }
            
            val response = api.deleteMagnet(apiKey = apiKey, id = id)
            val body = response.body()
            
            if (response.isSuccessful && body?.status == "success") {
                Result.success(Unit)
            } else {
                val error = body?.error
                checkForIpError(error?.code, error?.message)
                Result.failure(Exception("Delete failed"))
            }
        } catch (e: IpAuthorizationRequiredException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun unlockLink(link: String): Result<Link> {
        return try {
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("No API key configured"))
            }
            
            val response = api.unlockLink(apiKey = apiKey, link = link)
            val body = response.body()
            
            if (response.isSuccessful && body?.status == "success") {
                body.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Invalid response"))
            } else {
                val error = body?.error
                checkForIpError(error?.code, error?.message)
                Result.failure(Exception(error?.message ?: "Unlock failed"))
            }
        } catch (e: IpAuthorizationRequiredException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
