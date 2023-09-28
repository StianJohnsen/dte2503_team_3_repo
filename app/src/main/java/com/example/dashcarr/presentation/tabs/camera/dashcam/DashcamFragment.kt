package com.example.dashcarr.presentation.tabs.camera.dashcam

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.dashcarr.databinding.FragmentDashcamBinding
import com.example.dashcarr.presentation.core.BaseFragment

class DashcamFragment : BaseFragment<FragmentDashcamBinding>(
    FragmentDashcamBinding::inflate
) {
    private val viewModel: DashcamViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}