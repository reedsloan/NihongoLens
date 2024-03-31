package com.reedsloan.nihongolens.presentation.ocr_screen.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.activity.compose.setContent
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.reedsloan.nihongolens.MainActivity
import com.reedsloan.nihongolens.di.AppModule
import com.reedsloan.nihongolens.presentation.ScreenRoute
import com.reedsloan.nihongolens.presentation.permission.PermissionViewModel
import com.reedsloan.nihongolens.ui.theme.NihongoLensTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(AppModule::class)
class OCRScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val state = mutableStateOf(OCRScreenState())

    @Before
    fun setUp() {
        hiltRule.inject()
        composeRule.activity.setContent {
            val ocrViewModel = hiltViewModel<OCRViewModel>()

            LaunchedEffect(key1 = Unit) {
                initializeViewModel(ocrViewModel)
            }

            val permissionViewModel = hiltViewModel<PermissionViewModel>()
            val ocrScreenState by ocrViewModel.state.collectAsState()
            val context = LocalContext.current

            val cameraController = remember {
                LifecycleCameraController(context).apply {
                    setEnabledUseCases(
                        CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE
                    )
                }
            }

            LaunchedEffect(key1 = ocrScreenState) {
                state.value = ocrScreenState
            }

            val navController = rememberNavController()

            NihongoLensTheme {
                NavHost(
                    navController = navController,
                    startDestination = ScreenRoute.OCRScreen.route
                ) {
                    composable(route = ScreenRoute.OCRScreen.route) {
                        OCRScreen(
                            ocrScreenState = ocrScreenState,
                            onOCREvent = ocrViewModel::onEvent,
                            onPermissionEvent = permissionViewModel::onEvent,
                            cameraController = cameraController,
                            navController = navController
                        )
                    }
                }
            }
        }
    }

    private fun initializeViewModel(ocrViewModel: OCRViewModel) {
        // The testContext is used to access the src/test/resources directory
        val testContext = InstrumentationRegistry.getInstrumentation().context
        val assetManager = testContext.assets
        val inputStream = assetManager.open("ocr_test_image.jpg")
        val byteArray = inputStream.readBytes()
        inputStream.close()


        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        // Generate a matrix to rotate the image based on the image's rotation
        val matrix = Matrix().apply {
            postRotate(90f)
        }
        val rotatedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )


        // This updates the OCRViewModel with the image to scan so we can test the OCR process
        ocrViewModel.onEvent(OCREvent.ScanImage(rotatedBitmap))
    }

    @Test
    fun isOCRImageDisplayedCorrectly() {
        composeRule.waitUntil(15_000) {
            state.value.image != null
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("OCRImage").assertIsDisplayed()
    }

    @Test
    fun isOCRTextDisplayedCorrectly() {
        composeRule.waitUntil(35_000) {
            state.value.ocrResults != null
        }
        composeRule.waitForIdle()
        composeRule.onAllNodesWithTag("OCRText").onFirst().assertIsDisplayed()
        // Assert the OCR correctly detected the text "NHKの外国語サービス" in the test image
        composeRule.onAllNodesWithText("NHKの外国語サービス").onFirst().assertIsDisplayed()
    }

}