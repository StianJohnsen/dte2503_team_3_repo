package com.example.dashcarr.presentation.tabs.camera.dashcam

import android.app.Activity
import androidx.camera.core.CameraSelector
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.R
import com.example.dashcarr.data.repository.UserPreferencesRepository
import com.example.dashcarr.presentation.tabs.camera.CameraWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CancellationException
import javax.inject.Inject

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
@HiltViewModel
class DashcamViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val userPreferencesLiveData = userPreferencesRepository.userPreferenceFlow.asLiveData()
    private var camera: CameraWrapper? = null
    private lateinit var dashcamJob: Job

    fun closeCamera() {
        camera?.destroy()
        camera = null
    }

    fun activate(activity: Activity, fragment: Fragment, cameraDuration: Int, slideIn: () -> Unit) {
        if (camera == null) {
            camera = CameraWrapper(activity)
        }
        camera!!.startCamera(
            activity.findViewById(R.id.dashcam_preview),
            fragment,
            CameraSelector.DEFAULT_BACK_CAMERA
        ) {
            slideIn()
            dashcamJob = viewModelScope.launch {
                recordingLoop(cameraDuration)
            }
        }
    }

    private suspend fun recordingLoop(cameraDuration: Int) {
        val files = emptyList<Path>().toMutableList()
        while (camera != null) {
            files.add(0, camera!!.startRecording({ }, "Movies/Dashcarr/Dashcam")!!)
            viewModelScope.launch {
                if (files.size > 2) {
                    withContext(Dispatchers.IO) {
                        Files.deleteIfExists(files.removeLast())
                    }
                }
            }
            delay(cameraDuration * 1000L * 60)
            camera!!.stopRecording {}
        }
    }

    suspend fun deactivate() {
        dashcamJob.cancel(CancellationException("Dashcam mode deactivated"))
        dashcamJob.join()
        camera?.stopRecording {}
    }
}