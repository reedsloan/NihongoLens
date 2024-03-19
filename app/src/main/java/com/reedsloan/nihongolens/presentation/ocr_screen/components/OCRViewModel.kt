package com.reedsloan.nihongolens.presentation.ocr_screen.components

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


@HiltViewModel
class OCRViewModel @Inject constructor(
    private val app: Application
) : ViewModel() {
    private val _state = MutableStateFlow(OCRState())
    val state = _state

    fun onEvent(event: OCREvent) {
        when (event) {
            is OCREvent.StartScan -> {
                _state.value = OCRState(isScanning = true)
                event.localCameraController.takePicture(
                    ContextCompat.getMainExecutor(app),
                    object : OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            super.onCaptureSuccess(image)

                            val matrix = Matrix().apply {
                                postRotate(image.imageInfo.rotationDegrees.toFloat())
                            }
                            val rotatedBitmap = Bitmap.createBitmap(
                                image.toBitmap(),
                                0,
                                0,
                                image.width,
                                image.height,
                                matrix,
                                true
                            )

                            runTextRecognition(rotatedBitmap)
                            onPhotoTaken(rotatedBitmap)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            super.onError(exception)
                            Log.e("OCRViewModel", "Could not take photo", exception)
                        }
                    }
                )
            }

            is OCREvent.StopScan -> {
                _state.value = OCRState(isScanning = false, image = null)
            }
        }
    }

    private fun onPhotoTaken(image: Bitmap) {
        _state.value = OCRState(image = image)
    }

    private fun runTextRecognition(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(
            JapaneseTextRecognizerOptions.Builder()
                .build()
        )

        recognizer.process(image)
            .addOnSuccessListener { texts ->
                _state.update { it.copy(textRecognitionResult = texts, isScanning = false) }
            }
            .addOnFailureListener { e -> // Task failed with an exception
                _state.update { it.copy(isScanning = false, error = e.message) }
                e.printStackTrace()
            }
    }
}