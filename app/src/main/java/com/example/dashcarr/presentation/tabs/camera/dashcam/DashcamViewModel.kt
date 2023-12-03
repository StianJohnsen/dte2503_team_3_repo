package com.example.dashcarr.presentation.tabs.camera.dashcam

import android.app.Activity
import androidx.camera.core.CameraSelector
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.example.dashcarr.R
import com.example.dashcarr.presentation.tabs.camera.CameraWrapper

/**
 * ViewModel associated with the Dashcame.
 * Manages the camera operations including starting and stopping the recording.
 *
 * This ViewModel interacts with the CameraWrapper to abstract the complexities
 * of camera functionalities and to provide an easy interface for starting and
 * stopping recordings, as well as managing camera resources.
 *
 * @property camera An instance of CameraWrapper to handle camera operations.
 */
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