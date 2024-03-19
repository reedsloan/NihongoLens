package com.reedsloan.nihongolens.presentation.ocr_screen.components

import android.graphics.Bitmap
import com.google.mlkit.vision.text.Text

data class OCRState(
    val textRecognitionResult: Text? = null,
    val isScanning: Boolean = false,
    val image: Bitmap? = null,
    val error: String? = null
)
