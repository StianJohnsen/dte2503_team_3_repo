package com.example.dashcarr.presentation.tabs.camera.security

import android.Manifest
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentSecurityCameraBinding
import com.example.dashcarr.presentation.core.BaseFragment

class SecurityCameraFragment : BaseFragment<FragmentSecurityCameraBinding>(
    FragmentSecurityCameraBinding::inflate,
    showBottomNavBar = false
) {
    private val viewModel: SecurityCameraViewModel by viewModels()

    private val showStartButton: () -> Unit by lazy {
        {
            binding.videoCaptureButton.apply {
                text = getString(R.string.start_recording)
                isClickable = true
            }
            if (binding.recordingSign.visibility == View.VISIBLE) {
                binding.recordingSign.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fadeout))
            }
        }
    }
    private val showStopButton: () -> Unit by lazy {
        {
            binding.videoCaptureButton.apply {
                text = getString(R.string.stop_recording)
                isClickable = true
            }
            binding.recordingSign.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fadein))
            binding.recordingSign.visibility = View.VISIBLE
        }
    }
    private val hideButton: () -> Unit by lazy {
        {
            binding.videoCaptureButton.isClickable = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.videoCaptureButton.setOnClickListener {
            viewModel.changeRecordingState(showStartButton, showStopButton, hideButton)
        }

        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                viewModel.initViewModel(requireActivity(), this, showStartButton)
            } else {
                Toast.makeText(requireActivity(), "This feature needs camera access", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            }
        }.launch(Manifest.permission.CAMERA)

    }

    override fun onDestroyView() {
        viewModel.closeCamera()
        super.onDestroyView()
    }



}