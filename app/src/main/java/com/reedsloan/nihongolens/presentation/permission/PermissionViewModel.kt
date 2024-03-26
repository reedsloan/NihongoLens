package com.reedsloan.nihongolens.presentation.permission

import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reedsloan.nihongolens.domain.use_case.GetAppConfiguration
import com.reedsloan.nihongolens.domain.use_case.UpdateAppConfiguration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val app: Application,
    private val getAppConfiguration: GetAppConfiguration,
    private val updateAppConfiguration: UpdateAppConfiguration
) : ViewModel() {
    private val _state = MutableStateFlow(PermissionState())
    val state = _state.asStateFlow()

    fun onEvent(event: PermissionEvent) {
        when (event) {
            is PermissionEvent.RequestPermission -> {
                val doesUserHavePermission =
                    doesUserHavePermission(event.permissionRequest.permission)
                val permissionAlreadyInQueue =
                    state.value.permissionRequestQueue.contains(event.permissionRequest)

                if (doesUserHavePermission || permissionAlreadyInQueue) return

                _state.update {
                    it.copy(
                        permissionRequestQueue = it.permissionRequestQueue + event.permissionRequest
                    )
                }
            }

            is PermissionEvent.OnPermissionResult -> {
                // clear the current permission request
                if (event.granted) {
                    dequeuePermission()
                }

                // Add the permission to the list of previously requested permissions
                // so we can check if it's permanently declined later
                _state.update {
                    it.copy(
                        previouslyRequestedPermissions = it.previouslyRequestedPermissions + event.permission
                    )
                }
                updateAppData()
            }

            is PermissionEvent.Initialize -> {
                // get the app data from the repository
                getAppData()
            }

            PermissionEvent.OnDismissDialog -> {
                // clear the current permission request
                dequeuePermission()
            }
        }
    }


    /**
     * Check if the permission is permanently declined.
     * Intended for use in views to show the appropriate dialog.
     * @param activity The current activity
     * @param permission The permission to check
     * @return True if the permission is permanently declined, false otherwise
     */
    fun isPermissionPermanentlyDeclined(
        activity: Activity, permission: String
    ): Boolean {
        return !ActivityCompat.shouldShowRequestPermissionRationale(
            activity, permission
        ) && !isPermissionRequestFirstTime(permission)
    }

    private fun doesUserHavePermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            app, permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getAppData() {
        viewModelScope.launch {
            val appData = getAppConfiguration()
            _state.update { it.copy(appConfiguration = appData) }
        }
    }

    private fun updateAppData() {
        viewModelScope.launch {
            state.value.appConfiguration.let { updateAppConfiguration(it) }
        }
    }

    private fun isPermissionRequestFirstTime(permission: String): Boolean {
        return !state.value.previouslyRequestedPermissions.contains(permission)
    }

    private fun dequeuePermission() {
        _state.update {
            it.copy(permissionRequestQueue = it.permissionRequestQueue.drop(1))
        }
    }
}