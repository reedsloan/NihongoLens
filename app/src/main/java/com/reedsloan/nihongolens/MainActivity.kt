package com.reedsloan.nihongolens

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.reedsloan.nihongolens.ext.openAppSettings
import com.reedsloan.nihongolens.presentation.ScreenRoute
import com.reedsloan.nihongolens.presentation.permission.PermissionDialog
import com.reedsloan.nihongolens.presentation.permission.PermissionEvent
import com.reedsloan.nihongolens.presentation.permission.PermissionViewModel
import com.reedsloan.nihongolens.presentation.ocr_screen.components.OCRScreen
import com.reedsloan.nihongolens.presentation.ocr_screen.components.OCRViewModel
import com.reedsloan.nihongolens.ui.theme.NihongoLensTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NihongoLensTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val context = LocalContext.current

                    // Permission ViewModel
                    val permissionViewModel = hiltViewModel<PermissionViewModel>()
                    val permissionState by permissionViewModel.state.collectAsState()

                    // OCR ViewModel
                    val ocrViewModel = hiltViewModel<OCRViewModel>()
                    val ocrState by ocrViewModel.state.collectAsState()

                    // permission launcher
                    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions ->
                        permissions.forEach { (permission, granted) ->
                            permissionViewModel.onEvent(
                                PermissionEvent.OnPermissionResult(
                                    permission,
                                    granted
                                )
                            )
                        }
                    }

                    // Camera controller
                    val cameraController = remember {
                        LifecycleCameraController(applicationContext).apply {
                            setEnabledUseCases(
                                CameraController.IMAGE_CAPTURE or
                                        CameraController.VIDEO_CAPTURE
                            )
                        }
                    }

                    // Permission dialog
                    Box(Modifier.fillMaxSize()) {
                        // Get the first permission request from the queue
                        permissionState.permissionRequestQueue.firstOrNull()
                            .let { permissionRequest ->
                                val activity = context as Activity
                                if (permissionRequest != null) {
                                    PermissionDialog(
                                        permissionRequest = permissionRequest,
                                        isPermanentlyDeclined = permissionViewModel.isPermissionPermanentlyDeclined(
                                            activity, permissionRequest.permission
                                        ),
                                        onDismiss = { permissionViewModel.onEvent(PermissionEvent.OnDismissDialog) },
                                        onConfirm = {
                                            multiplePermissionsLauncher.launch(
                                                arrayOf(permissionRequest.permission)
                                            )
                                        },
                                        onGoToAppSettingsClick = {
                                            activity.openAppSettings()
                                            permissionViewModel.onEvent(PermissionEvent.OnDismissDialog)
                                        },
                                    )
                                }
                            }
                    }

                    // NavHost for all screens
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = ScreenRoute.OCRScreen.route
                        ) {
                            composable(
                                route = ScreenRoute.OCRScreen.route
                            ) {
                                OCRScreen(
                                    ocrState = ocrState,
                                    onOCREvent = { ocrViewModel.onEvent(it) },
                                    onPermissionEvent = { permissionViewModel.onEvent(it) },
                                    cameraController = cameraController
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}