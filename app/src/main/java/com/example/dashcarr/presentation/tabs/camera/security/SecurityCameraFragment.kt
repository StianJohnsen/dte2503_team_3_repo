package com.example.dashcarr.presentation.tabs.camera.security

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.dashcarr.databinding.FragmentSecurityCameraBinding
import com.example.dashcarr.presentation.core.BaseFragment

class SecurityCameraFragment : BaseFragment<FragmentSecurityCameraBinding>(
    FragmentSecurityCameraBinding::inflate
) {
    private val viewModel: SecurityCameraViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}