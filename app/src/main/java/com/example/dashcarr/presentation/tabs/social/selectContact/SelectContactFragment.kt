package com.example.dashcarr.presentation.tabs.social.selectContact

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.databinding.FragmentSelectContactBinding
import com.example.dashcarr.presentation.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

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
            selectContactList.clear()
            it.forEach {
                selectContactList.add(SelectContact(it.id, it.name))
            }
            adapter.submitList(selectContactList)

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getAllFriends(requireContext())
        observeViewModel()
        binding.apply {
            backToSettingsFromSelectContact.setOnClickListener {
                requireActivity().onBackPressed()
            }
        }


    }

}