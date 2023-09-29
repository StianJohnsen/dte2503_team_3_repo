package com.example.dashcarr.presentation.tabs.history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.dashcarr.data.entity.GeoPointEntity
import com.example.dashcarr.databinding.FragmentHistoryBinding
import com.example.dashcarr.presentation.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random

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
                    geoPointId = Random.nextInt(100),
                    tripId = Random.nextInt(100),
                    latitude = Random.nextDouble(),
                    longitude = Random.nextDouble(),
                    stepNum = Random.nextInt(100)
                )
            )
        }

        binding.getButton.setOnClickListener {
            viewModel.getGeoPoints()
        }
    }

}