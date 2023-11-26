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
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.databinding.FragmentCameraSettingsBinding
import com.example.dashcarr.presentation.core.BaseFragment
import java.lang.reflect.Method


class CameraSettingsFragment : BaseFragment<FragmentCameraSettingsBinding>(
    FragmentCameraSettingsBinding::inflate,
    showBottomNavBar = false
) {

    @SuppressLint("DiscouragedPrivateApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.back.setOnClickListener {
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
            setOnValueChangedListener { picker, oldVal, newVal ->
                if (picker.isDirty) {
                    return@setOnValueChangedListener
                }
                val text = "Changed from $oldVal to $newVal"
                Toast.makeText(requireActivity(), text, Toast.LENGTH_SHORT).show()
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
        super.onViewCreated(view, savedInstanceState)
    }

}