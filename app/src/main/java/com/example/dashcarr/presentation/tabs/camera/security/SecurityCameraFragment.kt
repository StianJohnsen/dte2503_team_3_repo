package com.example.dashcarr.presentation.tabs.camera.security

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.viewModels
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentSecurityCameraBinding
import com.example.dashcarr.presentation.core.BaseFragment

class SecurityCameraFragment : BaseFragment<FragmentSecurityCameraBinding>(
    FragmentSecurityCameraBinding::inflate
) {
    private val viewModel: SecurityCameraViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initViewModel(requireActivity(), this)
        requireActivity().findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            requireActivity().onBackPressed()
        }
        requireActivity().findViewById<Button>(R.id.video_capture_button).setOnClickListener {
            viewModel.start()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        viewModel.initViewModel(requireActivity(), this)
    }
}