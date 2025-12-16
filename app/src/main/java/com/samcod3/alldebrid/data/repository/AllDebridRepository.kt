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
            // Don't follow redirects automatically - we need to catch magnet: redirects
            val client = OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .followRedirects(false) // Manual redirect handling to catch magnet links
                .followSslRedirects(false)
                .build()
            
            var currentUrl = torrentUrl
            var redirectCount = 0
            val maxRedirects = 5
            
            while (redirectCount < maxRedirects) {
                val request = Request.Builder()
                    .url(currentUrl)
                    .header("User-Agent", "Mozilla/5.0 (Android; AllDebridManager)")
                    .build()
                
                val response = client.newCall(request).execute()
                
                // Check for redirect (3xx)
                if (response.isRedirect) {
                    val location = response.header("Location")
                    response.close()
                    
                    if (location == null) {
                        return@withContext Result.failure(Exception("Redirección sin destino"))
                    }
                    
                    // Check if redirect is to a magnet link!
                    if (location.startsWith("magnet:")) {
                        Log.d(TAG, "Tracker redirected to magnet URI, uploading directly")
                        return@withContext uploadMagnetDirect(apiKey, location)
                    }
                    
                    // Follow HTTP redirect
                    currentUrl = if (location.startsWith("http")) {
                        location
                    } else {
                        // Handle relative URLs
                        val base = java.net.URL(currentUrl)
                        java.net.URL(base, location).toString()
                    }
                    redirectCount++
                    Log.d(TAG, "Following redirect #$redirectCount to: $currentUrl")
                    continue
                }
                
                // Handle non-redirect response
                return@withContext handleTorrentResponse(response, apiKey)
            }
            
            // Too many redirects
            Result.failure(Exception("Demasiadas redirecciones"))
            
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Timeout downloading torrent", e)
            Result.failure(Exception("Tiempo de espera agotado - Jackett tardó demasiado"))
        } catch (e: java.net.ConnectException) {
            Log.e(TAG, "Connection failed", e)
            Result.failure(Exception("No se pudo conectar a Jackett"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download/upload torrent", e)
            Result.failure(Exception("Error: ${e.message ?: "desconocido"}"))
        }
    }
    
    private suspend fun handleTorrentResponse(response: okhttp3.Response, apiKey: String): Result<Unit> {
        if (!response.isSuccessful) {
            Log.e(TAG, "Failed to download torrent: HTTP ${response.code}")
            val errorMsg = when (response.code) {
                404 -> "Torrent no disponible - Este tracker puede no tener el archivo"
                403 -> "Acceso denegado por el tracker"
                500, 502, 503 -> "El tracker no está disponible temporalmente"
                else -> "Error descargando torrent (HTTP ${response.code})"
            }
            response.close()
            return Result.failure(Exception(errorMsg))
        }
        
        val contentType = response.header("Content-Type") ?: ""
        Log.d(TAG, "Downloaded content type: $contentType")
        
        val torrentBytes = response.body?.bytes()
        response.close()
        
        if (torrentBytes == null || torrentBytes.isEmpty()) {
            return Result.failure(Exception("El tracker devolvió un archivo vacío"))
        }
        
        Log.d(TAG, "Downloaded torrent file: ${torrentBytes.size} bytes")
        
        // Validate that this is actually a torrent file (bencoded format starts with 'd')
        if (torrentBytes[0] != 'd'.code.toByte()) {
            val preview = String(torrentBytes.take(100).toByteArray())
            Log.e(TAG, "Invalid torrent content (not bencoded): ${preview.take(50)}")
            
            // Check if it's HTML (common error response - login page or Cloudflare)
            if (preview.contains("<html", ignoreCase = true) || 
                preview.contains("<!DOCTYPE", ignoreCase = true)) {
                return Result.failure(Exception("El tracker pide login o tiene protección Cloudflare"))
            }
            return Result.failure(Exception("Formato de torrent inválido"))
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
        
        return if (uploadResponse.isSuccessful && body?.status == "success") {
            Log.d(TAG, "Torrent file uploaded successfully")
            Result.success(Unit)
        } else {
            val error = body?.error
            checkForIpError(error?.code, error?.message)
            Result.failure(Exception(error?.message ?: "Error subiendo a AllDebrid"))
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
