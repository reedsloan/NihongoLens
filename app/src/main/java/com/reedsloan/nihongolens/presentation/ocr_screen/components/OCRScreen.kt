package com.reedsloan.nihongolens.presentation.ocr_screen.components

import android.app.Activity
import android.graphics.PointF
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.atilika.kuromoji.ipadic.Token
import com.reedsloan.nihongolens.domain.model.JapaneseEnglishEntry
import com.reedsloan.nihongolens.presentation.components.AutoResizedText
import com.reedsloan.nihongolens.presentation.permission.PermissionEvent
import com.reedsloan.nihongolens.presentation.permission.PermissionRequest

@Composable
fun OCRScreen(
    ocrScreenState: OCRScreenState,
    onOCREvent: (OCREvent) -> Unit,
    onPermissionEvent: (PermissionEvent) -> Unit,
    cameraController: LifecycleCameraController,
    navController: NavController
) {
    // LaunchedEffect to request camera permission when the screen is first shown
    val activity = LocalContext.current as Activity
    LaunchedEffect(Unit) {
        onPermissionEvent(
            PermissionEvent.RequestPermission(
                activity, PermissionRequest.CameraPermissionRequest
            )
        )
    }

    // CameraPreview
    CameraPreview(
        controller = cameraController, modifier = Modifier.fillMaxSize()
    )
    val displayMetrics = LocalContext.current.resources.displayMetrics

    Box(
        modifier = Modifier
            .width(displayMetrics.widthPixels.dp)
            .height(displayMetrics.heightPixels.dp)
            .testTag("OCRScreen")
    ) {
        ocrScreenState.image?.let {
            Image(
                modifier = Modifier.testTag("OCRImage"),
                bitmap = it.asImageBitmap(),
                contentDescription = "OCR Image",
                contentScale = ContentScale.Crop,
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Debug View
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
        ) {
            // OCR State Debug
            Text(text = "isScanning: ${ocrScreenState.isScanning}", color = Color.Red)
            Text(text = "error: ${ocrScreenState.error}", color = Color.Red)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "tokenizerLoading: ${ocrScreenState.tokenizerLoading}", color = Color.Red
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (ocrScreenState.tokenizerLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Red)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "dictionaryIsLoading: ${ocrScreenState.dictionaryIsLoading}",
                    color = Color.Red
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (ocrScreenState.dictionaryIsLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Red)
                }
            }

            // Display width and height
            displayMetrics.let {
                Text(text = "Display Width: ${it.widthPixels}", color = Color.Red)
                Text(text = "Display Height: ${it.heightPixels}", color = Color.Red)
            }

            // Image width and height
            ocrScreenState.image?.let {
                Text(text = "Image Width: ${it.width}", color = Color.Red)
                Text(text = "Image Height: ${it.height}", color = Color.Red)
            }

            Text(text = ocrScreenState.ocrViewMode.toString(), color = Color.Red)
        }
    }

    BackHandler {
        onOCREvent(OCREvent.OnBack(navController::popBackStack))
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        RecognizedText(ocrScreenState, onOCREvent)


        Box(modifier = Modifier.fillMaxSize()) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (switchCamera, bottomRightButton) = createRefs()

                // Switch Camera (Front/Back)
                IconButton(onClick = {
                    cameraController.cameraSelector =
                        if (cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        }
                }, modifier = Modifier.constrainAs(switchCamera) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera"
                    )
                }

                when (ocrScreenState.ocrViewMode) {
                    OCRViewMode.Camera -> {
                        IconButton(
                            enabled = !ocrScreenState.isScanning,
                            onClick = {
                                onOCREvent(OCREvent.ClickCamera(cameraController, displayMetrics))
                            }, modifier = Modifier.constrainAs(bottomRightButton) {
                                bottom.linkTo(parent.bottom)
                                end.linkTo(parent.end)
                            }) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Capture Image"
                            )
                        }
                    }

                    else -> {
                        // back button
                        IconButton(
                            onClick = {
                                onOCREvent(OCREvent.OnBack(navController::popBackStack))
                            }, modifier = Modifier.constrainAs(bottomRightButton) {
                                bottom.linkTo(parent.bottom)
                                end.linkTo(parent.end)
                            }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecognizedText(
    ocrScreenState: OCRScreenState, onOCREvent: (OCREvent) -> Unit
) {
    val image = ocrScreenState.image ?: return

    val aspectRatio = image.width.toFloat() / image.height.toFloat()
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val screenAspectRatio =
        displayMetrics.widthPixels.toFloat() / displayMetrics.heightPixels.toFloat()

    val imageOrigin = ocrScreenState.image.let { PointF(it!!.width / 2F, it.height / 2F) }
    val deviceOrigin = displayMetrics.let {
        PointF(it.widthPixels / 2f, it.heightPixels / 2f)
    }
    Box(Modifier.fillMaxSize()) {
        when (val ocrViewMode = ocrScreenState.ocrViewMode) {
            OCRViewMode.Camera -> {
                // do nothing
            }

            OCRViewMode.Result -> {
                if (ocrScreenState.dictionaryIsLoading || ocrScreenState.tokenizerLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                }
                SelectionContainer {


                    ocrScreenState.ocrResults?.forEach { ocrResult ->
                        val topLeft = ocrResult.topLeft
                        val bottomRight = ocrResult.bottomRight

                        val offsetFromOriginXTopLeft = topLeft.x - imageOrigin.x
                        val offsetFromOriginYTopLeft = topLeft.y - imageOrigin.y
                        val offsetFromOriginX2BottomRight = bottomRight.x - imageOrigin.x
                        val offsetFromOriginY2BottomRight = bottomRight.y - imageOrigin.y

                        val offsetFromOriginXScaledTopLeft =
                            offsetFromOriginXTopLeft * (displayMetrics.widthPixels.toFloat() / image.width.toFloat())
                        val offsetFromOriginYScaledTopLeft =
                            offsetFromOriginYTopLeft * (displayMetrics.heightPixels.toFloat() / image.height.toFloat())

                        val offsetFromOriginXScaledBottomRight =
                            offsetFromOriginX2BottomRight * (displayMetrics.widthPixels.toFloat() / image.width.toFloat())
                        val offsetFromOriginYScaledBottomRight =
                            offsetFromOriginY2BottomRight * (displayMetrics.heightPixels.toFloat() / image.height.toFloat())

                        val translationXAdjusted =
                            deviceOrigin.x + offsetFromOriginXScaledTopLeft / if (aspectRatio > screenAspectRatio) (screenAspectRatio / aspectRatio) else 1f
                        val translationYAdjusted =
                            deviceOrigin.y + offsetFromOriginYScaledTopLeft * if (aspectRatio > screenAspectRatio) 1f else (aspectRatio / screenAspectRatio)
                        val bottomRightTranslationXAdjusted =
                            deviceOrigin.x + offsetFromOriginXScaledBottomRight / if (aspectRatio > screenAspectRatio) (screenAspectRatio / aspectRatio) else 1f
                        val bottomRightTranslationYAdjusted =
                            deviceOrigin.y + offsetFromOriginYScaledBottomRight * if (aspectRatio > screenAspectRatio) 1f else (aspectRatio / screenAspectRatio)
                        Box(
                            Modifier
                                .fillMaxSize()
                                .drawWithContent {
                                    // draw a dot at the top left corner of the bounding box
                                    drawCircle(
                                        color = Color.Red, radius = 10f, center = Offset(
                                            translationXAdjusted, translationYAdjusted
                                        )
                                    )

                                    // draw a blue dot at the bottom right corner of the bounding box
                                    drawCircle(
                                        color = Color.Blue, radius = 10f, center = Offset(
                                            bottomRightTranslationXAdjusted,
                                            bottomRightTranslationYAdjusted
                                        )
                                    )

                                    // draw a big blue circle at the center of the image
                                    drawCircle(
                                        color = Color.Blue, radius = 30f, center = Offset(
                                            imageOrigin.x * (displayMetrics.widthPixels.toFloat() / image.width.toFloat()),
                                            imageOrigin.y * (displayMetrics.heightPixels.toFloat() / image.height.toFloat())
                                        )
                                    )

                                    // draw a big green circle at the center of the device
                                    drawCircle(
                                        color = Color.Green, radius = 20f, center = Offset(
                                            deviceOrigin.x, deviceOrigin.y
                                        )
                                    )
                                }) {}

                        val width by rememberSaveable {
                            mutableFloatStateOf(
                                bottomRightTranslationXAdjusted - translationXAdjusted
                            )
                        }
                        val height by remember {
                            mutableFloatStateOf(
                                bottomRightTranslationYAdjusted - translationYAdjusted
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            // The text scanned from the image in the OCR result
                            AutoResizedText(
                                text = ocrResult.text,
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .testTag("OCRText")
                                    .graphicsLayer {
                                        translationX = translationXAdjusted
                                        translationY = translationYAdjusted
                                        rotationZ = ocrResult.angle
                                    }
                                    .width((width / displayMetrics.density).dp)
//                                    .height((height * 1.2F / displayMetrics.density).dp)
                                    .background(
                                        Color.White.copy(alpha = 0.8F),
                                        shape = MaterialTheme.shapes.extraSmall
                                    )
                                    .clickable {
                                        onOCREvent(OCREvent.OnClickLine(ocrResult.id))
                                    }
                            )
                        }
                    }
                }
            }


            is OCRViewMode.InspectResult -> {
                if (ocrScreenState.ocrResults == null) return
                val ocrResult = ocrScreenState.ocrResults.first { it.id == ocrViewMode.id }

                TextLookup(
                    tokenizedText = ocrResult.tokenizedText,
                    japaneseEnglishEntries = ocrResult.tokenToDefinitionMap,
                    loading = ocrScreenState.dictionaryIsLoading || ocrScreenState.tokenizerLoading || ocrScreenState.isScanning,
                    previewText = ocrResult.text
                )
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextLookup(
    loading: Boolean = false,
    japaneseEnglishEntries: Map<Token, List<JapaneseEnglishEntry>> = emptyMap(),
    tokenizedText: List<Token> = emptyList(),
    previewText: String = ""
) {
    var showDefinitionForToken: Token? by remember { mutableStateOf(null) }
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.extraSmall
            )
            .padding(16.dp)
            .clickable(indication = null, interactionSource = interactionSource) {
                showDefinitionForToken = null
            },
        contentAlignment = Alignment.Center) {

        FlowRow {
            if (tokenizedText.isNotEmpty()) {
                tokenizedText.forEach { featuresToken ->
                    SelectionContainer {
                    ExtractedText(featuresToken) {
                        // be careful because we have to lookup the root form of the word
                        Log.d(
                            "TextLookup",
                            "conjugationForm: ${featuresToken.conjugationForm}"
                        )
                        Log.d(
                            "TextLookup",
                            "conjugationType: ${featuresToken.conjugationType}"
                        )
                        showDefinitionForToken = featuresToken
                    }
                }
                }
            } else {
                Text(
                    text = previewText,
                    style = TextStyle(
                        fontSize = 36.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // show loading indicator indicating that the text is being processed
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Text is being processed...",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color.LightGray
                        )
                    )
                }
            }
        }
    }

    showDefinitionForToken?.let {
        WordDefinition(showDefinitionForToken, loading, japaneseEnglishEntries)
    }
}

@Composable
private fun ExtractedText(
    featuresToken: Token,
    onClick: () -> Unit
) {
    Text(text = featuresToken.surface, style = TextStyle(
        fontSize = 36.sp,
        color = Color.White,
        fontWeight = FontWeight.Bold,
    ), modifier = Modifier
        // border only on bottom
        .padding(end = 6.dp)
        .drawBehind {
            val strokeWidth = 2.dp.toPx()
            val distanceBelowText = 0.dp.toPx()
            val y = (size.height + distanceBelowText - strokeWidth / 2)

            drawLine(
                Color.White,
                Offset(0f, y),
                Offset(size.width, y),
                strokeWidth
            )
        }
        .clickable {
            onClick()
        })
}

@Composable
private fun WordDefinition(
    token: Token?,
    loading: Boolean,
    japaneseEnglishEntries: Map<Token, List<JapaneseEnglishEntry>>
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier
                .height(350.dp)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (loading || token == null) {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Loading definitions...",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    Modifier.padding(8.dp)
                ) {
                    item {
                        // text of dictionary for selected word
                        Text(
                            text = "Definition for ${token.baseForm ?: token.surface}",
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val sortedEntries = japaneseEnglishEntries[token]?.sortedByDescending {
                            // This is a simple heuristic to sort the definitions by suffixes first
                            var sortScore = 0
                            it.englishDefinitions.forEach { definition ->
                                if (definition.partOfSpeech.contains("Suffix")) {
                                    sortScore += 1
                                }
                            }
                            sortScore
                        } ?: emptyList()

                        sortedEntries.forEach { entry ->
                            Divider(Modifier.padding(vertical = 8.dp))
                            KanjiAndKanaForWord(entry)
                            JapaneseWithEnglishDefinitions(entry)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KanjiAndKanaForWord(entry: JapaneseEnglishEntry) {
    Row {
        val kanji = entry.word
        val kana = entry.wordKanaOnly

        Text(
            text = kanji.joinToString(", ") { it.text },
            style = MaterialTheme.typography.titleLarge,
        )

        // dot separator
        if (kanji.isNotEmpty() && kana.isNotEmpty()) {
            Text(
                text = "・",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        Text(
            text = kana.joinToString(", ") { it.text },
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
private fun JapaneseWithEnglishDefinitions(entry: JapaneseEnglishEntry) {
    entry.englishDefinitions.forEachIndexed { index, definition ->
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = definition.partOfSpeech.joinToString(", "),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.Gray
            ),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row {
            Text(
                text = "${index + 1}. ",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Gray
                )
            )

            Text(
                text = definition.text.joinToString { it },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

data class TextData(
    val word: String, val partOfSpeech: String?
)