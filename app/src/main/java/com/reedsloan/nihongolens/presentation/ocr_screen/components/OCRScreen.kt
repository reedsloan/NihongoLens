package com.reedsloan.nihongolens.presentation.ocr_screen.components

import android.app.Activity
import android.graphics.PointF
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.github.wanasit.kotori.optimized.DefaultTermFeatures
import com.reedsloan.nihongolens.domain.model.JapaneseEnglishEntry
import com.reedsloan.nihongolens.presentation.permission.PermissionEvent
import com.reedsloan.nihongolens.presentation.permission.PermissionRequest
import com.reedsloan.nihongolens.ui.theme.NihongoLensTheme
import kotlin.math.min

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
    ) {
        ocrScreenState.image?.let {
            Image(
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
                    text = "tokenizerLoading: ${ocrScreenState.tokenizerLoading}",
                    color = Color.Red
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
        modifier = Modifier
            .fillMaxSize()
    ) {
        RecognizedText(ocrScreenState, onOCREvent)


        Box(modifier = Modifier.fillMaxSize()) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (switchCamera, captureImage) = createRefs()

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

                // Capture Image
                IconButton(
//                    enabled = !ocrState.isScanning && !ocrState.dictionaryIsLoading && !ocrState.tokenizerLoading,
                    onClick = {
                        onOCREvent(OCREvent.ClickCamera(cameraController, displayMetrics))
                    }, modifier = Modifier.constrainAs(captureImage) {
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    }) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Capture Image"
                    )
                }
            }
        }
    }
}

@Composable
fun RecognizedText(
    ocrScreenState: OCRScreenState,
    onOCREvent: (OCREvent) -> Unit
) {
    val image = ocrScreenState.image ?: return

    val aspectRatio = image.width.toFloat() / ocrScreenState.image.height.toFloat()
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val screenAspectRatio =
        displayMetrics.widthPixels.toFloat() / displayMetrics.heightPixels.toFloat()

    val imageOrigin =
        ocrScreenState.image.let { PointF(it.width / 2F, it.height / 2F) }
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

                ocrScreenState.ocrResults?.forEach { ocrResult ->
                    val topLeft = ocrResult.topLeft

                    val offsetFromOriginX = topLeft.x - imageOrigin.x
                    val offsetFromOriginY = topLeft.y - imageOrigin.y

                    val offsetFromOriginXScaled =
                        offsetFromOriginX * (displayMetrics.widthPixels.toFloat() / ocrScreenState.image.width.toFloat())
                    val offsetFromOriginYScaled =
                        offsetFromOriginY * (displayMetrics.heightPixels.toFloat() / ocrScreenState.image.height.toFloat())

                    val translationXAdjusted =
                        deviceOrigin.x + offsetFromOriginXScaled / if (aspectRatio > screenAspectRatio) (screenAspectRatio / aspectRatio) else 1f
                    val translationYAdjusted =
                        deviceOrigin.y + offsetFromOriginYScaled * if (aspectRatio > screenAspectRatio) 1f else (aspectRatio / screenAspectRatio)

                    Box(
                        Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                // draw a dot at the top left corner of the bounding box
                                drawCircle(
                                    color = Color.Red,
                                    radius = 10f,
                                    center = Offset(
                                        translationXAdjusted,
                                        translationYAdjusted
                                    )
                                )

                                // draw a big blue circle at the center of the image
                                drawCircle(
                                    color = Color.Blue,
                                    radius = 30f,
                                    center = Offset(
                                        imageOrigin.x * (displayMetrics.widthPixels.toFloat() / ocrScreenState.image.width.toFloat()),
                                        imageOrigin.y * (displayMetrics.heightPixels.toFloat() / ocrScreenState.image.height.toFloat())
                                    )
                                )

                                // draw a big green circle at the center of the device
                                drawCircle(
                                    color = Color.Green,
                                    radius = 20f,
                                    center = Offset(
                                        deviceOrigin.x,
                                        deviceOrigin.y
                                    )
                                )
                            }) {}
                    Text(
                        text = ocrResult.text,
                        style = TextStyle(fontSize = 20.sp),
                        modifier = Modifier
                            .graphicsLayer {
                                translationX = translationXAdjusted
                                translationY = translationYAdjusted
                                rotationZ = ocrResult.angle
                            }
                            .background(Color.White, shape = MaterialTheme.shapes.extraSmall)
                            .clickable {
                                onOCREvent(OCREvent.OnClickLine(ocrResult.id))
                            },
                    )
                }
            }

            is OCRViewMode.InspectResult -> {
                if (ocrScreenState.ocrResults == null) return
                val ocrResult = ocrScreenState.ocrResults.first { it.id == ocrViewMode.id }

                HighlightingPreview(
                    strings = ocrResult.tokenizedText.map {
                        TextData(it.text, it.features.partOfSpeech)
                    },
                    japaneseEnglishEntries = ocrResult.japaneseEnglishEntries,
                    loading = ocrScreenState.dictionaryIsLoading
                )
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true)
@Composable
fun HighlightingPreview(
    loading: Boolean = false,
    japaneseEnglishEntries: List<JapaneseEnglishEntry> = emptyList(),
    // TODO: Maybe put this in a UI test later :)
    strings: List<TextData> = listOf(
        TextData("こんにちは", DefaultTermFeatures.PartOfSpeech.INTERJECTION),
        TextData("私", DefaultTermFeatures.PartOfSpeech.NOUN),
        TextData("は", DefaultTermFeatures.PartOfSpeech.PARTICLE),
        TextData("アメリカ", DefaultTermFeatures.PartOfSpeech.NOUN),
        TextData("人", DefaultTermFeatures.PartOfSpeech.NOUN),
        TextData("です", DefaultTermFeatures.PartOfSpeech.VERB),
        TextData("昨日", DefaultTermFeatures.PartOfSpeech.NOUN),
        TextData("は", DefaultTermFeatures.PartOfSpeech.PARTICLE),
        TextData("とても", DefaultTermFeatures.PartOfSpeech.ADVERB),
        TextData("寒かった", DefaultTermFeatures.PartOfSpeech.ADJECTIVE),
        TextData("です", DefaultTermFeatures.PartOfSpeech.VERB),
        TextData("が", DefaultTermFeatures.PartOfSpeech.CONJUNCTION),
        TextData("、", DefaultTermFeatures.PartOfSpeech.OTHER),
        TextData("今日", DefaultTermFeatures.PartOfSpeech.NOUN),
        TextData("は", DefaultTermFeatures.PartOfSpeech.PARTICLE),
        TextData("晴れ", DefaultTermFeatures.PartOfSpeech.NOUN),
        TextData("ています", DefaultTermFeatures.PartOfSpeech.VERB),
        TextData("。", DefaultTermFeatures.PartOfSpeech.OTHER),
        TextData("明日", DefaultTermFeatures.PartOfSpeech.NOUN),
        TextData("は", DefaultTermFeatures.PartOfSpeech.PARTICLE),
        TextData("雨", DefaultTermFeatures.PartOfSpeech.NOUN),
        TextData("が", DefaultTermFeatures.PartOfSpeech.CONJUNCTION),
        TextData("降る", DefaultTermFeatures.PartOfSpeech.VERB),
        TextData("かもしれません", DefaultTermFeatures.PartOfSpeech.VERB)
    )
) {
    var showDefinitionForIndex: Int? by remember { mutableStateOf(null) }

    NihongoLensTheme {
        Surface(
            Modifier
                .fillMaxSize()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.extraSmall
                    )
                    .clickable { showDefinitionForIndex = null }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                FlowRow {
                    strings.forEachIndexed { index, previewText ->
                        val color = when (previewText.partOfSpeech) {
                            DefaultTermFeatures.PartOfSpeech.ADNOMINAL -> Color(3, 132, 252)
                            DefaultTermFeatures.PartOfSpeech.ADJECTIVE -> Color(255, 165, 0)
                            DefaultTermFeatures.PartOfSpeech.ADVERB -> Color(0, 128, 0)
                            DefaultTermFeatures.PartOfSpeech.AUXILIARY -> Color(209, 36, 36)
                            DefaultTermFeatures.PartOfSpeech.CONJUNCTION -> Color(0, 0, 255)
                            DefaultTermFeatures.PartOfSpeech.INTERJECTION -> Color(163, 11, 118)
                            DefaultTermFeatures.PartOfSpeech.NOUN -> Color(3, 132, 252)
                            DefaultTermFeatures.PartOfSpeech.PARTICLE -> Color(255, 200, 0)
                            DefaultTermFeatures.PartOfSpeech.PREFIX -> Color(255, 192, 203)
                            DefaultTermFeatures.PartOfSpeech.SUFFIX -> Color(255, 105, 180)
                            DefaultTermFeatures.PartOfSpeech.SYMBOL -> Color(128, 128, 0)
                            DefaultTermFeatures.PartOfSpeech.VERB -> Color(77, 204, 63)
                            DefaultTermFeatures.PartOfSpeech.OTHER -> Color(192, 192, 192)
                            DefaultTermFeatures.PartOfSpeech.UNKNOWN -> Color(0, 0, 0)
                        }

                        val colorScalar by remember { mutableFloatStateOf(1.5f) }
                        val selectedColor = color.copy(
                            red = min(1F, color.red * colorScalar),
                            green = min(1F, color.green * colorScalar),
                            blue = min(1F, color.blue * colorScalar)
                        )

                        Text(
                            text = previewText.word,
                            style = TextStyle(
                                fontSize = 36.sp,
                                color = if (showDefinitionForIndex == index) selectedColor else color,
                                fontWeight = FontWeight.Bold,
                            ),
                            modifier = Modifier
                                // border only on bottom
                                .padding(end = 6.dp)
                                .drawBehind {
                                    val strokeWidth = 2.dp.toPx()
                                    val distanceBelowText = 0.dp.toPx()
                                    val y = (size.height + distanceBelowText - strokeWidth / 2)


                                    if (previewText.partOfSpeech == DefaultTermFeatures.PartOfSpeech.OTHER || previewText.partOfSpeech == DefaultTermFeatures.PartOfSpeech.UNKNOWN) {
                                        return@drawBehind
                                    }

                                    drawLine(
                                        if (showDefinitionForIndex == index) Color.White else color,
                                        Offset(0f, y),
                                        Offset(size.width, y),
                                        strokeWidth
                                    )
                                }
                                .clickable {
                                    showDefinitionForIndex = index
                                }
                        )
                    }
                }
            }

            showDefinitionForIndex?.let {
                Box(
                    contentAlignment = Alignment.Center
                )
                {
                    ElevatedCard(
                        modifier = Modifier
                            .height(350.dp)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        if (loading) {
                            Column(
                                Modifier
                                    .fillMaxSize(),
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
                                        text = "Definition for ${strings[it].word}",
                                        style = TextStyle(fontSize = 20.sp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    japaneseEnglishEntries[it].let { entry ->

                                        Divider(Modifier.padding(vertical = 8.dp))
                                        Row {
                                            val kanji = entry.word
                                            val kana = entry.wordKanaOnly

                                            kanji.forEach {
                                                Text(
                                                    text = it.toString(),
                                                    style = MaterialTheme.typography.titleLarge,
                                                )
                                            }

                                            // dot separator
                                            if (kanji.isNotEmpty() && kana.isNotEmpty()) {
                                                Text(
                                                    text = "・",
                                                    style = MaterialTheme.typography.titleLarge,
                                                )
                                            }

                                            kana.forEach {
                                                Text(
                                                    text = it.toString(),
                                                    style = MaterialTheme.typography.titleLarge,
                                                )

                                            }
                                        }
                                        val englishDefinitions = entry.englishDefinitions

                                        englishDefinitions.forEachIndexed { index, definition ->
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Part of Speech: ${definition.partOfSpeech}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = Color.Gray
                                                )
                                            )

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
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class TextData(
    val word: String,
    val partOfSpeech: DefaultTermFeatures.PartOfSpeech
)