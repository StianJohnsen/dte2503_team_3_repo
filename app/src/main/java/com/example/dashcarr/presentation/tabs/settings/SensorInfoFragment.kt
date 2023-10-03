package com.example.dashcarr.presentation.tabs.settings

import android.os.Bundle
import android.view.View
import com.example.dashcarr.databinding.FragmentSensorInfoBinding
import com.example.dashcarr.presentation.core.BaseFragment


class SensorInfoFragment : BaseFragment<FragmentSensorInfoBinding>(
    FragmentSensorInfoBinding::inflate
) {
    override fun observeViewModel() {
        TODO("Not yet implemented")
    }

    override fun initListeners() {
        TODO("Not yet implemented")
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val unfilteredFile = requireContext().openFileInput("unfiltered_sensor_data.csv")
        val filteredFile = requireContext().openFileInput("filtered_sensor_data.csv")

        binding.filltFileSize.text = "Filtered filesize: ${filteredFile.available()} BYTES"
        binding.unfilltFileSize.text = "Unfiltered filesize: ${unfilteredFile.available()} BYTES"

    }


}