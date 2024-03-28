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
import com.reedsloan.nihongolens.domain.model.JapaneseEnglishDictionary
import com.reedsloan.nihongolens.domain.model.JapaneseEnglishEntry
import com.reedsloan.nihongolens.domain.use_case.GetDictionary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class OCRViewModel @Inject constructor(
    private val app: Application,
    private val getDictionary: GetDictionary
) : ViewModel() {
    private val _state = MutableStateFlow(OCRScreenState())
    val state = _state
    private lateinit var dictionary: JapaneseEnglishDictionary
    private val dictionaryJob = viewModelScope.launch {
        _state.update { it.copy(dictionaryIsLoading = true) }
        dictionary = getDictionary()
        _state.update { it.copy(dictionaryIsLoading = false) }
    }

    private lateinit var tokenizer: Tokenizer<DefaultTermFeatures>

    private val tokenizerJob = viewModelScope.launch {
        _state.update { it.copy(tokenizerLoading = true) }

        // This withContext block is a coroutine context switcher.
        // It switches the coroutine context to Dispatchers.IO
        // so that the tokenizer can be created on a background thread.
        withContext(Dispatchers.IO) {
            Tokenizer.createDefaultTokenizer()
        }.let { result ->
            tokenizer = result
            _state.update { it.copy(tokenizerLoading = false) }
        }
    }

    init {
        tokenizerJob.start()
//        dictionaryJob.start()
    }

    fun onEvent(event: OCREvent) {
        when (event) {
            is OCREvent.ClickCamera -> {
                _state.update { it.copy(ocrViewMode = OCRViewMode.Result, isScanning = true) }
                event.localCameraController.takePicture(ContextCompat.getMainExecutor(app),
                    object : OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            super.onCaptureSuccess(image)

                            // Generate a matrix to rotate the image based on the image's rotation
                            val matrix = Matrix().apply {
                                postRotate(image.imageInfo.rotationDegrees.toFloat())
                            }

                            val rotatedBitmap = Bitmap.createBitmap(
                                image.toBitmap(), 0, 0, image.width, image.height, matrix, true
                            )

                            runTextRecognition(rotatedBitmap)

                            // We must call this so the UI can display the image
                            updatePreviewPhoto(rotatedBitmap)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            super.onError(exception)
                            Log.e("OCRViewModel", "Could not take photo", exception)
                            // update the state with the error message
                            _state.update { it.copy(imageCaptureError = exception.message) }
                        }
                    })
            }

            is OCREvent.ScanImage -> {
                _state.update { it.copy(isScanning = true, ocrViewMode = OCRViewMode.Result) }

                runTextRecognition(event.image)

                // We must call this so the UI can display the image
                updatePreviewPhoto(event.image)
            }

            is OCREvent.StopScan -> {
                _state.update { it.copy(isScanning = false) }
            }

            is OCREvent.OnClickLine -> {
                _state.update {
                    it.copy(
                        ocrViewMode = OCRViewMode.InspectResult(event.lineId)
                    )
                }
            }

            is OCREvent.OnClickTextBlock -> {

            }

            is OCREvent.OnClickWord -> {

            }

            is OCREvent.OnBack -> {
                when (state.value.ocrViewMode) {
                    OCRViewMode.Result -> {
                        _state.update { it.copy(ocrViewMode = OCRViewMode.Camera) }
                        // Clear the image and results
                        _state.update { it.copy(image = null, ocrResults = null) }
                    }

                    is OCRViewMode.InspectResult -> {
                        _state.update { it.copy(ocrViewMode = OCRViewMode.Result) }
                    }

                    OCRViewMode.Camera -> {
                        event.navigateBack()
                    }
                }
            }
        }
    }

    private suspend fun getJapaneseEnglishEntries(strings: List<String>): Map<String, List<JapaneseEnglishEntry>> {
        dictionaryJob.join()

        return strings.associateWith { string ->
            val results = emptyList<JapaneseEnglishEntry>().toMutableList()
            // Add all entries that match the string at the beginning so they are shown first
            dictionary.entries.forEach { entry ->
                if (entry.word.any { it.text == string }) {
                    results.add(entry)
                }
            }

            // If no entries are found for the word, search for kana-only words as well
            if(results.isEmpty()) {
                // also search the kana only words
                dictionary.entries.forEach { entry ->
                    if (entry.wordKanaOnly.any { it.text == string }) {
                        results.add(entry)
                    }
                }
            }

            // Get all entries that contain the string anywhere in the word,
            // perhaps less relevant so they are shown last
            // Not sure if this is actually necessary TBH will have to test
//            dictionary.entries.forEach {  entry ->
//                if (entry.word.any { it.text.contains(string) }) {
//                    results.add(entry)
//                }
//            }

            Log.d("OCRViewModel", "Results for $string: $results")
            results
        }
    }

    private fun updatePreviewPhoto(image: Bitmap) {
        _state.update { it.copy(image = image) }
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
                        ocrResults =
                        texts.textBlocks.flatMap { it.lines }.mapIndexed { index, line ->
                            val tokenizedText = tokenizeText(line.text)
                            OCRResult(
                                text = line.text,
                                topLeft = line.cornerPoints?.get(0)?.let { Point(it.x, it.y) }!!,
                                confidence = line.confidence,
                                tokenizedText = tokenizedText,
                                angle = line.angle,
                                id = index,
                                japaneseEnglishEntries = getJapaneseEnglishEntries(tokenizedText.map { it.text })
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

    private suspend fun tokenizeText(text: String): List<Token<DefaultTermFeatures>> {
        tokenizerJob.join()
        Log.d("OCRViewModel", "Begin tokenizing text: $text at ${System.currentTimeMillis()}")
        return tokenizer.tokenize(text).map { it }
    }
}