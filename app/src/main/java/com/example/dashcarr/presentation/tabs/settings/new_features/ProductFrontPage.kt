package com.example.dashcarr.presentation.tabs.settings.new_features

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentProductFrontPageBinding
import com.example.dashcarr.presentation.MainActivity
import com.example.dashcarr.presentation.core.BaseFragment

/**
 * A fragment for displaying the product front page.
 */
class ProductFrontPage : BaseFragment<FragmentProductFrontPageBinding>(
    FragmentProductFrontPageBinding::inflate,
    showBottomNavBar = false
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            productInfoText.setText(R.string.product_info_text)
            productInfoText.movementMethod = ScrollingMovementMethod()
            exitProductFrontPage.setOnClickListener {
                findNavController().navigate(R.id.action_productFrontPage_to_action_map)
                (requireActivity() as MainActivity).showRedButton(startDelay = 500)
            }
        }
        (requireActivity() as MainActivity).hideRedButton()
    }
}