package com.samcod3.alldebrid.data.repository

import android.util.Log
import com.samcod3.alldebrid.data.api.AllDebridApi
import com.samcod3.alldebrid.data.datastore.SettingsDataStore
import com.samcod3.alldebrid.data.model.AllDebridError
import com.samcod3.alldebrid.data.model.FileNode
import com.samcod3.alldebrid.data.model.FlatFile
import com.samcod3.alldebrid.data.model.Link
import com.samcod3.alldebrid.data.model.Magnet
import com.samcod3.alldebrid.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    private val settingsDataStore: SettingsDataStore,
    private val httpClient: OkHttpClient
) {
    
    companion object {
        private const val TAG = "AllDebridRepository"
    }

    private suspend fun <T> retryWithBackoff(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 500L,
        factor: Double = 2.0,
        block: suspend () -> Result<T>
    ): Result<T> {
        var lastException: Throwable? = null
        repeat(maxAttempts) { attempt ->
            val result = block()
            if (result.isSuccess) return result

            lastException = result.exceptionOrNull()

            // No reintentar para errores de autorización
            if (lastException is IpAuthorizationRequiredException) {
                return result
            }

            // No reintentar para rate limit
            if (lastException?.message?.contains("rate", ignoreCase = true) == true) {
                return result
            }

            if (attempt < maxAttempts - 1) {
                val delay = (initialDelayMs * Math.pow(factor, attempt.toDouble())).toLong()
                Log.d(TAG, "Retry attempt ${attempt + 1}/$maxAttempts, waiting ${delay}ms")
                kotlinx.coroutines.delay(delay)
            }
        }
        return Result.failure(lastException ?: Exception("Max retries exceeded"))
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

    private inline fun <T> safeApiCall(
        tag: String? = null,
        message: String? = null,
        block: () -> Result<T>
    ): Result<T> {
        return try {
            block()
        } catch (e: IpAuthorizationRequiredException) {
            Result.failure(e)
        } catch (e: Exception) {
            if (tag != null) Log.e(tag, message ?: "API call failed", e)
            Result.failure(e)
        }
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
    
    suspend fun validateApiKey(apiKey: String): Result<User> = safeApiCall {
        val response = api.getUser(apiKey = apiKey)
        val body = response.body()

        if (response.isSuccessful && body?.status == "success") {
            body.data?.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Invalid response"))
        } else {
            val error = body?.error
            checkForIpError(error?.code, error?.message)
            Result.failure(Exception("API error: ${error?.message ?: response.code()}"))
        }
    }
    
    suspend fun getMagnets(): Result<List<Magnet>> = safeApiCall {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@safeApiCall Result.failure(Exception("No API key configured"))
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
    }
    
    /**
     * Get files for a magnet using the new /v4/magnet/files endpoint.
     * In API v4.1, files are no longer included in magnet/status response.
     */
    suspend fun getMagnetFiles(id: Long): Result<List<FlatFile>> = safeApiCall(TAG, "Failed to get magnet files") {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@safeApiCall Result.failure(Exception("No API key configured"))
        }

        val response = api.getMagnetFiles(apiKey = apiKey, id = id)
        val body = response.body()

        if (response.isSuccessful && body?.status == "success") {
            val magnetFiles = body.data?.magnets?.firstOrNull()
            val flatFiles = magnetFiles?.files?.let { flattenFiles(it) } ?: emptyList()
            Log.d(TAG, "Got ${flatFiles.size} files for magnet $id")
            Result.success(flatFiles)
        } else {
            val error = body?.error
            checkForIpError(error?.code, error?.message)
            Result.failure(Exception(error?.message ?: "Failed to get files"))
        }
    }
    
    /**
     * Flatten the nested file structure from API into a flat list.
     * Handles recursive folder structures.
     */
    private fun flattenFiles(files: List<FileNode>, parentPath: String = ""): List<FlatFile> {
        val result = mutableListOf<FlatFile>()
        
        for (item in files) {
            val fullPath = if (parentPath.isEmpty()) item.name else "$parentPath/${item.name}"
            
            if (item.children != null) {
                // It's a folder - recurse into children
                result.addAll(flattenFiles(item.children, fullPath))
            } else if (item.link != null) {
                // It's a file with a link
                result.add(FlatFile(
                    filename = item.name,
                    path = fullPath,
                    size = item.size ?: 0,
                    link = item.link
                ))
            }
        }
        return result
    }
    
    private fun extractMagnetFromString(input: String): String? {
        if (input.startsWith("magnet:")) {
            val cleaned = input.substringBefore(' ').substringBefore('\n')
            return if (cleaned.contains("xt=urn:btih:")) cleaned else null
        }
        val htmlRegex = """href=["'](magnet:[^"']+)["']""".toRegex()
        htmlRegex.find(input)?.groupValues?.get(1)?.let { return it }
        val paramRegex = """[?&]magnet=([^&]+)""".toRegex()
        paramRegex.find(input)?.groupValues?.get(1)?.let {
            return java.net.URLDecoder.decode(it, "UTF-8")
        }
        return null
    }

    private fun extractMagnetFromError(errorMessage: String): String? {
        val regex = """magnet:\?xt=urn:btih:[a-zA-Z0-9]{32,40}[^\s<>"]*""".toRegex()
        return regex.find(errorMessage)?.value?.also {
            Log.i(TAG, "Magnet extracted from error message")
        }
    }

    private fun extractMagnetWithFallback(input: String): String {
        // Solo parse directo - redirects HTTP se manejan en downloadAndUploadTorrent()
        return extractMagnetFromString(input) ?: input
    }

    /**
     * Upload a link to AllDebrid - handles magnets, remote URLs, and local torrent files
     * Returns: true = cached (instant), false = downloading
     */
    suspend fun uploadLink(link: String): Result<Boolean> {
        return retryWithBackoff(maxAttempts = 3) {
            uploadLinkInternal(link)
        }
    }

    private suspend fun uploadLinkInternal(link: String): Result<Boolean> = safeApiCall(TAG, "Upload failed") {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@safeApiCall Result.failure(Exception("No API key configured"))
        }

        val processedLink = extractMagnetWithFallback(link)
        if (processedLink != link) {
            Log.i(TAG, "Processed link: magnet extracted")
        }

        // CASE A: Magnet link - send directly
        if (processedLink.startsWith("magnet:")) {
            Log.d(TAG, "Uploading magnet link directly")
            return@safeApiCall uploadMagnetDirect(apiKey, processedLink)
        }

        // CASE B: HTTP/HTTPS URL (torrent file)
        if (processedLink.startsWith("http://") || processedLink.startsWith("https://")) {
            if (isLocalUrl(processedLink)) {
                Log.d(TAG, "Local URL detected, downloading torrent file first")
                return@safeApiCall downloadAndUploadTorrent(apiKey, processedLink)
            } else {
                Log.d(TAG, "Remote URL, trying direct upload first")
                val result = uploadMagnetDirect(apiKey, processedLink)
                if (result.isSuccess) {
                    return@safeApiCall result
                }
                Log.d(TAG, "Direct upload failed, trying download and upload")
                return@safeApiCall downloadAndUploadTorrent(apiKey, processedLink)
            }
        }

        // Unknown format, try direct upload
        uploadMagnetDirect(apiKey, processedLink)
    }
    
    /**
     * Upload magnet directly. Returns true if cached (instant), false if downloading
     */
    private suspend fun uploadMagnetDirect(apiKey: String, magnet: String): Result<Boolean> {
        var currentMagnet = magnet
        val maxAttempts = 3

        repeat(maxAttempts) { attempt ->
            val response = api.uploadMagnet(apiKey = apiKey, magnet = currentMagnet)
            val body = response.body()

            if (response.isSuccessful && body?.status == "success") {
                val uploadedMagnet = body.data?.magnets?.firstOrNull() ?: body.data?.files?.firstOrNull()
                return Result.success(uploadedMagnet?.ready ?: false)
            }

            val error = body?.error
            checkForIpError(error?.code, error?.message)

            val extracted = error?.message?.let { extractMagnetFromError(it) }
            if (extracted != null && extracted != currentMagnet) {
                Log.i(TAG, "Retrying with extracted magnet (attempt ${attempt + 1}/$maxAttempts)")
                currentMagnet = extracted
            } else {
                return Result.failure(Exception(error?.message ?: "Upload failed"))
            }
        }
        return Result.failure(Exception("Upload failed after $maxAttempts attempts"))
    }
    
    private suspend fun downloadAndUploadTorrent(apiKey: String, torrentUrl: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Use injected client with custom redirect behavior
            // newBuilder() reuses connection pool while allowing config changes
            val client = httpClient.newBuilder()
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
    
    private suspend fun handleTorrentResponse(response: okhttp3.Response, apiKey: String): Result<Boolean> {
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
            // Check if the magnet is ready (cached) or needs downloading
            val uploadedMagnet = body.data?.magnets?.firstOrNull() ?: body.data?.files?.firstOrNull()
            val isReady = uploadedMagnet?.ready ?: false
            Result.success(isReady)
        } else {
            val error = body?.error
            checkForIpError(error?.code, error?.message)
            Result.failure(Exception(error?.message ?: "Error subiendo a AllDebrid"))
        }
    }
    
    // Keep legacy name for compatibility
    suspend fun uploadMagnet(magnet: String): Result<Boolean> = uploadLink(magnet)
    
    suspend fun deleteMagnet(id: Long): Result<Unit> = safeApiCall {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@safeApiCall Result.failure(Exception("No API key configured"))
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
    }
    
    suspend fun unlockLink(link: String): Result<Link> {
        return retryWithBackoff(maxAttempts = 2) {
            unlockLinkInternal(link)
        }
    }

    private suspend fun unlockLinkInternal(link: String): Result<Link> = safeApiCall {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@safeApiCall Result.failure(Exception("No API key configured"))
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
    }
}
