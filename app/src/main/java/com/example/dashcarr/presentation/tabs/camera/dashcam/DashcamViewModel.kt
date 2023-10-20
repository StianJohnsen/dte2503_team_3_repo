package com.example.dashcarr.presentation.tabs.camera.dashcam

import android.app.Activity
import androidx.camera.core.CameraSelector
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.example.dashcarr.R
import com.example.dashcarr.presentation.tabs.camera.CameraWrapper

class DashcamViewModel : ViewModel() {

    private var camera: CameraWrapper? = null

    fun closeCamera() {
        camera?.destroy()
        camera = null
    }

    fun startRecording(activity: Activity, fragment: Fragment, slideIn: () -> Unit) {
        if (camera == null) {
            camera = CameraWrapper(activity)
        }
        camera!!.startCamera(
            activity.findViewById(R.id.dashcam_preview),
            fragment,
            CameraSelector.DEFAULT_BACK_CAMERA
        ) {
            slideIn()
            camera!!.startRecording({}, "Movies/Dashcarr/Dashcam")
        }
    }

    fun saveRecording(destroy: () -> Unit) {
        camera!!.stopRecording {
            destroy()
        }

    }


}