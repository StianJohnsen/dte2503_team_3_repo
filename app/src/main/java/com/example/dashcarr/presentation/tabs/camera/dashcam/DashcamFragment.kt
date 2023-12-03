package com.example.dashcarr.presentation.tabs.camera.dashcam

import android.Manifest
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.fragment.app.viewModels
import com.example.dashcarr.databinding.FragmentDashcamBinding
import com.example.dashcarr.presentation.core.BaseFragment
import com.example.dashcarr.presentation.tabs.settings.PowerSavingMode

/**
 * Fragment for handling dashcam functionalities in the app.
 * This fragment is responsible for managing camera access for recording,
 * displaying the camera preview, and saving recordings.
 *
 * It utilizes a ViewModel to handle camera operations and recording logic.
 * The fragment also manages animations for the dashcam preview and handles
 * necessary permissions for camera access.
 *
 * @property viewModel ViewModel associated with dashcam functionalities, handling camera operations.
 */
class DashcamFragment() : BaseFragment<FragmentDashcamBinding>(
    FragmentDashcamBinding::inflate,
    showBottomNavBar = false
) {

    companion object {

        @Volatile
        private var instance: DashcamFragment? = null

        fun getInstance(): DashcamFragment {
            return instance ?: synchronized(this) {
                instance = DashcamFragment()
                return@synchronized instance!!
            }
        }

        fun exists() = instance !== null

        fun destroy() {
            instance = null
        }
    }

    private val viewModel: DashcamViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        if (PowerSavingMode.getPowerMode()) {
            Toast.makeText(context, "Recording increases the power consumption!", Toast.LENGTH_SHORT).show()
        }
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                viewModel.startRecording(requireActivity(), this) {
                    val animation = ObjectAnimator.ofFloat(binding.dashcamPreview, "translationX", 300F, 0F).apply {
                        duration = 1000L
                        interpolator = DecelerateInterpolator()
                    }
                    animation.doOnStart {
                        binding.dashcamPreview.visibility = View.VISIBLE
                    }
                    animation.start()
                }
            } else {
                destroy()
                parentFragmentManager.beginTransaction().remove(this).commit()
            }
        }.launch(Manifest.permission.CAMERA)

        return view
    }

    fun saveRecording() {
        viewModel.saveRecording {
            val animation = ObjectAnimator.ofFloat(binding.dashcamPreview, "translationX", 0F, 300F).apply {
                duration = 1000L
                interpolator = AccelerateInterpolator()
            }
            animation.doOnEnd {
                destroy()
                parentFragmentManager.beginTransaction().remove(this).commit()
            }
            animation.start()
        }
    }

    override fun onDestroyView() {
        viewModel.closeCamera()
        super.onDestroyView()
    }

}