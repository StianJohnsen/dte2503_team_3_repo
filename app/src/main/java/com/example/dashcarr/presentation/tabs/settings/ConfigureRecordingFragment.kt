package com.example.dashcarr.presentation.tabs.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.dashcarr.databinding.FragmentConfigureRecordingBinding
import com.example.dashcarr.presentation.core.BaseFragment
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.google.android.material.bottomnavigation.BottomNavigationView


class ConfigureRecordingFragment : BaseFragment<FragmentConfigureRecordingBinding>(
    FragmentConfigureRecordingBinding::inflate
) {
    private lateinit var bottomNavigationView: BottomNavigationView
    private val viewModel: SavedRecordingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)!!
        bottomNavigationView?.visibility = View.VISIBLE
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            configureRecordingFragment = this@ConfigureRecordingFragment
        }
        binding.buttonStartRecording.setOnClickListener {
            findNavController().navigate(R.id.action_action_configure_to_RecordingFragment)
        }
        binding.imageBackConfigure.setOnClickListener {
            findNavController().navigate(R.id.action_action_configure_to_SettingsFragment)
        }
    }
}