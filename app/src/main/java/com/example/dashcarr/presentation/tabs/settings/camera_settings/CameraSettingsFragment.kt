package com.example.dashcarr.presentation.tabs.settings.camera_settings

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.NumberPicker
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.databinding.FragmentCameraSettingsBinding
import com.example.dashcarr.presentation.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Method

@AndroidEntryPoint
class CameraSettingsFragment : BaseFragment<FragmentCameraSettingsBinding>(
    FragmentCameraSettingsBinding::inflate,
    showBottomNavBar = false
) {
    private val viewModel: CameraSettingsViewModel by viewModels()

    @SuppressLint("DiscouragedPrivateApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBackToSettings.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnOpenFiles.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(
                Uri.parse(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString() + "/Dashcarr"
                ), "resource/folder"
            )
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(requireActivity(), "Could not find ES File Explorer", Toast.LENGTH_SHORT).show()
            }
        }
        binding.durationPicker.apply {
            setFormatter(NumberPicker.Formatter { return@Formatter "$it min" })
            minValue = 1
            maxValue = 30
            wrapSelectorWheel = true
            viewModel.userPreferencesLiveData.observe(this@CameraSettingsFragment.viewLifecycleOwner) {
                value = it.cameraDuration
            }
            setOnValueChangedListener { picker, oldVal, newVal ->
                viewModel.updateDuration(newVal)
            }
        }
        try {
            val method: Method =
                NumberPicker::class.java.getDeclaredMethod("changeValueByOne", Boolean::class.javaPrimitiveType)
            method.isAccessible = true
            method.invoke(binding.durationPicker, true)
        } catch (e: Exception) {
            Log.e(this::class.simpleName, e.stackTraceToString())
        }
    }

}