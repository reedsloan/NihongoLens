package com.reedsloan.nihongolens.data.local.jmdict


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Word(
//    @Json(name = "id")
//    val id: String,
    @Json(name = "kana")
    val kana: List<Kana>,
    @Json(name = "kanji")
    val kanji: List<Kanji>,
    @Json(name = "sense")
    val sense: List<Sense>
)