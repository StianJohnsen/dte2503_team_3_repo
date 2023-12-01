package com.example.dashcarr.presentation.tabs.camera.security

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentSecurityCameraBinding
import com.example.dashcarr.presentation.core.BaseFragment
import com.example.dashcarr.presentation.tabs.settings.PowerSavingMode
import kotlin.math.abs

/**
 * Fragment for handling security camera functionalities in the app.
 * Manages the camera for security purposes, including starting and stopping recording based on sensor data.
 * Also handles biometric authentication for stopping the recording.
 *
 * It utilizes a ViewModel to handle camera operations and biometric authentication logic.
 * The fragment integrates sensor event listeners to detect changes that may trigger the stop of recording.
 *
 * @property viewModel ViewModel associated with security camera functionalities, handling camera and sensor operations.
 * @property currentState Holds the current state of the security camera using StateMachine pattern.
 * @property biometricPrompt Manages the biometric authentication prompt.
 * @property authenticators Authentication types supported for biometric authentication.
 * @property lastAcceleration Stores the last accelerometer reading for movement detection.
 * @property filteredAcceleration Filtered value for acceleration to detect significant movements.
 * @property sensorManager Manages sensor-related operations.
 */
class SecurityCameraFragment : BaseFragment<FragmentSecurityCameraBinding>(
    FragmentSecurityCameraBinding::inflate,
    showBottomNavBar = false
), SensorEventListener {

    private val viewModel: SecurityCameraViewModel by viewModels()
    private lateinit var currentState: SecurityCameraViewModel.StateMachine
    private lateinit var biometricPrompt: BiometricPrompt
    private val authenticators =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    private var lastAcceleration: FloatArray? = null
    private var filteredAcceleration = 0F
    private val filterValue = 0.3F
    private val sensorManager by lazy { requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentState = viewModel.getState()
        if (PowerSavingMode.getPowerMode()) {
            Toast.makeText(context, "Recording increases the power consumption!", Toast.LENGTH_SHORT).show()
        }

        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                if (currentState.getCurrentState() == SecurityCameraViewModel.States.NOT_STARTED) {
                    viewModel.startCamera(requireActivity(), this) {
                        binding.videoCaptureButton.apply {
                            text = getString(R.string.start_recording)
                            isClickable = true
                        }
                    }
                }
            } else {
                Toast.makeText(requireActivity(), "Can't access camera", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_action_security_camera_to_action_map)
            }
        }.launch(Manifest.permission.CAMERA)

        binding.backButton.setOnClickListener {
            val action =
                SecurityCameraFragmentDirections.actionActionSecurityCameraToActionMap(
                    isRideActivated = true
                )
            findNavController().navigate(action)
        }
        binding.videoCaptureButton.setOnClickListener {
            binding.videoCaptureButton.isClickable = false
            if (currentState.getCurrentState() == SecurityCameraViewModel.States.RECORDING) {
                stopTriggered()
            } else if (currentState.getCurrentState() == SecurityCameraViewModel.States.CAMERA_STARTED) {
                viewModel.startRecording(
                    showStopButton = {
                        binding.videoCaptureButton.apply {
                            text = getString(R.string.stop_recording)
                            isClickable = true
                        }
                        binding.recordingSign.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fadein))
                        binding.recordingSign.visibility = View.VISIBLE

                        val accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                        sensorManager.registerListener(
                            this, accelerationSensor, SensorManager.SENSOR_DELAY_NORMAL
                        )
                    })
            }
        }
    }

    private fun stopRecording() {
        currentState.authorize()
        viewModel.stopRecording(showStartButton = {
            if (context != null) {
                Toast.makeText(activity, "Video saved", Toast.LENGTH_SHORT).show()
                binding.videoCaptureButton.apply {
                    text = getString(R.string.start_recording)
                    isClickable = true
                }
                if (binding.recordingSign.visibility == View.VISIBLE) {
                    binding.recordingSign.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fadeout))
                }
            }
        })
    }

    private fun stopTriggered() {
        unregisterAccelerationListener()
        binding.videoCaptureButton.isClickable = false
        currentState.triggerStop()

        if (hasBiometricCapability()) {
            showPrompt()
        } else {
            stopRecording()
        }
    }

    private fun showPrompt() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.prompt_title))
            .setSubtitle(getString(R.string.prompt_subtitle))
            .setDescription(getString(R.string.prompt_description))
            .setAllowedAuthenticators(authenticators)
            .build()
        val executor = ContextCompat.getMainExecutor(requireActivity())
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Log.e(
                    this::class.simpleName,
                    "Error while Biometric Prompt with error code: $errorCode and description: $errString"
                )
            }

            override fun onAuthenticationFailed() {
                stopRecording()
                viewModel.sendMessage()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                stopRecording()
            }
        }
        biometricPrompt = BiometricPrompt(requireActivity(), executor, callback)
        biometricPrompt.authenticate(promptInfo)
        Handler(Looper.getMainLooper()).postDelayed({
            if (currentState.getCurrentState() == SecurityCameraViewModel.States.WAIT_FOR_AUTHORIZATION) {
                biometricPrompt.cancelAuthentication()
                stopRecording()
                viewModel.sendMessage()
            }
        }, 10000)
    }

    private fun hasBiometricCapability(): Boolean {
        val isBiometricReady = BiometricManager.from(requireContext())
            .canAuthenticate(authenticators)
        return isBiometricReady == BiometricManager.BIOMETRIC_SUCCESS
    }

    override fun onDetach() {
        if (currentState.getCurrentState() == SecurityCameraViewModel.States.WAIT_FOR_AUTHORIZATION) {
            biometricPrompt.cancelAuthentication()
            stopRecording()
            viewModel.sendMessage()
        }
        viewModel.closeCamera()
        super.onDetach()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.values == null) return
        if (lastAcceleration == null) {
            lastAcceleration = event.values
            return
        }
        val currentAcceleration = lastAcceleration!!.zip(event.values).maxOfOrNull { abs(it.first - it.second) }!!
        filteredAcceleration = (1 - filterValue) * filteredAcceleration + filterValue * currentAcceleration
        lastAcceleration = event.values.copyOf()
        if (filteredAcceleration > 1.0F) {
            stopTriggered()
        }
    }

    private fun unregisterAccelerationListener() {
        sensorManager.unregisterListener(this)
        lastAcceleration = null
        filteredAcceleration = 0F
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

}