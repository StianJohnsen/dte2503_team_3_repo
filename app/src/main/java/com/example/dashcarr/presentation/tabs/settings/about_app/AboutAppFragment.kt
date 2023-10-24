package com.example.dashcarr.presentation.tabs.settings.about_app

import android.os.Bundle
import android.view.View
import com.example.dashcarr.databinding.FragmentAnimationSampleBinding
import com.example.dashcarr.presentation.core.BaseFragment

/**
 * Fragment showcasing experimental features.
 * For now its animation samples.
 */
class AboutAppFragment : BaseFragment<FragmentAnimationSampleBinding>(
    FragmentAnimationSampleBinding::inflate,
    showBottomNavBar = false
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backToSettings.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

}