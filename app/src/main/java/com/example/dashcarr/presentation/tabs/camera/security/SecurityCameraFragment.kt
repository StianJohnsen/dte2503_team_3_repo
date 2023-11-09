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
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentSecurityCameraBinding
import com.example.dashcarr.presentation.core.BaseFragment
import com.example.dashcarr.presentation.tabs.settings.PowerSavingMode
import kotlin.math.abs

class SecurityCameraFragment : BaseFragment<FragmentSecurityCameraBinding>(
    FragmentSecurityCameraBinding::inflate,
    showBottomNavBar = false
), SensorEventListener {
    private val viewModel: SecurityCameraViewModel by viewModels()
    private val authenticators =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    private var lastAcceleration: FloatArray? = null
    private var filteredAcceleration = 0F
    private val filterValue = 0.3F
    private val sensorManager by lazy { requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (PowerSavingMode.getPowerMode()) {
            Toast.makeText(context, "Recording increases the power consumption!", Toast.LENGTH_SHORT).show()
        }

        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                viewModel.startCamera(requireActivity(), this) {
                    binding.videoCaptureButton.apply {
                        text =
                            getString(if (viewModel.isRecording()) R.string.stop_recording else R.string.start_recording)
                        isClickable = true
                    }
                }
            } else {
                Toast.makeText(requireActivity(), "This feature needs camera access", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            }
        }.launch(Manifest.permission.CAMERA)

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.videoCaptureButton.setOnClickListener {
            binding.videoCaptureButton.isClickable = false
            if (viewModel.isRecording()) {
                stopTriggered()
            } else {
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

    private fun stopTriggered() {
        val stopRecording = {
            viewModel.stopRecording(showStartButton = {
                binding.videoCaptureButton.apply {
                    text = getString(R.string.start_recording)
                    isClickable = true
                }
                if (binding.recordingSign.visibility == View.VISIBLE) {
                    binding.recordingSign.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fadeout))
                }
            })
        }

        if (hasBiometricCapability()) {
            showPrompt(success = stopRecording, failed = {
                stopRecording()
                Toast.makeText(requireContext(), "send message", Toast.LENGTH_SHORT).show()
            })
        } else {
            stopRecording()
        }
    }

    private fun hasBiometricCapability(): Boolean {
        val isBiometricReady = BiometricManager.from(requireContext())
            .canAuthenticate(authenticators)
        return isBiometricReady == BiometricManager.BIOMETRIC_SUCCESS
    }


    private fun showPrompt(success: () -> Unit, failed: () -> Unit) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("title")
            .setSubtitle("subtitle")
            .setDescription("description")
            .setAllowedAuthenticators(authenticators)
            .build()
        var isClosed = false
        val executor = ContextCompat.getMainExecutor(requireActivity())
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Log.e(
                    this::class.simpleName,
                    "Error while Biometric Prompt with error code: $errorCode and description: $errString"
                )
                isClosed = true
            }

            override fun onAuthenticationFailed() {
                isClosed = true
                failed()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                isClosed = true
                success()
            }
        }
        val biometricPrompt = BiometricPrompt(requireActivity(), executor, callback)
        biometricPrompt.authenticate(promptInfo)
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isClosed) {
                biometricPrompt.cancelAuthentication()
                Toast.makeText(context, "Time Expired!", Toast.LENGTH_SHORT).show()
                failed()
            }
        }, 10000)
    }

    override fun onDestroyView() {
        viewModel.closeCamera()
        super.onDestroyView()
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
            sensorManager.unregisterListener(this)
            lastAcceleration = null
            filteredAcceleration = 0F
            stopTriggered()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // NOOP
    }

}