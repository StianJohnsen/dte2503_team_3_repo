package com.example.dashcarr.presentation

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentAnimationSampleBinding
import com.example.dashcarr.presentation.core.BaseFragment

/**
 * Fragment showcasing experimental features.
 * For now its animation samples.
 */
class AnimationSampleFragment : BaseFragment<FragmentAnimationSampleBinding>(
    FragmentAnimationSampleBinding::inflate,
    showBottomNavBar = false
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backToSettings.setOnClickListener {
            findNavController().navigate(R.id.action_animationSampleFragment_to_action_settings)
        }
    }

}