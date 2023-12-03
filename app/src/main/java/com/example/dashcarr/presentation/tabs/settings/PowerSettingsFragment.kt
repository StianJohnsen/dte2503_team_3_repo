package com.example.dashcarr.presentation.tabs.settings

import android.content.Context
import android.os.BatteryManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentPowerSettingsBinding
import com.example.dashcarr.presentation.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * This fragment is responsible for displaying the power settings screen.
 * It uses a spinner to display the power settings options.
 * It also displays the battery lost percentage.
 */
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
        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(
                requireContext(),
                R.layout.spinner_item,
                resources.getStringArray(R.array.power_settings_array)
            )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

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
                // NOOP
            }
        }

        binding.apply {
            backToSettings.setOnClickListener {
                requireActivity().onBackPressed()
            }

            val batteryManager: BatteryManager =
                requireContext().getSystemService(AppCompatActivity.BATTERY_SERVICE) as BatteryManager
            val currentCapacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val lostPercentages = viewModel.getLostBatteryPercentages(currentCapacity)
            batteryLostPercentage.text = if (lostPercentages >= 0) getString(
                R.string.lost_percentage,
                lostPercentages
            ) else getString(R.string.gained_percentage, -1 * lostPercentages)
        }
    }

}