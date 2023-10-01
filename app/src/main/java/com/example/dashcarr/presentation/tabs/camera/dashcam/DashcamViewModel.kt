package com.example.dashcarr.presentation.tabs.camera.dashcam

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.example.dashcarr.R
import com.example.dashcarr.presentation.tabs.camera.CameraWrapper

class DashcamViewModel : ViewModel() {

    private lateinit var camera: CameraWrapper
    public fun initViewModel(
        activity: FragmentActivity,
        fragment: Fragment,
        recordingStarted: () -> Unit,
        recordingStopped: () -> Unit
    ) {
        if (!this::camera.isInitialized) {
            camera = CameraWrapper(activity)
        }
        if (!camera.isCameraStarted()) {
            camera.startCamera(activity.findViewById(R.id.dashcam_preview), fragment) {
                camera.startRecording(recordingStarted, "Movies/Dashcarr/Dashcam")
            }
        } else if (camera.isRecording()) {
            camera.stopRecording {
                recordingStopped()
                camera.destroy()
            }
        } else {
            camera.startRecording(recordingStarted, "Movies/Dashcarr/Dashcam")
        }

    }

    public fun closeCamera() {
        camera.destroy()
    }

}