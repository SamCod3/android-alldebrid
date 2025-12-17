package com.samcod3.alldebrid.data.model

import com.google.gson.annotations.SerializedName

data class SearchResult(
    @SerializedName("Title")
    val title: String,
    
    @SerializedName("Size")
    val size: Long? = null,
    
    @SerializedName("Seeders")
    val seeders: Int? = null,
    
    @SerializedName("Peers")
    val peers: Int? = null,
    
    @SerializedName("Link")
    val link: String? = null,
    
    @SerializedName("MagnetUri")
    val magnetUri: String? = null,
    
    @SerializedName("Tracker")
    val tracker: String? = null,
    
    @SerializedName("CategoryDesc")
    val categoryDesc: String? = null,
    
    @SerializedName("PublishDate")
    val publishDate: String? = null,
    
    // Local state - not from API
    val addedToDebrid: Boolean = false,
    val isDownloading: Boolean = false, // True if AllDebrid is downloading, false if cached/instant
    val failed: Boolean = false
)

data class JackettSearchResponse(
    @SerializedName("Results")
    val results: List<SearchResult>
)
