package com.example.dashcarr.presentation.tabs.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentProductFrontPageBinding
import com.example.dashcarr.presentation.core.BaseFragment


class ProductFrontPage : BaseFragment<FragmentProductFrontPageBinding>(
    FragmentProductFrontPageBinding::inflate,
    showBottomNavBar = false
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ProductFrontPageAdapter()
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            productInfoRecycler.adapter = adapter
            exitProductFrontPage.setOnClickListener {
                findNavController().navigate(R.id.action_productFrontPage_to_action_map)
            }
        }

        val productInfoList = listOf<ProductInfo>(
            ProductInfo(1,"MapView updates when your location changes\n\n"),
            ProductInfo(2,"OpenStreetMap is implemented as the map technology\n\n"),
            ProductInfo(3,"We use sensordata to estimate your current driving status\n\n"),
            ProductInfo(4,"Start a driving session where you save all sensor data about your drive and later analysis\n\n"),
            ProductInfo(5,"During the driving session you can also see your current: distance, elevation, elapsed time and speed\n\n"),
            ProductInfo(6,"We have also introduced an experimental feature where you use your phone's display as a heads-up-display\n\n")
        )

            adapter.submitList(productInfoList)


    }

    override fun observeViewModel() {
        TODO("Not yet implemented")
    }

    override fun initListeners() {
        TODO("Not yet implemented")
    }
}