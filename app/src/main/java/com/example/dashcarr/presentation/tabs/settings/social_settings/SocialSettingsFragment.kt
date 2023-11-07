package com.example.dashcarr.presentation.tabs.settings.social_settings

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.data.database.AppDatabase
import com.example.dashcarr.databinding.FragmentSocialSettingsBinding
import com.example.dashcarr.presentation.core.BaseFragment
import kotlinx.coroutines.launch

class SocialSettingsFragment : BaseFragment<FragmentSocialSettingsBinding>(
    FragmentSocialSettingsBinding::inflate,
    showBottomNavBar = false
) {

    companion object {
        fun newInstance() = SocialSettingsFragment()
    }

    private val viewModel: SocialSettingsViewModel by viewModels {
        SocialSettingsViewModelFactory(AppDatabase.getInstance(requireContext()).FriendsDao())
    }

    private fun initListeners() {

        binding.btnAddFriend.setOnClickListener {
            val action = SocialSettingsFragmentDirections.actionSocialSettingsFragmentToAddFriendFragment(friendId = -1)
            findNavController().navigate(action)
        }

        binding.backToSettings.setOnClickListener {
            findNavController().navigate(R.id.action_socialSettingsFragment_to_action_settings)
        }

        val dao = AppDatabase.getInstance(requireContext()).FriendsDao()

        lifecycleScope.launch {
            val friends = dao.getAllFriends()
            friends.forEach { friend ->
                val btn = Button(context).apply {
                    text = friend.name
                    id = friend.id

                    val hasPhone = viewModel.hasPhoneNumber(friend)
                    val hasEmail = viewModel.hasEmail(friend)

                    val drawableRight = when {
                        hasPhone && hasEmail -> ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.baseline__phone_email_24
                        )

                        hasPhone -> ContextCompat.getDrawable(requireContext(), R.drawable.baseline_phone_24)
                        hasEmail -> ContextCompat.getDrawable(requireContext(), R.drawable.baseline_email_24)
                        else -> null
                    }

                    setBackgroundResource(R.color.white)

                    val paddingEnd = resources.getDimensionPixelSize(R.dimen.margin_padding_size_10)
                    val paddingStart = 0
                    compoundDrawablePadding = -paddingEnd

                    setCompoundDrawablesWithIntrinsicBounds(null, null, drawableRight, null)
                    drawableRight?.setBounds(0, 0, drawableRight.intrinsicWidth, drawableRight.intrinsicHeight)
                    setPadding(paddingStart, 0, paddingEnd, 0)
                }

                btn.setOnClickListener {
                    val action =
                        SocialSettingsFragmentDirections.actionSocialSettingsFragmentToAddFriendFragment(friendId = friend.id)
                    findNavController().navigate(action)
                }

                binding.linearFriends.addView(btn)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()

    }
}