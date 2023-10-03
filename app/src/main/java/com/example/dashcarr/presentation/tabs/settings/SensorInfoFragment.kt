package com.example.dashcarr.presentation.tabs.settings

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentSensorInfoBinding
import com.example.dashcarr.presentation.core.BaseFragment


class SensorInfoFragment : BaseFragment<FragmentSensorInfoBinding>(
    FragmentSensorInfoBinding::inflate
) {






    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val unfilteredFile = requireContext().openFileInput("unfiltered_sensor_data.csv")
        val filteredFile = requireContext().openFileInput("filtered_sensor_data.csv")

        binding.filltFileSize.text = "Filtered filesize: ${filteredFile.available()} BYTES"
        binding.unfilltFileSize.text = "Unfiltered filesize: ${unfilteredFile.available()} BYTES"

    }




}