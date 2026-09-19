package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HfSibling(
    @Json(name = "rfilename") val rfilename: String
)

@JsonClass(generateAdapter = true)
data class HfModelDetail(
    @Json(name = "id") val id: String,
    @Json(name = "author") val author: String? = null,
    @Json(name = "downloads") val downloads: Long? = null,
    @Json(name = "likes") val likes: Int? = null,
    @Json(name = "tags") val tags: List<String>? = null,
    @Json(name = "pipeline_tag") val pipelineTag: String? = null,
    @Json(name = "siblings") val siblings: List<HfSibling>? = null,
    @Json(name = "lastModified") val lastModified: String? = null
)

@JsonClass(generateAdapter = true)
data class HfSearchItem(
    @Json(name = "id") val id: String,
    @Json(name = "author") val author: String? = null,
    @Json(name = "downloads") val downloads: Long? = null,
    @Json(name = "likes") val likes: Int? = null,
    @Json(name = "tags") val tags: List<String>? = null,
    @Json(name = "pipeline_tag") val pipelineTag: String? = null
)
