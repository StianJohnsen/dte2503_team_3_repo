package com.example.dashcarr.presentation.tabs.camera.dashcam

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.camera.view.PreviewView
import androidx.fragment.app.viewModels
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentDashcamBinding
import com.example.dashcarr.presentation.core.BaseFragment

class DashcamFragment : BaseFragment<FragmentDashcamBinding>(
    FragmentDashcamBinding::inflate
) {
    private val viewModel: DashcamViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<ImageButton>(R.id.start_dashcam_recording).setOnClickListener {
            requireActivity().findViewById<PreviewView>(R.id.dashcam_preview).visibility = View.GONE
        }
        viewModel.initViewModel(requireActivity(), this)

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    }

}