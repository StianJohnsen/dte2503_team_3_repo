package com.example.dashcarr.presentation.tabs.camera.security

import androidx.camera.core.CameraSelector
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.example.dashcarr.R
import com.example.dashcarr.presentation.tabs.camera.CameraWrapper

class SecurityCameraViewModel : ViewModel() {

    private lateinit var camera: CameraWrapper
    fun startCamera(activity: FragmentActivity, fragment: Fragment, showButton: () -> Unit) {
        if (!this::camera.isInitialized) {
            camera = CameraWrapper(activity)
        }
        if (!camera.isCameraStarted()) {
            camera.startCamera(
                activity.findViewById(R.id.video_preview),
                fragment,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                showButton
            )
        }
    }

    fun startRecording(showStopButton: () -> Unit) {
        camera.startRecording(showStopButton, "Movies/Dashcarr/Security_Camera")
    }

    fun stopRecording(showStartButton: () -> Unit) {
        camera.stopRecording(showStartButton)
    }

    fun isRecording(): Boolean {
        return camera.isRecording()
    }

    fun closeCamera() {
        if (this::camera.isInitialized) {
            camera.destroy()
        }
    }
}