package com.example.dashcarr.presentation.tabs.settings.social_settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentAddFriendBinding
import com.example.dashcarr.domain.entity.FriendsEntity
import com.example.dashcarr.presentation.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddFriendFragment : BaseFragment<FragmentAddFriendBinding>(
    FragmentAddFriendBinding::inflate,
    showBottomNavBar = false
) {
    private val viewModel: AddFriendViewModel by viewModels()

    private var currentFriendId: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentFriendId = arguments?.getInt("friendId", -1).takeIf { it != -1 }

        if (currentFriendId != null) {
            viewModel.getFriendById(currentFriendId!!).observe(viewLifecycleOwner) { friend: FriendsEntity ->
                binding.inputName.setText(friend.name)
                binding.inputEmail.setText(friend.email)
                binding.inputPhone.setText(friend.phone)
                binding.btnAddFriend.text = getString(R.string.update)
                binding.btnDeleteFriend.visibility = View.VISIBLE
            }
        } else {
            binding.btnAddFriend.text = getString(R.string.add_friend_button)
            binding.btnDeleteFriend.visibility = View.GONE
        }

        initListeners()
    }

    private fun initListeners() {
        binding.backToSocialSettings.setOnClickListener {
            findNavController().navigate(R.id.action_addFriendFragment_to_SocialSettingsFragment)
        }

        binding.btnAddFriend.setOnClickListener {
            val inputName = binding.inputName.text.toString().trim()
            val email = binding.inputEmail.text.toString().trim()
            val phone = binding.inputPhone.text.toString().trim()
            val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
            val emailToValidate = binding.inputEmail.text.toString()

            if (inputName.isEmpty()) {
                Toast.makeText(context, "Name is a mandatory field", Toast.LENGTH_SHORT).show()
            } else if (email.isEmpty() && phone.isEmpty()) {
                Toast.makeText(context, "Phone or Email are mandatory fields", Toast.LENGTH_SHORT).show()
            } else if (!emailToValidate.matches(emailRegex.toRegex()) && emailToValidate != "") {
                Toast.makeText(context, "Check your email please", Toast.LENGTH_SHORT).show()
            } else {
                if (currentFriendId == null) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            viewModel.saveNewFriend(
                                friend = FriendsEntity(
                                    name = inputName,
                                    phone = phone,
                                    email = email,
                                    createdTimeStamp = System.currentTimeMillis()
                                )
                            )
                        } catch (e: Exception) {
                        }
                    }
                } else {
                    viewModel.updateFriend(requireContext(), currentFriendId!!, inputName, email, phone)
                }
                findNavController().popBackStack()
            }
        }

        binding.btnDeleteFriend.setOnClickListener {
            findNavController().navigate(R.id.action_addFriendFragment_to_SocialSettingsFragment)
            currentFriendId?.let { id ->
                viewModel.deleteFriend(requireContext(), id)
            }
        }
    }
}