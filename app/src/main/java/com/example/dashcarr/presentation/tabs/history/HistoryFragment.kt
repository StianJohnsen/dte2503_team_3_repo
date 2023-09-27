package com.example.dashcarr.presentation.tabs.history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.dashcarr.data.entity.GeoPointEntity
import com.example.dashcarr.databinding.FragmentHistoryBinding
import com.example.dashcarr.presentation.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment : BaseFragment<FragmentHistoryBinding>(
    FragmentHistoryBinding::inflate
) {
    private val viewModel: HistoryViewModel by viewModels()

    override fun observeViewModel() {
        TODO("Not yet implemented")
    }

    override fun initListeners() {
        TODO("Not yet implemented")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tempButton.setOnClickListener {
            viewModel.saveTest(
                GeoPointEntity(
                    geoPointId = 1,
                    tripId = 2,
                    latitude = 12.12,
                    longitude = 21.21,
                    stepNum = 0
                )
            )
        }

        binding.getButton.setOnClickListener {
            viewModel.getGeoPoints()
        }
    }

}