package com.reedsloan.nihongolens.presentation.ocr_screen.components

import android.graphics.Bitmap
import android.util.DisplayMetrics
import androidx.camera.view.LifecycleCameraController
import com.google.mlkit.vision.text.Text

sealed class OCREvent {
    data class ClickCamera(val localCameraController: LifecycleCameraController, val displayMetrics: DisplayMetrics) : OCREvent()
    data class OnClickTextBlock(val textBlock: Text.TextBlock) : OCREvent()
    data class OnClickLine(val lineId: Int, val text: String) : OCREvent()
    data class OnClickWord(val word: String) : OCREvent()

    data class OnBack(val navigateBack: () -> Unit) : OCREvent()
    data class ScanImage(val image: Bitmap) : OCREvent()

    data object StopScan : OCREvent()
}