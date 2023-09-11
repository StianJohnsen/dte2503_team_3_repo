package com.example.dashcarr.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentSocialBinding
import com.example.dashcarr.viewmodel.ViewModelExample


class SocialFragment : Fragment() {

    private lateinit var binding: FragmentSocialBinding
    private val viewModel: ViewModelExample by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentBinding = FragmentSocialBinding.inflate(inflater,container,false)
        binding = fragmentBinding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}