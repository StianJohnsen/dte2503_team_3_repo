package com.example.dashcarr.presentation.tabs.social.selectContact

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.databinding.FragmentSelectContactBinding
import com.example.dashcarr.presentation.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment for selecting contacts from a list.
 * This fragment displays a list of selectable contacts and allows the user to choose a contact.
 */
@AndroidEntryPoint
class SelectContactFragment : BaseFragment<FragmentSelectContactBinding>(
    FragmentSelectContactBinding::inflate,
    showBottomNavBar = false
) {

    private val viewModel: SelectContactViewModel by viewModels()

    private fun observeViewModel() {
        val adapter = SelectContactAdapter {
            Log.d(this::class.simpleName, it.name)
            val action =
                SelectContactFragmentDirections.actionSelectContactFragmentToSelectMessageFragment(contactId = it.id)
            findNavController().navigate(action)
        }

        binding.selectContactRecycler.adapter = adapter

        val selectContactList = mutableListOf<SelectContact>()

        viewModel.friendsList.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                Toast.makeText(requireContext(), "Add friends", Toast.LENGTH_SHORT).show()
            }
            selectContactList.clear()
            it.forEach { friendsEntity ->
                selectContactList.add(SelectContact(friendsEntity.id, friendsEntity.name))
            }
            adapter.submitList(selectContactList)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        binding.apply {
            backToSettingsFromSelectContact.setOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}