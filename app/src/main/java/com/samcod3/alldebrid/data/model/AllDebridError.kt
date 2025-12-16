package com.samcod3.alldebrid.data.model

/**
 * Represents AllDebrid API error responses
 */
sealed class AllDebridError(val code: String, val message: String) {
    
    // Authentication errors
    object MissingApiKey : AllDebridError("AUTH_MISSING_APIKEY", "The auth apikey was not sent")
    object BadApiKey : AllDebridError("AUTH_BAD_APIKEY", "The auth apikey is invalid")
    object Blocked : AllDebridError("AUTH_BLOCKED", "This apikey is geo-blocked or ip-blocked")
    object UserBanned : AllDebridError("AUTH_USER_BANNED", "This account is banned")
    
    // VPN/Server errors
    object NoServer : AllDebridError("NO_SERVER", "Servers are not allowed to use this feature. VPN detected.")
    
    // Generic
    object Maintenance : AllDebridError("MAINTENANCE", "AllDebrid is under maintenance")
    class Unknown(code: String, message: String) : AllDebridError(code, message)
    
    companion object {
        /**
         * Errors that require IP authorization
         */
        val IP_AUTHORIZATION_REQUIRED = listOf("AUTH_BLOCKED", "NO_SERVER")
        
        /**
         * URL to authorize VPN/new IPs
         */
        const val VPN_AUTHORIZATION_URL = "https://alldebrid.com/vpn/"
        
        fun fromCode(code: String, message: String = ""): AllDebridError {
            return when (code) {
                "AUTH_MISSING_APIKEY" -> MissingApiKey
                "AUTH_BAD_APIKEY" -> BadApiKey
                "AUTH_BLOCKED" -> Blocked
                "AUTH_USER_BANNED" -> UserBanned
                "NO_SERVER" -> NoServer
                "MAINTENANCE" -> Maintenance
                else -> Unknown(code, message)
            }
        }
        
        fun requiresIpAuthorization(code: String): Boolean {
            return code in IP_AUTHORIZATION_REQUIRED
        }
    }
}
