package com.reedsloan.nihongolens.presentation.ocr_screen.components

import android.app.Activity
import android.graphics.PointF
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.graphics.div
import androidx.core.graphics.times
import com.reedsloan.nihongolens.presentation.permission.PermissionEvent
import com.reedsloan.nihongolens.presentation.permission.PermissionRequest

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

    val (scalarX, scalarY) = ocrState.image?.let {
        val displayMetrics = LocalContext.current.resources.displayMetrics
        val displayWidth = displayMetrics.widthPixels
        val displayHeight = displayMetrics.heightPixels
        val imageWidth = it.width
        val imageHeight = it.height

        val scalarX = displayWidth.toFloat() / imageWidth
        val scalarY = displayHeight.toFloat() / imageHeight

        Pair(scalarX, scalarY)
    } ?: Pair(1f, 1f)

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
        ocrState.textRecognitionResult.let { textRecognitionResult ->
            textRecognitionResult?.textBlocks?.forEach { textBlock ->
                textBlock.lines.forEach { line ->
                    val topLeft = line.cornerPoints!![0]
                    val bottomRight = line.cornerPoints!![3]
                    val aspectRatio =
                        ocrState.image!!.width.toFloat() / ocrState.image.height.toFloat()
                    val screenAspectRatio =
                        displayMetrics.widthPixels.toFloat() / displayMetrics.heightPixels.toFloat()

                    val imageOrigin =
                        ocrState.image.let { PointF(it.width / 2F, it.height / 2F) }
                    val deviceOrigin = displayMetrics.let {
                        PointF(it.widthPixels / 2f, it.heightPixels / 2f)
                    }

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
                        // Do something with the element
                        // based on the distance between the top left and bottom right corners of the bounding box scale the DP of the text
                        val distance = PointF(
                            bottomRight.x.toFloat() - topLeft.x.toFloat(),
                            bottomRight.y.toFloat() - topLeft.y.toFloat()
                        ).length()

                        Text(
                            text = line.text,
                            modifier = Modifier
                                .graphicsLayer {
                                    translationX = translationXAdjusted
                                    translationY = translationYAdjusted
                                    rotationZ = line.angle
                                }
                                .background(Color.White, shape = MaterialTheme.shapes.extraSmall),
                            color = Color.Red,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

//                    line.elements.forEach { element ->
//
//                    }
            }
        }
    }

    // draw bounding boxes
//    Box(modifier = Modifier.fillMaxSize()) {
//        ocrState.textRecognitionResult?.let { textRecognitionResult ->
//            textRecognitionResult.textBlocks.forEach { textBlock ->
//                textBlock.lines.forEach { line ->
//                    line.elements.forEach { element ->
//                        val topLeft = element.cornerPoints!![0] * scalarY
//                        val bottomRight = element.cornerPoints!![3] * scalarX
//
//                        // Do something with the element
//                        Box(
//                            modifier = Modifier
//                                .graphicsLayer {
//                                    translationY = topLeft.y.toFloat()
//                                    translationX = topLeft.x.toFloat()
//                                    rotationZ = element.angle
//                                }
//                                .width(
//                                    topLeft.x.toFloat().dp - bottomRight.x.toFloat().dp,
//                                )
//                                .height(
//                                    topLeft.y.toFloat().dp - bottomRight.y.toFloat().dp,
//                                )
//                                .border(2.dp, Color.Red),
//                        )
//                    }
//                }
//            }
//        }
//    }

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
                Icon(imageVector = Icons.Default.Cameraswitch, contentDescription = "Switch Camera")
            }

            // Capture Image
            IconButton(
                enabled = !ocrState.isScanning,
                onClick = {
                    onOCREvent(OCREvent.StartScan(cameraController, displayMetrics))
                }, modifier = Modifier.constrainAs(captureImage) {
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }) {
                Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Capture Image")
            }
        }
    }
}