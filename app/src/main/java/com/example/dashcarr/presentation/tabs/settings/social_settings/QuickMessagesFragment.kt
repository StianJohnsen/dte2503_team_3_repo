package com.example.dashcarr.presentation.tabs.settings.social_settings

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.data.database.AppDatabase
import com.example.dashcarr.databinding.FragmentQuickMessagesBinding
import com.example.dashcarr.presentation.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment for managing quick messages in the app's social settings.
 * It displays a list of predefined messages, allowing users to add, view,
 * and edit them through interaction with AppDatabase.
 */
@AndroidEntryPoint
class QuickMessagesFragment : BaseFragment<FragmentQuickMessagesBinding>(
    FragmentQuickMessagesBinding::inflate,
    showBottomNavBar = false
) {

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun initListeners() {

        binding.btnBackToSettings.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnBackToSettings.setOnClickListener {
            findNavController().navigate(R.id.action_quickMessagesFragment_to_action_settings)
        }

        binding.addMessageButton.setOnClickListener {
            findNavController().navigate(R.id.action_socialSettingsFragment_to_addMessagesFragment)
        }

        val dao = AppDatabase.getInstance(requireContext()).MessagesDao()

        lifecycleScope.launch {
            val messages = dao.getAllMessages()
            messages.forEach { message ->
                val btn = Button(context).apply {
                    text = message.content
                    id = message.id.toInt()

                    setBackgroundResource(R.color.secondary_color)

                    compoundDrawablePadding = 10
                }

                btn.setOnClickListener {
                    val action =
                        QuickMessagesFragmentDirections.actionQuickMessageFragmentToAddMessageFragment(
                            messageId = message.id.toInt()
                        )
                    findNavController().navigate(action)
                }

                binding.linearFriends.addView(btn)
            }
        }
    }
}
