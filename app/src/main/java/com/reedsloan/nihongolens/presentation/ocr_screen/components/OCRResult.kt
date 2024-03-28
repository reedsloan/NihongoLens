package com.reedsloan.nihongolens.presentation.ocr_screen.components

import android.graphics.Point
import com.github.wanasit.kotori.Token
import com.github.wanasit.kotori.optimized.DefaultTermFeatures
import com.reedsloan.nihongolens.domain.model.JapaneseEnglishEntry

data class OCRResult(
    val text: String,
    val tokenizedText: List<Token<DefaultTermFeatures>> = emptyList(),
    val confidence: Float,
    val topLeft: Point,
    val angle: Float,
    val id: Int,
    val japaneseEnglishEntries: Map<String, List<JapaneseEnglishEntry>> = emptyMap(),
)
