package com.example.dashcarr.presentation.tabs.map

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.dashcarr.databinding.FragmentMapBinding
import com.example.dashcarr.presentation.core.BaseFragment


class MapFragment : BaseFragment<FragmentMapBinding>(
    FragmentMapBinding::inflate
) {
    private val viewModel: MapViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}