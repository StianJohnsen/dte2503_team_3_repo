package com.example.dashcarr.presentation.tabs.camera.dashcam

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.ActivityInfo
import android.os.Build
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
import androidx.lifecycle.lifecycleScope
import com.example.dashcarr.databinding.FragmentDashcamBinding
import com.example.dashcarr.presentation.core.BaseFragment
import com.example.dashcarr.presentation.tabs.settings.PowerSavingMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
@AndroidEntryPoint
class DashcamFragment : BaseFragment<FragmentDashcamBinding>(
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
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

        if (PowerSavingMode.getPowerMode()) {
            Toast.makeText(context, "Recording increases the power consumption!", Toast.LENGTH_SHORT).show()
        }
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted ->
            if (isGranted[Manifest.permission.CAMERA]!! && (isGranted[Manifest.permission.WRITE_EXTERNAL_STORAGE]!! || Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)) {
                viewModel.userPreferencesLiveData.observe(viewLifecycleOwner) {
                    viewModel.activate(requireActivity(), this, it.cameraDuration) {
                        val animation = ObjectAnimator.ofFloat(binding.dashcamPreview, "translationX", 300F, 0F).apply {
                            duration = 1000L
                            interpolator = DecelerateInterpolator()
                        }
                        animation.doOnStart {
                            binding.dashcamPreview.visibility = View.VISIBLE
                        }
                        animation.start()
                    }
                }
            } else {
                destroy()
                parentFragmentManager.beginTransaction().remove(this).commit()
            }
        }.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA))

        return view
    }

    fun deactivate(onFinished: () -> Unit = {}) {
        destroy()
        val turnOffRecordingJob = lifecycleScope.launch {
            viewModel.deactivate()
            Toast.makeText(activity, "Video saved", Toast.LENGTH_SHORT).show()
        }
        val animation = ObjectAnimator.ofFloat(binding.dashcamPreview, "translationX", 0F, 300F).apply {
            duration = 1000L
            interpolator = AccelerateInterpolator()
        }
        animation.doOnEnd {
            lifecycleScope.launch {
                turnOffRecordingJob.join()
                onFinished()
                parentFragmentManager.beginTransaction().remove(this@DashcamFragment)
                    .commitAllowingStateLoss()
            }
        }
        animation.start()
    }

    override fun onDestroyView() {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        viewModel.closeCamera()
        super.onDestroyView()
    }

}