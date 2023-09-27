package com.example.dashcarr.presentation.tabs.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.dashcarr.databinding.FragmentSettingsBinding
import com.example.dashcarr.presentation.core.BaseFragment


class RecordingDetailsFragment : BaseFragment<FragmentSettingsBinding>(
    FragmentSettingsBinding::inflate
) {
    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}