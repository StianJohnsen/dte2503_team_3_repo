package com.example.dashcarr.presentation.tabs.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentSettingsBinding
import com.example.dashcarr.presentation.core.BaseFragment


class SettingsFragment : BaseFragment<FragmentSettingsBinding>(
    FragmentSettingsBinding::inflate
) {
    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            plusIconView.setOnClickListener { moveToSensorView() }
        }
    }

    fun moveToSensorView() {
        findNavController().navigate(R.id.action_action_settings_to_sensorFragment)
    }


}