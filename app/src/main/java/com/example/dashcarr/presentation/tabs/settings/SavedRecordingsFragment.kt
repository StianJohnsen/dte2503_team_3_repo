package com.example.dashcarr.presentation.tabs.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.dashcarr.presentation.core.BaseFragment
import com.example.dashcarr.databinding.FragmentSavedRecordingsBinding


class SavedRecordingsFragment : BaseFragment<FragmentSavedRecordingsBinding>(
    FragmentSavedRecordingsBinding::inflate
) {
    private val viewModel: SavedRecordingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}