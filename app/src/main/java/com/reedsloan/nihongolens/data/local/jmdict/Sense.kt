package com.reedsloan.nihongolens.data.local.jmdict


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Sense(
    @Json(name = "antonym")
    val antonym: List<List<String>>,
    @Json(name = "appliesToKana")
    val appliesToKana: List<String>,
    @Json(name = "appliesToKanji")
    val appliesToKanji: List<String>,
    @Json(name = "dialect")
    val dialect: List<String>,
    @Json(name = "field")
    val field: List<String>,
    @Json(name = "gloss")
    val gloss: List<Gloss>,
    @Json(name = "info")
    val info: List<String>,
    @Json(name = "languageSource")
    val languageSource: List<Any>,
    @Json(name = "misc")
    // contains tags such as uk for "usually kana" and obs for "obsolete"
    val misc: List<String>,
    @Json(name = "partOfSpeech")
    val partOfSpeech: List<String>,
    @Json(name = "related")
    val related: List<List<String>>
)