package com.example.dashcarr.presentation.tabs.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.activityViewModels
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentPowerSettingsBinding
import com.example.dashcarr.presentation.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PowerSettingsFragment : BaseFragment<FragmentPowerSettingsBinding>(
    FragmentPowerSettingsBinding::inflate,
    showBottomNavBar = false
) {

    val viewModel: PowerSettingsViewModel by activityViewModels()
    private val sharedPreferences by lazy {
        requireActivity().getSharedPreferences("PowerSettingsPreferences", Context.MODE_PRIVATE)
    }

    private val spinnerValueKey = "selectedSpinnerValue"


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinner = binding.powerSettingsSpinner
        val savedValue = sharedPreferences.getInt(spinnerValueKey, 0)

        spinner.setSelection(savedValue)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                sharedPreferences.edit().putInt(spinnerValueKey, position).apply()

                val selectedValue = spinner.selectedItem
                val testPower = PowerSavingMode.PowerState.valueOf(selectedValue.toString().uppercase())
                viewModel.setPowerSetting(testPower.ordinal)
            }


            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        binding.apply {
            backToSettings.setOnClickListener {
                requireActivity().onBackPressed()
            }
            batteryLostPercentage.text = getString(R.string.lost_percentage, 5)
        }
    }

}