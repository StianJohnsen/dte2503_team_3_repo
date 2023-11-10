package com.example.dashcarr.presentation.tabs.settings.social_settings

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.example.dashcarr.data.database.AppDatabase
import com.example.dashcarr.databinding.FragmentAddMessagesBinding
import com.example.dashcarr.presentation.core.BaseFragment


class AddMessagesFragment : BaseFragment<FragmentAddMessagesBinding>(
    FragmentAddMessagesBinding::inflate,
    showBottomNavBar = false
) {

    private val viewModel: AddMessagesViewModel by viewModels {
        AddMessagesViewModelFactory(AppDatabase.getInstance(requireContext()).MessagesDao())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            addMessageToDbButton.setOnClickListener {
                val message = binding.addMessageInputField.text.toString()
                viewModel.addToDatabase(requireContext(), message)
                requireActivity().onBackPressed()
                Log.d(this::class.simpleName, message)
            }
            backToSocialSettingsFromAddMessages.setOnClickListener {
                requireActivity().onBackPressed()
            }
        }
    }
}