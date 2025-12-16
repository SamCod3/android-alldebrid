package com.samcod3.alldebrid.data.repository

import com.samcod3.alldebrid.data.api.AllDebridApi
import com.samcod3.alldebrid.data.datastore.SettingsDataStore
import com.samcod3.alldebrid.data.model.Link
import com.samcod3.alldebrid.data.model.Magnet
import com.samcod3.alldebrid.data.model.User
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AllDebridRepository @Inject constructor(
    private val api: AllDebridApi,
    private val settingsDataStore: SettingsDataStore
) {
    
    private suspend fun getApiKey(): String {
        return settingsDataStore.apiKey.first()
    }
    
    suspend fun validateApiKey(apiKey: String): Result<User> {
        return try {
            val response = api.getUser(apiKey = apiKey)
            if (response.isSuccessful && response.body()?.status == "success") {
                response.body()?.data?.user?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Invalid response"))
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
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
            if (response.isSuccessful && response.body()?.status == "success") {
                val magnets = response.body()?.data?.magnets ?: emptyList()
                Result.success(magnets)
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
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
            if (response.isSuccessful && response.body()?.status == "success") {
                Result.success(Unit)
            } else {
                val error = response.body()?.error?.message ?: "Upload failed"
                Result.failure(Exception(error))
            }
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
            if (response.isSuccessful && response.body()?.status == "success") {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Delete failed"))
            }
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
            if (response.isSuccessful && response.body()?.status == "success") {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Invalid response"))
            } else {
                val error = response.body()?.error?.message ?: "Unlock failed"
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
