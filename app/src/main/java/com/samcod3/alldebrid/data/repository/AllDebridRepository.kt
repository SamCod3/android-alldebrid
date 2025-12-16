package com.samcod3.alldebrid.data.repository

import com.samcod3.alldebrid.data.api.AllDebridApi
import com.samcod3.alldebrid.data.datastore.SettingsDataStore
import com.samcod3.alldebrid.data.model.AllDebridError
import com.samcod3.alldebrid.data.model.Link
import com.samcod3.alldebrid.data.model.Magnet
import com.samcod3.alldebrid.data.model.User
import kotlinx.coroutines.flow.first
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
    
    suspend fun uploadMagnet(magnet: String): Result<Unit> {
        return try {
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("No API key configured"))
            }
            
            val response = api.uploadMagnet(apiKey = apiKey, magnet = magnet)
            val body = response.body()
            
            if (response.isSuccessful && body?.status == "success") {
                Result.success(Unit)
            } else {
                val error = body?.error
                checkForIpError(error?.code, error?.message)
                Result.failure(Exception(error?.message ?: "Upload failed"))
            }
        } catch (e: IpAuthorizationRequiredException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
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
