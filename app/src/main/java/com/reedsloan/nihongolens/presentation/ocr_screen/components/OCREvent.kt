package com.reedsloan.nihongolens.presentation.ocr_screen.components

sealed class OCREvent {
    data object StartScan : OCREvent()
    data class ScanComplete(val text: String) : OCREvent()
    data object StopScan : OCREvent()
}