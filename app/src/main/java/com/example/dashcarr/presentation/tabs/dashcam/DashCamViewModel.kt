package com.example.dashcarr.presentation.tabs.dashcam

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.dashcarr.presentation.MainActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DashCamViewModel @Inject constructor(
) : ViewModel() {

    private var videoIntent: Intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
    private lateinit var launcher: ActivityResultLauncher<Intent>
    fun initViewModel(activity: MainActivity) {
        launcher =
            activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                if (it.resultCode == Activity.RESULT_OK) {
                    Toast.makeText(
                        activity.baseContext, "Video saved to:\n"
                                + it.data?.data, Toast.LENGTH_LONG
                    ).show()
                    Log.e("test", it.data.toString())
                }
            }
    }

    /**
     * Starts the camera intent if the permissions are granted.
     * If permissions are missing, it will request the permission without starting the intent.
     */
    fun startRecording(activity: MainActivity) {
        val hasPermission = ContextCompat.checkSelfPermission(
            activity.baseContext,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launcher.launch(videoIntent)
        } else {
            ActivityCompat.requestPermissions(
                activity, arrayOf(android.Manifest.permission.CAMERA), 1
            )
        }
    }
}