package com.reedsloan.nihongolens.presentation.ocr_screen.components

import android.graphics.Point
import com.atilika.kuromoji.ipadic.Token
import com.reedsloan.nihongolens.domain.model.JapaneseEnglishEntry

data class OCRResult(
    val text: String,
    val tokenizedText: List<Token> = emptyList(),
    val confidence: Float,
    val topLeft: Point,
    val angle: Float,
    val id: Int,
    val japaneseEnglishEntries: Map<Token, List<JapaneseEnglishEntry>> = emptyMap(),
)
