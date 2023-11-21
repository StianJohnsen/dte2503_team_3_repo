package com.example.dashcarr.presentation.tabs.settings.social_settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
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
    private var currentMessageId: Int? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentMessageId = arguments?.getInt("messageId", -1).takeIf { it != -1 }

        if (currentMessageId != null) {
            viewModel.getMessageById(currentMessageId!!).observe(viewLifecycleOwner) { friend ->
                binding.addMessageInputField.setText(friend.content)
                binding.btnAddMessage.text = getString(R.string.update)
                binding.btnDeleteMessage.visibility = View.VISIBLE
            }
        } else {
            binding.btnAddMessage.text = getString(R.string.add_message)
            binding.btnDeleteMessage.visibility = View.GONE
        }

        initListeners()
    }

    private fun initListeners() {
        binding.backToSocialSettingsFromAddMessages.setOnClickListener {
            findNavController().navigate(R.id.action_addMessageFragment_to_QuickMessagesFragment)
        }

        binding.btnAddMessage.setOnClickListener {
            val inputContent = binding.addMessageInputField.text.toString().trim()

            if (inputContent.isEmpty()) {
                Toast.makeText(context, "Content is a mandatory field", Toast.LENGTH_SHORT).show()
            } else {
                if (currentMessageId == null) {
                    viewModel.addToDatabase(requireContext(), inputContent)
                } else {
                    viewModel.updateMessage(requireContext(), currentMessageId!!, inputContent)
                }
                findNavController().popBackStack()
            }
        }

        binding.btnDeleteMessage.setOnClickListener {
            findNavController().navigate(R.id.action_addMessageFragment_to_QuickMessagesFragment)
            currentMessageId?.let { id ->
                viewModel.deleteMessage(requireContext(), id)
            }
        }
    }
}