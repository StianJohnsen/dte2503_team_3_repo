package com.example.dashcarr.presentation.tabs.settings

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentSettingsBinding
import com.example.dashcarr.extensions.collectWithLifecycle
import com.example.dashcarr.presentation.core.BaseFragment
import com.example.dashcarr.presentation.tabs.map.MapViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * This fragment manages the settings tab of the app.
 * It allows users to navigate to various settings like map settings, power settings, social settings, etc.
 * The `SettingsFragment` uses data binding with `FragmentSettingsBinding` and incorporates the `SettingsViewModel` and `MapViewModel` for managing UI states and data.
 * It defines `observeViewModel` and `initListeners` methods for handling UI interactions and observing changes in ViewModel.
 */
@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(
    FragmentSettingsBinding::inflate,
    showBottomNavBar = true
) {
    private val viewModel: SettingsViewModel by viewModels()
    private val mapViewModel: MapViewModel by viewModels()

    private fun observeViewModel() {
        viewModel.logOutState.collectWithLifecycle(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_global_loginFragment)
        }
    }

    private fun initListeners() {
        binding.btnAboutApp.setOnClickListener {
            showBottomNavigation(false)
            findNavController().navigate(R.id.action_action_settings_to_animationSampleFragment)
        }


        binding.btnMapsSettings.setOnClickListener {
            showBottomNavigation(false)
            findNavController().navigate(R.id.action_action_settings_to_mapsSettingsFragment)
        }

        binding.btnNewFeatures.setOnClickListener {
            findNavController().navigate(R.id.action_action_settings_to_productFrontPage)
        }

        binding.btnSocialSettings.setOnClickListener {
            findNavController().navigate(R.id.action_action_settings_to_quickMessagesFragment)
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logOut()
            mapViewModel.updateAppPreferences(false)
        }

        binding.btnPowerSettings.setOnClickListener {
            findNavController().navigate(R.id.action_action_settings_to_powerSettings)
        }

        binding.btnHistorySettings.setOnClickListener {
            findNavController().navigate(R.id.action_action_settings_to_socialSettingsFragment)
        }

        binding.btnDashcamSettings.setOnClickListener {
            findNavController().navigate(R.id.action_action_settings_to_cameraSettings)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        observeViewModel()
        val currentLocal = resources.configuration.locales[0]
        Log.d("spraak", currentLocal.language)

    }
}