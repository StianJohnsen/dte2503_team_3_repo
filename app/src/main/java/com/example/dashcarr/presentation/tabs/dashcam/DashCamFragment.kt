package com.example.dashcarr.presentation.tabs.dashcam

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.dashcarr.databinding.FragmentDashCamBinding
import com.example.dashcarr.presentation.core.BaseFragment

class DashCamFragment : BaseFragment<FragmentDashCamBinding>(
    FragmentDashCamBinding::inflate
) {
    private val viewModel: DashCamViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}