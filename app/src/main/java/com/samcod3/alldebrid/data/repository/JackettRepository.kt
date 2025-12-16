package com.samcod3.alldebrid.data.repository

import com.samcod3.alldebrid.data.api.JackettApi
import com.samcod3.alldebrid.data.datastore.SettingsDataStore
import com.samcod3.alldebrid.data.model.SearchResult
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JackettRepository @Inject constructor(
    private val api: JackettApi,
    private val settingsDataStore: SettingsDataStore
) {
    
    suspend fun search(query: String): Result<List<SearchResult>> {
        return try {
            val jackettUrl = settingsDataStore.jackettUrl.first()
            val jackettApiKey = settingsDataStore.jackettApiKey.first()
            
            if (jackettUrl.isBlank() || jackettApiKey.isBlank()) {
                return Result.failure(Exception("Jackett not configured"))
            }
            
            val searchUrl = "${jackettUrl.trimEnd('/')}/api/v2.0/indexers/all/results"
            
            val response = api.search(
                baseUrl = searchUrl,
                apiKey = jackettApiKey,
                query = query
            )
            
            if (response.isSuccessful) {
                val results = response.body()?.results ?: emptyList()
                Result.success(results)
            } else {
                Result.failure(Exception("Search failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun isConfigured(): Boolean {
        val url = settingsDataStore.jackettUrl.first()
        val apiKey = settingsDataStore.jackettApiKey.first()
        return url.isNotBlank() && apiKey.isNotBlank()
    }
}
