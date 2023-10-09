package com.example.dashcarr.presentation.tabs.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
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
    private val viewModel: ConfigureRecordingViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private var startTimeMillis: Long = 0

    var isFilteredChecked = false
    var isAccelChecked = false
    var isGyroChecked = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun observeViewModel() {
        TODO("Not yet implemented")
    }

    override fun initListeners() {
        TODO("Not yet implemented")
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
                startRecording()
            }
            checkBoxFilterData.setOnCheckedChangeListener { buttonView, isChecked ->
                isFilteredChecked = isChecked


            }
            checkBoxAcc.setOnCheckedChangeListener { buttonView, isChecked ->
                isAccelChecked = isChecked
            }

            checkBoxGyro.setOnCheckedChangeListener { buttonView, isChecked ->
                isGyroChecked = isChecked
            }
        }


    }


    fun startRecording() {
        Log.d("checking", isFilteredChecked.toString() + "\n" + isAccelChecked + "\n" + isGyroChecked)
        val args = Bundle()
        args.putBoolean("isFiltered", isFilteredChecked)
        args.putBoolean("isAccel", isAccelChecked)
        args.putBoolean("isGyro", isGyroChecked)
        /*
                val action = ConfigureRecordingFragmentDirections.actionActionConfigureToRecordingFragment(
                    isFilteredChecked,
                    isAccelChecked,
                    isGyroChecked
                )
         */

        findNavController().navigate(R.id.action_action_configure_to_RecordingFragment, args)

    }
}