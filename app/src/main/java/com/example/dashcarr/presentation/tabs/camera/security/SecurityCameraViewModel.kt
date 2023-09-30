package com.example.dashcarr.presentation.tabs.camera.security

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.example.dashcarr.R
import com.example.dashcarr.presentation.tabs.camera.CameraWrapper

class SecurityCameraViewModel : ViewModel() {

    private lateinit var camera: CameraWrapper
    public fun initViewModel(activity: FragmentActivity, fragment: Fragment) {
        if (!this::camera.isInitialized) {
            camera = CameraWrapper(activity)
        }

        if (!camera.askForPermission()) return
        camera.startCamera(activity.findViewById(R.id.video_preview), fragment) {

        }
    }

    public fun start() {
        if (!camera.isRecording()) {
            camera.startRecording({

            }, "Movies/Dashcarr/Security_Camera")
        } else {
            camera.stopRecording({

            })
        }

    }

}