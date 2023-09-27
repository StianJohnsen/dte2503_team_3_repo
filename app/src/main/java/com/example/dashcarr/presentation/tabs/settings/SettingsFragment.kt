package com.example.dashcarr.presentation.tabs.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentSettingsBinding
import com.example.dashcarr.presentation.core.BaseFragment



class SettingsFragment : BaseFragment<FragmentSettingsBinding>(
    FragmentSettingsBinding::inflate
) {
    private lateinit var bottomNavigationView: BottomNavigationView
    private val viewModel: SettingsViewModel by viewModels()

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
            settingsFragment = this@SettingsFragment
        }
        binding.button2.setOnClickListener {
            findNavController().navigate(R.id.action_action_settings_to_SavedRecordingsFragment)
        }
    }
}
/*
Log.d("SettingsFragment", "moveToRecordings called")
        try {
            findNavController().navigate(R.id.go_to_recordings)
        } catch (e: Exception) {
            Log.e("SettingsFragment", "Navigation failed", e)
        }
 */