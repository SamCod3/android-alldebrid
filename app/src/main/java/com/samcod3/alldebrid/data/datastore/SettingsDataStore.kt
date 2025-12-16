package com.samcod3.alldebrid.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.samcod3.alldebrid.data.model.Device
import com.samcod3.alldebrid.data.model.DeviceType
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
        
        // Custom IP Range
        private val USE_CUSTOM_IP_RANGE = booleanPreferencesKey("use_custom_ip_range")
        private val CUSTOM_IP_PREFIX = stringPreferencesKey("custom_ip_prefix")
        
        // Selected Device
        private val SEL_DEV_ID = stringPreferencesKey("sel_dev_id")
        private val SEL_DEV_NAME = stringPreferencesKey("sel_dev_name")
        private val SEL_DEV_ADDRESS = stringPreferencesKey("sel_dev_address")
        private val SEL_DEV_PORT = intPreferencesKey("sel_dev_port")
        private val SEL_DEV_TYPE = stringPreferencesKey("sel_dev_type")
        private val SEL_DEV_CONTROL_URL = stringPreferencesKey("sel_dev_control_url")
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
    
    val useCustomIpRange: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_CUSTOM_IP_RANGE] ?: false
    }
    
    val customIpPrefix: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_IP_PREFIX] ?: ""
    }
    
    val selectedDevice: Flow<Device?> = context.dataStore.data.map { prefs ->
        val id = prefs[SEL_DEV_ID]
        val name = prefs[SEL_DEV_NAME]
        val address = prefs[SEL_DEV_ADDRESS]
        val port = prefs[SEL_DEV_PORT]
        val typeStr = prefs[SEL_DEV_TYPE]
        val controlUrl = prefs[SEL_DEV_CONTROL_URL]
        
        if (id != null && name != null && address != null && port != null && typeStr != null) {
            try {
                Device(
                    id = id,
                    name = name,
                    address = address,
                    port = port,
                    type = DeviceType.valueOf(typeStr),
                    controlUrl = controlUrl
                )
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
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
    
    suspend fun saveCustomIpRange(enabled: Boolean, ipPrefix: String) {
        context.dataStore.edit { preferences ->
            preferences[USE_CUSTOM_IP_RANGE] = enabled
            preferences[CUSTOM_IP_PREFIX] = ipPrefix
        }
    }
    
    suspend fun saveSelectedDevice(device: Device) {
        context.dataStore.edit { prefs ->
            prefs[SEL_DEV_ID] = device.id
            prefs[SEL_DEV_NAME] = device.name
            prefs[SEL_DEV_ADDRESS] = device.address
            prefs[SEL_DEV_PORT] = device.port
            prefs[SEL_DEV_TYPE] = device.type.name
            if (device.controlUrl != null) {
                prefs[SEL_DEV_CONTROL_URL] = device.controlUrl
            } else {
                prefs.remove(SEL_DEV_CONTROL_URL)
            }
        }
    }
    
    suspend fun clearSelectedDevice() {
        context.dataStore.edit { prefs ->
            prefs.remove(SEL_DEV_ID)
            prefs.remove(SEL_DEV_NAME)
            prefs.remove(SEL_DEV_ADDRESS)
            prefs.remove(SEL_DEV_PORT)
            prefs.remove(SEL_DEV_TYPE)
            prefs.remove(SEL_DEV_CONTROL_URL)
        }
    }
    
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
