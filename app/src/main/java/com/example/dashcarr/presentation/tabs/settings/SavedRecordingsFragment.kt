package com.example.dashcarr.presentation.tabs.settings

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentSavedRecordingsBinding
import com.example.dashcarr.presentation.core.BaseFragment
import java.io.IOException

class SavedRecordingsFragment : BaseFragment<FragmentSavedRecordingsBinding>(
    FragmentSavedRecordingsBinding::inflate
) {
    private val viewModel: SavedRecordingsViewModel by viewModels()
    override fun observeViewModel() {
        TODO("Not yet implemented")
    }

    override fun initListeners() {
        TODO("Not yet implemented")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            savedRecordingsFragment = this@SavedRecordingsFragment
        }
        binding.buttonShowStats.setOnClickListener {
            findNavController().navigate(R.id.action_action_savedrecordings_to_StatisticsFragment)
        }
        binding.imageBackSaved.setOnClickListener {
            findNavController().navigate(R.id.action_action_savedrecordings_to_SettingsFragment)
        }

        try {
            val assetManager = requireContext().assets
            val fileList = assetManager.list("")?.filter { it.endsWith(".csv") }

            if (fileList != null && fileList.isNotEmpty()) {

                val linearLayout: LinearLayout = view.findViewById(R.id.linear_recordings_buttons)

                // Create button by file
                for (fileName in fileList) {
                    val button = Button(context)
                    button.text =
                        fileName.removeSuffix(".csv")
                    button.setOnClickListener {
                        val action =
                            SavedRecordingsFragmentDirections.actionActionSavedrecordingsToRecordingDetailsFragment(
                                fileName.removeSuffix(".csv")
                            )
                        findNavController().navigate(action)
                    }
                    linearLayout.addView(button)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}