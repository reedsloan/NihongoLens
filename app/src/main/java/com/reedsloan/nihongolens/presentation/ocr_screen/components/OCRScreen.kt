package com.reedsloan.nihongolens.presentation.ocr_screen.components

import android.app.Activity
import android.graphics.PointF
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.github.wanasit.kotori.Token
import com.github.wanasit.kotori.optimized.DefaultTermFeatures
import com.reedsloan.nihongolens.presentation.permission.PermissionEvent
import com.reedsloan.nihongolens.presentation.permission.PermissionRequest
import com.reedsloan.nihongolens.ui.theme.NihongoLensTheme

@Composable
fun OCRScreen(
    ocrState: OCRState,
    onOCREvent: (OCREvent) -> Unit,
    onPermissionEvent: (PermissionEvent) -> Unit,
    cameraController: LifecycleCameraController,
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
        ocrState.image?.let {
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
            Text(text = "isScanning: ${ocrState.isScanning}", color = Color.Red)
            Text(text = "error: ${ocrState.error}", color = Color.Red)

            // Display width and height
            displayMetrics.let {
                Text(text = "Display Width: ${it.widthPixels}", color = Color.Red)
                Text(text = "Display Height: ${it.heightPixels}", color = Color.Red)
            }

            // Image width and height
            ocrState.image?.let {
                Text(text = "Image Width: ${it.width}", color = Color.Red)
                Text(text = "Image Height: ${it.height}", color = Color.Red)
            }
        }
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ocrState.ocrResult?.forEach { ocrResult ->
            RecognizedText(ocrResult, ocrState, onOCREvent)
        }

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
                    enabled = !ocrState.isScanning,
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
    ocrResult: OCRResult,
    ocrState: OCRState,
    onOCREvent: (OCREvent) -> Unit
) {
    val aspectRatio =
        ocrState.image!!.width.toFloat() / ocrState.image.height.toFloat()
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val screenAspectRatio =
        displayMetrics.widthPixels.toFloat() / displayMetrics.heightPixels.toFloat()

    val imageOrigin =
        ocrState.image.let { PointF(it.width / 2F, it.height / 2F) }
    val deviceOrigin = displayMetrics.let {
        PointF(it.widthPixels / 2f, it.heightPixels / 2f)
    }
    val topLeft = ocrResult.topLeft

    val offsetFromOriginX = topLeft.x - imageOrigin.x
    val offsetFromOriginY = topLeft.y - imageOrigin.y

    val offsetFromOriginXScaled =
        offsetFromOriginX * (displayMetrics.widthPixels.toFloat() / ocrState.image.width.toFloat())
    val offsetFromOriginYScaled =
        offsetFromOriginY * (displayMetrics.heightPixels.toFloat() / ocrState.image.height.toFloat())

    val translationXAdjusted =
        deviceOrigin.x + offsetFromOriginXScaled / if (aspectRatio > screenAspectRatio) (screenAspectRatio / aspectRatio) else 1f
    val translationYAdjusted =
        deviceOrigin.y + offsetFromOriginYScaled * if (aspectRatio > screenAspectRatio) 1f else (aspectRatio / screenAspectRatio)

    var textResult: List<Token<DefaultTermFeatures>>? by remember {
        mutableStateOf(null)
    }

    val scope = rememberCoroutineScope()

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
                        imageOrigin.x * (displayMetrics.widthPixels.toFloat() / ocrState.image.width.toFloat()),
                        imageOrigin.y * (displayMetrics.heightPixels.toFloat() / ocrState.image.height.toFloat())
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
            })
    Box(Modifier.fillMaxSize()) {
        when (val ocrViewMode = ocrState.ocrViewMode) {
            OCRViewMode.Camera -> {
                // do nothing
            }

            OCRViewMode.Result -> {
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

            is OCRViewMode.InspectResult -> {
                if (ocrViewMode.id == ocrResult.id) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.extraSmall
                            )
                    ) {
                        Row {
                            ocrResult.tokenizedText.forEach {
                                Text(
                                    text = it.text,
                                    style = TextStyle(
                                        fontSize = 20.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Black
                                    ),
                                    modifier = Modifier
                                        .background(
                                            Color.White,
                                            shape = MaterialTheme.shapes.extraSmall
                                        )
                                        .clickable {
                                            onOCREvent(OCREvent.OnClickWord(it))
                                        }
                                        // border only on bottom
                                        .drawBehind {
                                            val strokeWidth = this.drawContext.size.width * density
                                            val y = size.height - strokeWidth / 2

                                            val color = when (it.features.partOfSpeech) {
                                                DefaultTermFeatures.PartOfSpeech.ADNOMINAL -> Color.Red
                                                DefaultTermFeatures.PartOfSpeech.ADJECTIVE -> Color.Blue
                                                DefaultTermFeatures.PartOfSpeech.ADVERB -> Color.Green
                                                DefaultTermFeatures.PartOfSpeech.AUXILIARY -> Color.Yellow
                                                DefaultTermFeatures.PartOfSpeech.CONJUNCTION -> Color.Magenta
                                                DefaultTermFeatures.PartOfSpeech.INTERJECTION -> Color.Cyan
                                                DefaultTermFeatures.PartOfSpeech.NOUN -> Color.Gray
                                                DefaultTermFeatures.PartOfSpeech.PARTICLE -> Color.DarkGray
                                                DefaultTermFeatures.PartOfSpeech.PREFIX -> Color.LightGray
                                                DefaultTermFeatures.PartOfSpeech.SUFFIX -> Color.Black
                                                DefaultTermFeatures.PartOfSpeech.SYMBOL -> Color.White
                                                DefaultTermFeatures.PartOfSpeech.VERB -> Color.DarkGray
                                                DefaultTermFeatures.PartOfSpeech.OTHER -> Color.LightGray
                                                DefaultTermFeatures.PartOfSpeech.UNKNOWN -> Color.Black
                                            }

                                            drawLine(
                                                color,
                                                Offset(0f, y),
                                                Offset(size.width, y),
                                                strokeWidth
                                            )
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true)
@Composable
fun HighlightingPreview() {
    val strings = listOf(
        PreviewText("こんにちは", DefaultTermFeatures.PartOfSpeech.INTERJECTION),
        PreviewText("私", DefaultTermFeatures.PartOfSpeech.NOUN),
        PreviewText("は", DefaultTermFeatures.PartOfSpeech.PARTICLE),
        PreviewText("アメリカ", DefaultTermFeatures.PartOfSpeech.NOUN),
        PreviewText("人", DefaultTermFeatures.PartOfSpeech.NOUN),
        PreviewText("です", DefaultTermFeatures.PartOfSpeech.VERB),
        PreviewText("昨日", DefaultTermFeatures.PartOfSpeech.NOUN),
        PreviewText("は", DefaultTermFeatures.PartOfSpeech.PARTICLE),
        PreviewText("とても", DefaultTermFeatures.PartOfSpeech.ADVERB),
        PreviewText("寒かった", DefaultTermFeatures.PartOfSpeech.ADJECTIVE),
        PreviewText("です", DefaultTermFeatures.PartOfSpeech.VERB),
        PreviewText("が", DefaultTermFeatures.PartOfSpeech.CONJUNCTION),
        PreviewText("、", DefaultTermFeatures.PartOfSpeech.OTHER),
        PreviewText("今日", DefaultTermFeatures.PartOfSpeech.NOUN),
        PreviewText("は", DefaultTermFeatures.PartOfSpeech.PARTICLE),
        PreviewText("晴れ", DefaultTermFeatures.PartOfSpeech.NOUN),
        PreviewText("ています", DefaultTermFeatures.PartOfSpeech.VERB),
        PreviewText("。", DefaultTermFeatures.PartOfSpeech.OTHER),
        PreviewText("明日", DefaultTermFeatures.PartOfSpeech.NOUN),
        PreviewText("は", DefaultTermFeatures.PartOfSpeech.PARTICLE),
        PreviewText("雨", DefaultTermFeatures.PartOfSpeech.NOUN),
        PreviewText("が", DefaultTermFeatures.PartOfSpeech.CONJUNCTION),
        PreviewText("降る", DefaultTermFeatures.PartOfSpeech.VERB),
        PreviewText("かもしれません", DefaultTermFeatures.PartOfSpeech.VERB),
    )

    NihongoLensTheme {
        Surface(Modifier
            .fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.extraSmall
                    ),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                FlowRow {
                    strings.forEach {
                        Text(
                            text = it.word,
                            style = TextStyle(
                                fontSize = 36.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                // border only on bottom
                                .padding(end = 6.dp)
                                .drawBehind {
                                    val strokeWidth = 2.dp.toPx()
                                    val distanceBelowText = 4.dp.toPx()
                                    val y = (size.height + distanceBelowText - strokeWidth / 2)

                                    val color = when (it.partOfSpeech) {
                                        DefaultTermFeatures.PartOfSpeech.ADNOMINAL -> Color(255, 0, 0)       // Red (Describes nouns)
                                        DefaultTermFeatures.PartOfSpeech.ADJECTIVE -> Color(255, 165, 0)     // Orange (Describes nouns)
                                        DefaultTermFeatures.PartOfSpeech.ADVERB -> Color(0, 128, 0)          // Dark Green (Describes verbs, adjectives, or other adverbs)
                                        DefaultTermFeatures.PartOfSpeech.AUXILIARY -> Color(255, 255, 0)     // Yellow (Helping verbs)
                                        DefaultTermFeatures.PartOfSpeech.CONJUNCTION -> Color(0, 0, 255)      // Blue (Joins words or groups of words)
                                        DefaultTermFeatures.PartOfSpeech.INTERJECTION -> Color(255, 0, 255)   // Magenta (Expresses emotion)
                                        DefaultTermFeatures.PartOfSpeech.NOUN -> Color(128, 0, 128)           // Purple (Person, place, thing, or idea)
                                        DefaultTermFeatures.PartOfSpeech.PARTICLE -> Color(128, 128, 128)     // Gray (Small word with grammatical function)
                                        DefaultTermFeatures.PartOfSpeech.PREFIX -> Color(255, 192, 203)       // Pink (Affix added before a word)
                                        DefaultTermFeatures.PartOfSpeech.SUFFIX -> Color(255, 105, 180)       // Hot Pink (Affix added after a word)
                                        DefaultTermFeatures.PartOfSpeech.SYMBOL -> Color(128, 128, 0)         // Olive (Represents something)
                                        DefaultTermFeatures.PartOfSpeech.VERB -> Color(210, 180, 140)        // Tan (Action or state of being)
                                        DefaultTermFeatures.PartOfSpeech.OTHER -> Color(192, 192, 192)        // Light Gray (Other parts of speech)
                                        DefaultTermFeatures.PartOfSpeech.UNKNOWN -> Color(0, 0, 0)            // Black (Unknown part of speech)
                                    }

                                    drawText(
                                        it.partOfSpeech.name,
                                        Offset(0f, 0f),
                                        style = TextStyle(
                                            fontSize = 36.sp,
                                            color = color,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    drawLine(
                                        color,
                                        Offset(0f, y),
                                        Offset(size.width, y),
                                        strokeWidth
                                    )
                                }
                        )
                    }
                }
            }
        }
    }
}

data class PreviewText(
    val word: String,
    val partOfSpeech: DefaultTermFeatures.PartOfSpeech
)