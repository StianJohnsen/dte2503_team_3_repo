package com.example.dashcarr.presentation.tabs.camera.dashcam

import android.animation.ObjectAnimator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.viewModels
import com.example.dashcarr.databinding.FragmentDashcamBinding
import com.example.dashcarr.presentation.core.BaseFragment

class DashcamFragment : BaseFragment<FragmentDashcamBinding>(
    FragmentDashcamBinding::inflate
) {

    companion object {

        @Volatile
        private var instance: DashcamFragment? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: DashcamFragment().also { instance = it }
            }
    }

    private val viewModel: DashcamViewModel by viewModels()

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        doSomething()
    }

    public fun doSomething() {
        viewModel.initViewModel(requireActivity(), this, {
            // recording started
            ObjectAnimator.ofFloat(binding.dashcamPreview, "translationX", 300F, 0F).apply {
                duration = 1000L
                interpolator = DecelerateInterpolator()
            }.start()
        }, {
            // recording stopped
            ObjectAnimator.ofFloat(binding.dashcamPreview, "translationX", 0F, 300F).apply {
                duration = 1000L
                interpolator = AccelerateInterpolator()
            }.start()
        })

    }


}