package com.reedsloan.nihongolens.presentation.ocr_screen.components

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Point
import android.util.Log
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.wanasit.kotori.Token
import com.github.wanasit.kotori.Tokenizer
import com.github.wanasit.kotori.optimized.DefaultTermFeatures
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OCRViewModel @Inject constructor(
    private val app: Application,
    private val tokenizer: Tokenizer<DefaultTermFeatures>
) : ViewModel() {
    private val _state = MutableStateFlow(OCRState())
    val state = _state

    fun onEvent(event: OCREvent) {
        when (event) {
            is OCREvent.ClickCamera -> {
                if (_state.value.ocrViewMode == OCRViewMode.Camera) {
                    _state.value = OCRState(ocrViewMode = OCRViewMode.Result, isScanning = true)
                    event.localCameraController.takePicture(ContextCompat.getMainExecutor(app),
                        object : OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                super.onCaptureSuccess(image)

                                val matrix = Matrix().apply {
                                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                                }
                                val rotatedBitmap = Bitmap.createBitmap(
                                    image.toBitmap(), 0, 0, image.width, image.height, matrix, true
                                )

                                runTextRecognition(rotatedBitmap)
                                onPhotoTaken(rotatedBitmap)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                super.onError(exception)
                                Log.e("OCRViewModel", "Could not take photo", exception)
                            }
                        })
                } else {
                    _state.value = OCRState(ocrViewMode = OCRViewMode.Camera, ocrResult = null)
                    return
                }

            }

            is OCREvent.StopScan -> {
                _state.value = OCRState(isScanning = false, image = null)
            }

            is OCREvent.OnClickLine -> {
                _state.update {
                    it.copy(
                        ocrViewMode = OCRViewMode.InspectResult(event.lineId)
                    )
                }
            }

            is OCREvent.OnClickTextBlock -> {
                tokenizeText(event.textBlock.text)
            }

            is OCREvent.OnClickWord -> {

            }
        }
    }

    private fun onPhotoTaken(image: Bitmap) {
        _state.value = OCRState(image = image)
    }


    private fun runTextRecognition(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(
            JapaneseTextRecognizerOptions.Builder().build()
        )

        recognizer.process(image).addOnSuccessListener { texts ->
            viewModelScope.launch {
                _state.update { ocrState ->
                    ocrState.copy(
                        isScanning = false,
                        ocrResult =
                        texts.textBlocks.flatMap { it.lines }.mapIndexed { index, line ->
                            OCRResult(
                                text = line.text,
                                topLeft = line.cornerPoints?.get(0)?.let { Point(it.x, it.y) }!!,
                                confidence = line.confidence,
                                tokenizedText = tokenizeText(line.text),
                                angle = line.angle,
                                id = index
                            )
                        }
                    )
                }
            }

        }.addOnFailureListener { e -> // Task failed with an exception
            _state.update { it.copy(isScanning = false, error = e.message) }
            e.printStackTrace()
            recognizer.close()
        }
    }

    private fun tokenizeText(text: String): List<Token<DefaultTermFeatures>> {
        Log.d("OCRViewModel", "Begin tokenizing text: $text at ${System.currentTimeMillis()}")
        return tokenizer.tokenize(text).map { it }

    }
}