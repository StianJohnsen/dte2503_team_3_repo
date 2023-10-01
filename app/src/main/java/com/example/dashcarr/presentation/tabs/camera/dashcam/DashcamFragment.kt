package com.example.dashcarr.presentation.tabs.camera.dashcam

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
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
        update()
    }

    fun update() {
        viewModel.updateCamera(requireActivity(), this, {
            // recording started
            val animation = ObjectAnimator.ofFloat(binding.dashcamPreview, "translationX", 300F, 0F).apply {
                duration = 1000L
                interpolator = DecelerateInterpolator()
            }
            animation.doOnStart {
                binding.dashcamPreview.visibility = View.VISIBLE
            }
            animation.start()
        }, {
            // recording stopped
            val animation = ObjectAnimator.ofFloat(binding.dashcamPreview, "translationX", 0F, 300F).apply {
                duration = 1000L
                interpolator = AccelerateInterpolator()
            }
            animation.doOnEnd {
                binding.dashcamPreview.visibility = View.GONE
            }
            animation.start()
        })
    }

    override fun onDestroyView() {
        viewModel.closeCamera()
        super.onDestroyView()
    }
}