package com.samcod3.alldebrid.data.api

import android.util.Log
import android.webkit.CookieManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API for managing AllDebrid API keys using dashboard session cookies.
 * This replicates the DashboardAPI from the Chrome extension.
 */
@Singleton
class DashboardApi @Inject constructor() {
    
    companion object {
        private const val TAG = "DashboardApi"
        private const val BASE_URL = "https://alldebrid.com"
        private const val APIKEYS_URL = "$BASE_URL/apikeys"
    }
    
    /**
     * Check if user is logged in by checking for session cookies
     */
    fun isLoggedIn(): Boolean {
        val cookies = CookieManager.getInstance().getCookie(BASE_URL)
        return cookies?.contains("uid=") == true
    }

    /**
     * Clear all AllDebrid cookies to force re-login
     */
    fun clearCookies() {
        val cookieManager = CookieManager.getInstance()
        // Remove all cookies (most reliable method)
        cookieManager.removeAllCookies { success ->
            Log.d(TAG, "Cookies removed: $success")
        }
        cookieManager.flush()
    }
    
    /**
     * Fetch all API keys from the dashboard
     */
    suspend fun fetchKeys(): Result<List<ApiKeyData>> = withContext(Dispatchers.IO) {
        try {
            val cookies = CookieManager.getInstance().getCookie(BASE_URL)
            if (cookies.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("Not logged in"))
            }
            
            val url = URL(APIKEYS_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Cookie", cookies)
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) AllDebridManager/1.0")
                connectTimeout = 10000
                readTimeout = 10000
            }
            
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(Exception("HTTP error: $responseCode"))
            }
            
            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()
            
            // Check if logged in
            if (responseText.contains("name=\"login\"")) {
                return@withContext Result.failure(Exception("Not logged in"))
            }
            
            // Parse keys from JavaScript variable: var keys = [...];
            val keysRegex = Regex("""var keys = (\[[\s\S]*?\]);""")
            val match = keysRegex.find(responseText)
            
            if (match == null) {
                Log.w(TAG, "Could not find keys variable in response")
                return@withContext Result.success(emptyList())
            }
            
            val keysJson = match.groupValues[1]
            val keys = mutableListOf<ApiKeyData>()
            
            try {
                val jsonArray = JSONArray(keysJson)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    keys.add(
                        ApiKeyData(
                            name = obj.optString("name", "Unnamed"),
                            apikey = obj.getString("apikey")
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing keys JSON", e)
                return@withContext Result.failure(Exception("Error parsing keys: ${e.message}"))
            }
            
            Result.success(keys)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching keys", e)
            Result.failure(e)
        }
    }
    
    /**
     * Create a new API key
     */
    suspend fun createKey(name: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cookies = CookieManager.getInstance().getCookie(BASE_URL)
            if (cookies.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("Not logged in"))
            }
            
            val url = URL("$APIKEYS_URL/")
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Cookie", cookies)
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) AllDebridManager/1.0")
                connectTimeout = 10000
                readTimeout = 10000
            }
            
            // Send form data
            val postData = "name=${URLEncoder.encode(name, "UTF-8")}"
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(postData)
                writer.flush()
            }
            
            val responseCode = connection.responseCode
            connection.disconnect()
            
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to create key: HTTP $responseCode"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating key", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete an API key
     */
    suspend fun deleteKey(apikey: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cookies = CookieManager.getInstance().getCookie(BASE_URL)
            if (cookies.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("Not logged in"))
            }
            
            val url = URL("$APIKEYS_URL/")
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Cookie", cookies)
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) AllDebridManager/1.0")
                connectTimeout = 10000
                readTimeout = 10000
            }
            
            // Send form data with delete parameter
            val postData = "delete=${URLEncoder.encode(apikey, "UTF-8")}"
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(postData)
                writer.flush()
            }
            
            val responseCode = connection.responseCode
            connection.disconnect()
            
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete key: HTTP $responseCode"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting key", e)
            Result.failure(e)
        }
    }
    
    /**
     * Rename an API key
     */
    suspend fun renameKey(apikey: String, newName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cookies = CookieManager.getInstance().getCookie(BASE_URL)
            if (cookies.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("Not logged in"))
            }
            
            val encodedKey = URLEncoder.encode(apikey, "UTF-8")
            val encodedName = URLEncoder.encode(newName, "UTF-8")
            val url = URL("$APIKEYS_URL?apikey=$encodedKey&newName=$encodedName")
            
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Cookie", cookies)
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) AllDebridManager/1.0")
                connectTimeout = 10000
                readTimeout = 10000
            }
            
            val responseCode = connection.responseCode
            val responseText = if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else ""
            connection.disconnect()
            
            if (responseText == "Updated") {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to rename: $responseText"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error renaming key", e)
            Result.failure(e)
        }
    }
}

data class ApiKeyData(
    val name: String,
    val apikey: String
)
