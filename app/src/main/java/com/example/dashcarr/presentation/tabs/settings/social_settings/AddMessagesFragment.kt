package com.example.dashcarr.presentation.tabs.settings.social_settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentAddMessagesBinding
import com.example.dashcarr.presentation.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment for adding or updating messages within the social settings of the application. This fragment
 * provides the user interface to input, modify, or delete a message.
 *
 * This fragment utilizes the [AddMessagesViewModel] for handling data operations related to messages.
 */
@AndroidEntryPoint
class AddMessagesFragment : BaseFragment<FragmentAddMessagesBinding>(
    FragmentAddMessagesBinding::inflate,
    showBottomNavBar = false
) {

    private val viewModel: AddMessagesViewModel by viewModels()
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
                Toast.makeText(context, context?.getString(R.string.mandatory_content), Toast.LENGTH_SHORT).show()
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