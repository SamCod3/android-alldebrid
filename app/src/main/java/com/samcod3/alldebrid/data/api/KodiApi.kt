package com.samcod3.alldebrid.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface KodiApi {
    
    @POST
    suspend fun sendCommand(
        @Url url: String,
        @Body request: KodiRequest
    ): Response<KodiResponse>
}

data class KodiRequest(
    val jsonrpc: String = "2.0",
    val method: String,
    val params: Map<String, Any>? = null,
    val id: Int = 1
)

data class KodiResponse(
    val jsonrpc: String,
    val id: Int,
    val result: Any? = null,
    val error: KodiError? = null
)

data class KodiError(
    val code: Int,
    val message: String
)

// Extension functions for common Kodi operations
object KodiCommands {
    fun ping() = KodiRequest(method = "JSONRPC.Ping")
    
    fun playUrl(url: String) = KodiRequest(
        method = "Player.Open",
        params = mapOf("item" to mapOf("file" to url))
    )
    
    fun stop(playerId: Int = 1) = KodiRequest(
        method = "Player.Stop",
        params = mapOf("playerid" to playerId)
    )
    
    fun pause(playerId: Int = 1) = KodiRequest(
        method = "Player.PlayPause",
        params = mapOf("playerid" to playerId)
    )
    
    fun getActivePlayers() = KodiRequest(method = "Player.GetActivePlayers")
}
