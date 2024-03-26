package com.reedsloan.nihongolens.data.local.jmdict


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Kanji(
    @Json(name = "common")
    val common: Boolean,
    @Json(name = "tags")
    val tags: List<String>,
    @Json(name = "text")
    val text: String
)