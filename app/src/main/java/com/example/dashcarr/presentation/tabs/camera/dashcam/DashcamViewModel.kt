package com.example.dashcarr.presentation.tabs.camera.dashcam

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.example.dashcarr.R
import com.example.dashcarr.presentation.tabs.camera.CameraWrapper

class DashcamViewModel : ViewModel() {


    private lateinit var camera: CameraWrapper
    public fun initViewModel(activity: FragmentActivity, fragment: Fragment) {
        if (!this::camera.isInitialized) {
            camera = CameraWrapper(activity)
        }
        if (!camera.isCameraStarted()) {
            camera.startCamera(activity.findViewById(R.id.dashcam_preview), fragment) {
                if (camera.askForPermission()) {
                    camera.startRecording({
//                        activity.findViewById<PreviewView>(R.id.dashcam_preview).visibility = View.VISIBLE
                    }, "Movies/Dashcarr/Dashcam")
                }
            }
        }
        if (camera.isRecording()) {
            camera.stopRecording() {
                camera.destroy()
//                activity.findViewById<PreviewView>(R.id.dashcam_preview).visibility = View.GONE
                Log.e("test", "ende")
            }
        }

    }

}