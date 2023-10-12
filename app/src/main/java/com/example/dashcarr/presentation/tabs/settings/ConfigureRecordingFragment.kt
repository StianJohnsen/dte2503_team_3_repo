package com.example.dashcarr.presentation.tabs.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentConfigureRecordingBinding
import com.example.dashcarr.presentation.core.BaseFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class ConfigureRecordingFragment : BaseFragment<FragmentConfigureRecordingBinding>(
    FragmentConfigureRecordingBinding::inflate,
    showBottomNavBar = false
) {
    private lateinit var bottomNavigationView: BottomNavigationView

    private var isFilteredChecked = false
    private var isAccelChecked = false
    private var isGyroChecked = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        bottomNavigationView = activity?.findViewById(R.id.bottom_nav)!!
        bottomNavigationView.visibility = View.VISIBLE
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            configureRecordingFragment = this@ConfigureRecordingFragment
            buttonStartRecording.setOnClickListener {
                startRecording()
            }
            imageBackConfigure.setOnClickListener {
                findNavController().navigate(R.id.action_action_configure_to_SettingsFragment)
            }
            checkBoxFilterData.setOnCheckedChangeListener { _, isChecked ->
                isFilteredChecked = isChecked


            }
            checkBoxAcc.setOnCheckedChangeListener { _, isChecked ->
                isAccelChecked = isChecked
            }

            checkBoxGyro.setOnCheckedChangeListener { _, isChecked ->
                isGyroChecked = isChecked
            }
        }


    }


    private fun startRecording() {
        Log.d("checking", isFilteredChecked.toString() + "\n" + isAccelChecked + "\n" + isGyroChecked)
        val args = Bundle()
        args.putBoolean("isFiltered", isFilteredChecked)
        args.putBoolean("isAccel", isAccelChecked)
        args.putBoolean("isGyro", isGyroChecked)

        findNavController().navigate(R.id.action_action_configure_to_RecordingFragment, args)

    }
}