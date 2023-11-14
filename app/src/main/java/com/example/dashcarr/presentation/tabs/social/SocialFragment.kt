package com.example.dashcarr.presentation.tabs.social

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentSocialBinding
import com.example.dashcarr.domain.entity.SentMessagesEntity
import com.example.dashcarr.extensions.collectWithLifecycle
import com.example.dashcarr.extensions.hideKeyboard
import com.example.dashcarr.extensions.toastErrorUnknownShort
import com.example.dashcarr.extensions.toastShort
import com.example.dashcarr.presentation.core.BaseFragment
import com.example.dashcarr.presentation.tabs.social.adapter.SentMessagesAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SocialFragment : BaseFragment<FragmentSocialBinding>(
    FragmentSocialBinding::inflate,
    showBottomNavBar = true), SentMessagesAdapter.IOnSentMessageClickListener
{
    private val viewModel: SocialViewModel by viewModels()

    private val messagesAdapter by lazy {
        SentMessagesAdapter(this)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        initListeners()
        observeViewModel()
    }

    private fun setupAdapter() {
        binding.recyclerView.adapter = messagesAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun initListeners() {

        binding.btnDelete.setOnClickListener {
            viewModel.deleteMessage()
        }

        binding.btnCancelDelete.setOnClickListener {
            viewModel.hideDeleteDialog()
        }

    }

    private fun observeViewModel() {
        viewModel.sentMessages.observe(viewLifecycleOwner) {
            messagesAdapter.submitList(it)
        }
        viewModel.showDeleteDialog.collectWithLifecycle(viewLifecycleOwner) {
            if (!it) hideKeyboard()
            binding.flDeleteMessageLayout.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.onDeleteMessageSuccess.collectWithLifecycle(viewLifecycleOwner) {
            toastShort(getString(R.string.message_deleted_successfully))
        }
        viewModel.onDeleteMessageFailure.collectWithLifecycle(viewLifecycleOwner) {
            toastErrorUnknownShort()
        }
    }

    override fun onClick(message: SentMessagesEntity) {
        viewModel.showConfirmDeleteDialog(message)
    }


}