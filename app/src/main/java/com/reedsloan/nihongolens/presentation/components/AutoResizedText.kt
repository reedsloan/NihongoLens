package com.reedsloan.nihongolens.presentation.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycling

/**
 * Clever implementation inspired by Philipp Lackner's video:
 *
 * [How to Create a Magic Text That Always Fits Its Container](https://www.youtube.com/watch?v=ntlyrFw0F9U)
 *
 * TODO: This actually doesn't work as expected. It performs very slowly.
 */
@Composable
fun AutoResizedText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    var resizedFontSize by rememberSaveable { mutableFloatStateOf(style.fontSize.value) }
    val defaultFontSize = style.fontSize
    var shouldDraw by rememberSaveable { mutableStateOf(false) }
    var isFirstLayout by rememberSaveable { mutableStateOf(true) }
    var isIncreasing by rememberSaveable { mutableStateOf(false) }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        onDispose {
            Log.d("AutoResizedText", "Disposed")
        }
    }

    Text(
        text = text,
        style = style.copy(
            fontSize = resizedFontSize.sp
        ),
        modifier = modifier.drawWithContent {
            if (shouldDraw) {
                drawContent()
            }
        },
        color = color,
        softWrap = false,
        onTextLayout = { result ->
            if (result.didOverflowWidth && !isIncreasing) {
                if (style.fontSize.isUnspecified) {
                    resizedFontSize = defaultFontSize.value
                }
                resizedFontSize *= 0.9F
            } else if (isIncreasing) {
                resizedFontSize *= 1.1F
            } else {
                if(!isFirstLayout) {
                    isIncreasing = true
                }
                shouldDraw = true
            }

            isFirstLayout = false
        }
    )
}