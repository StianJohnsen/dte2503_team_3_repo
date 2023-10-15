package com.example.dashcarr.presentation.tabs.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentSettingsBinding
import com.example.dashcarr.extensions.collectWithLifecycle
import com.example.dashcarr.presentation.core.BaseFragment
import com.example.dashcarr.presentation.tabs.map.MapViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(
    FragmentSettingsBinding::inflate,
    showBottomNavBar = true
) {
    private val viewModel: SettingsViewModel by viewModels()
    private val mapViewModel: MapViewModel by viewModels()

     fun observeViewModel() {
        viewModel.logOutState.collectWithLifecycle(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_global_loginFragment)
        }
    }

     fun initListeners() {
        binding.btnProfileSettings.setOnClickListener {
            showBottomNavigation(false)
            findNavController().navigate(R.id.action_action_settings_to_animationSampleFragment)
        }
        binding.btnLogout.setOnClickListener {
            viewModel.logOut()
            mapViewModel.updateAppPreferences(false)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        observeViewModel()

    }


}