package com.samcod3.alldebrid.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("username")
    val username: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("isPremium")
    val isPremium: Boolean,
    
    @SerializedName("premiumUntil")
    val premiumUntil: Long? = null,
    
    @SerializedName("lang")
    val lang: String? = null
)

data class UserResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("data")
    val data: UserData?,
    
    @SerializedName("error")
    val error: ApiError? = null
)

data class UserData(
    @SerializedName("user")
    val user: User
)
