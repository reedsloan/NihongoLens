package com.reedsloan.nihongolens.presentation.ocr_screen.components

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class OCRViewModel @Inject constructor(): ViewModel() {
    private val _state = MutableStateFlow(OCRState())
    val state = _state

    fun onEvent(event: OCREvent) {
        when (event) {
            is OCREvent.StartScan -> {
                _state.value = OCRState(isScanning = true)
            }

            is OCREvent.ScanComplete -> {
                _state.value = OCRState(text = event.text, isScanning = false)
            }

            is OCREvent.StopScan -> {
                _state.value = OCRState(isScanning = false)
            }
        }
    }
}