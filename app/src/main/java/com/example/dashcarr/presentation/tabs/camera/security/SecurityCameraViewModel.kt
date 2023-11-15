package com.example.dashcarr.presentation.tabs.camera.security

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.example.dashcarr.R
import com.example.dashcarr.presentation.tabs.camera.CameraWrapper

class SecurityCameraViewModel : ViewModel() {
    enum class States {
        NOT_STARTED, CAMERA_STARTED, RECORDING, AUTHORIZED, WAIT_FOR_AUTHORIZATION
    }

    class StateMachine {
        private var state = States.NOT_STARTED
        fun getCurrentState(): States = state

        fun startCamera() {
            if (state == States.NOT_STARTED) {
                state = States.CAMERA_STARTED
            } else {
                throw IllegalStateException("Camera was already started")
            }
        }

        fun startRecording() {
            if (state == States.CAMERA_STARTED) {
                state = States.RECORDING
            } else {
                throw IllegalStateException("Can't start a recording in state ${state.name}")
            }
        }

        fun stopCamera() {
            state = States.NOT_STARTED
        }

        fun triggerStop() {
            if (state == States.RECORDING) {
                state = States.WAIT_FOR_AUTHORIZATION
            } else {
                throw IllegalStateException("To trigger a stop, the camera needs to be recording. Current State: ${state.name}")
            }
        }

        fun authorize() {
            if (state == States.WAIT_FOR_AUTHORIZATION) {
                state = States.AUTHORIZED
            } else {
                throw IllegalStateException("The camera is not waiting for a authorization. Current State: ${state.name}")
            }
        }

        fun stopRecording() {
            if (state == States.AUTHORIZED) {
                state = States.CAMERA_STARTED
            } else {
                throw IllegalStateException("The camera is authorized. Current State: ${state.name}")
            }
        }
    }


    private lateinit var camera: CameraWrapper
    private val state = StateMachine()
    fun getState(): StateMachine = state

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
        state.startCamera()
    }

    fun startRecording(showStopButton: () -> Unit) {
        camera.startRecording(showStopButton, "Movies/Dashcarr/Security_Camera")
        state.startRecording()
    }

    fun stopRecording(showStartButton: () -> Unit = {}) {
        camera.stopRecording(showStartButton)
        state.stopRecording()
    }

    fun closeCamera() {
        if (this::camera.isInitialized) {
            camera.destroy()
            state.stopCamera()
        }
    }

    fun sendMessage() {
        Log.i(this::class.simpleName, "Send message")
    }
}