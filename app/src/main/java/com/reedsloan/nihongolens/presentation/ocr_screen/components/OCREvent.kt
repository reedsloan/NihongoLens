package com.reedsloan.nihongolens.presentation.ocr_screen.components

import android.util.DisplayMetrics
import androidx.camera.view.LifecycleCameraController
import com.github.wanasit.kotori.Token
import com.github.wanasit.kotori.optimized.DefaultTermFeatures
import com.google.mlkit.vision.text.Text

sealed class OCREvent {
    data class ClickCamera(val localCameraController: LifecycleCameraController, val displayMetrics: DisplayMetrics) : OCREvent()
    data class OnClickTextBlock(val textBlock: Text.TextBlock) : OCREvent()
    data class OnClickLine(val lineId: Int) : OCREvent()
    data class OnClickWord(val word: Token<DefaultTermFeatures>) : OCREvent()

    data object StopScan : OCREvent()
}