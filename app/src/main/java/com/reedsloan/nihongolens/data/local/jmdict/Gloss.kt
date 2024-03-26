package com.reedsloan.nihongolens.data.local.jmdict


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Gloss(
    @Json(name = "lang")
    val lang: String,
    @Json(name = "text")
    val text: String,
    @Json(name = "type")
    val type: String?
)