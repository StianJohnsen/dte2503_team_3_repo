package com.example.dashcarr.presentation.tabs.social

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.dashcarr.databinding.FragmentSocialBinding
import com.example.dashcarr.presentation.core.BaseFragment


class SocialFragment : BaseFragment<FragmentSocialBinding>(
    FragmentSocialBinding::inflate
) {
    private val viewModel: SocialViewModel by viewModels()

    override fun observeViewModel() {
        TODO("Not yet implemented")
    }

    override fun initListeners() {
        TODO("Not yet implemented")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}