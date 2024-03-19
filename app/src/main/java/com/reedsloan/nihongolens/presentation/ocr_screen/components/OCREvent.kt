package com.reedsloan.nihongolens.presentation.ocr_screen.components

import android.util.DisplayMetrics
import androidx.camera.view.LifecycleCameraController
import com.google.mlkit.vision.text.Text

sealed class OCREvent {
    data class StartScan(val localCameraController: LifecycleCameraController, val displayMetrics: DisplayMetrics) : OCREvent()
    data object StopScan : OCREvent()
}