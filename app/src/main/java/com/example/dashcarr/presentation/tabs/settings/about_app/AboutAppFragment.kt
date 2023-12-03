package com.example.dashcarr.presentation.tabs.settings.about_app

import android.os.Bundle
import android.view.View
import com.example.dashcarr.databinding.FragmentAboutAppBinding
import com.example.dashcarr.presentation.core.BaseFragment

/**
 * A Fragment class in the 'DashCarr' application dedicated to displaying information about the app.
 * It extends from [BaseFragment] with a custom binding [FragmentAboutAppBinding].
 */
class AboutAppFragment : BaseFragment<FragmentAboutAppBinding>(
    FragmentAboutAppBinding::inflate,
    showBottomNavBar = false
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backToSettings.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }
}