package com.samcod3.alldebrid.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private val API_KEY = stringPreferencesKey("api_key")
        private val JACKETT_URL = stringPreferencesKey("jackett_url")
        private val JACKETT_API_KEY = stringPreferencesKey("jackett_api_key")
        private val SELECTED_DEVICE_ID = stringPreferencesKey("selected_device_id")
        private val USE_CUSTOM_IP_RANGE = booleanPreferencesKey("use_custom_ip_range")
        private val CUSTOM_IP_PREFIX = stringPreferencesKey("custom_ip_prefix")
    }
    
    val apiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[API_KEY] ?: ""
    }
    
    val jackettUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[JACKETT_URL] ?: ""
    }
    
    val jackettApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[JACKETT_API_KEY] ?: ""
    }
    
    val selectedDeviceId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_DEVICE_ID]
    }
    
    val useCustomIpRange: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_CUSTOM_IP_RANGE] ?: false
    }
    
    val customIpPrefix: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_IP_PREFIX] ?: ""
    }
    
    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = apiKey
        }
    }
    
    suspend fun saveJackettConfig(url: String, apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[JACKETT_URL] = url
            preferences[JACKETT_API_KEY] = apiKey
        }
    }
    
    suspend fun saveSelectedDeviceId(deviceId: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_DEVICE_ID] = deviceId
        }
    }
    
    suspend fun saveCustomIpRange(enabled: Boolean, ipPrefix: String) {
        context.dataStore.edit { preferences ->
            preferences[USE_CUSTOM_IP_RANGE] = enabled
            preferences[CUSTOM_IP_PREFIX] = ipPrefix
        }
    }
    
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
