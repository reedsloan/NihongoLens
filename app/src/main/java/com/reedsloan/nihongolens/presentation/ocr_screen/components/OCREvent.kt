package com.reedsloan.nihongolens.presentation.ocr_screen.components

import android.util.DisplayMetrics
import androidx.camera.view.LifecycleCameraController
import com.google.mlkit.vision.text.Text

sealed class OCREvent {
    data class ClickCamera(val localCameraController: LifecycleCameraController, val displayMetrics: DisplayMetrics) : OCREvent()
    data class OnClickTextBlock(val textBlock: Text.TextBlock) : OCREvent()
    data class OnClickLine(val lineId: Int) : OCREvent()
    data class OnClickWord(val word: String) : OCREvent()

    data class OnBack(val navigateBack: () -> Unit) : OCREvent()

    data object StopScan : OCREvent()
}