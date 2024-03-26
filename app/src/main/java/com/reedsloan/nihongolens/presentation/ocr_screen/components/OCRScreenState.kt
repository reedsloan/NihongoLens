package com.reedsloan.nihongolens.presentation.ocr_screen.components

import android.graphics.Bitmap

data class OCRScreenState(
    val ocrResults: List<OCRResult>? = null,
    val isScanning: Boolean = false,
    val image: Bitmap? = null,
    val error: String? = null,
    val textStructure: TextStructure = TextStructure.BLOCK,
    val ocrViewMode: OCRViewMode = OCRViewMode.Camera,
    val selectedOcrResultId: Int? = null,
    val dictionaryIsLoading: Boolean = false,
    val tokenizerLoading: Boolean = false,
)

enum class TextStructure {
    WORD,
    LINE,
    BLOCK,
}

sealed class OCRViewMode {
    data object Camera : OCRViewMode()
    data object Result : OCRViewMode()

    data class InspectResult(val id: Int) : OCRViewMode()
}