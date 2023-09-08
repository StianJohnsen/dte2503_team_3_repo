package com.example.dashcarr.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentMapBinding
import com.example.dashcarr.viewmodel.ViewModelExample


class MapFragment : Fragment() {

    private lateinit var binding: FragmentMapBinding
    private val viewModel: ViewModelExample by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentBinding = FragmentMapBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            mapFragment = this@MapFragment
            minusIconView.setOnClickListener { decNumView() }
            plusIconView.setOnClickListener { incNumView() }
        }
        viewModel.numCounter.observe(this.viewLifecycleOwner) {
            binding.mapTextView.text = it.toString()
            Log.d("Test", it.toString())

        }
    }

    fun decNumView(){
        viewModel.decCounter()
    }

    fun incNumView(){
        viewModel.incCounter()
    }
}
