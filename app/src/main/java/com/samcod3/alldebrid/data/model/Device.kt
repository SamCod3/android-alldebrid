package com.samcod3.alldebrid.data.model

enum class DeviceType {
    KODI,
    DLNA
}

data class Device(
    val id: String,
    val name: String,
    val address: String,
    val port: Int,
    val type: DeviceType,
    val controlUrl: String? = null
) {
    val fullAddress: String
        get() = "http://$address:$port"
}
