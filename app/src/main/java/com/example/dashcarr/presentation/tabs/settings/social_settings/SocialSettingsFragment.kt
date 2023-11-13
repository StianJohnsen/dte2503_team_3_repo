package com.example.dashcarr.presentation.tabs.settings.social_settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dashcarr.R
import com.example.dashcarr.data.database.AppDatabase
import com.example.dashcarr.databinding.FragmentSocialSettingsBinding
import com.example.dashcarr.domain.entity.SentMessagesEntity
import com.example.dashcarr.extensions.collectWithLifecycle
import com.example.dashcarr.extensions.hideKeyboard
import com.example.dashcarr.extensions.toastErrorUnknownShort
import com.example.dashcarr.extensions.toastShort
import com.example.dashcarr.presentation.core.BaseFragment
import com.example.dashcarr.presentation.tabs.settings.social_settings.adapter.SentMessagesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SocialSettingsFragment : BaseFragment<FragmentSocialSettingsBinding>(
    FragmentSocialSettingsBinding::inflate,
    showBottomNavBar = false), SentMessagesAdapter.IOnSentMessageClickListener
{
    private val viewModel: SocialSettingsViewModel by viewModels()

    private val messagesAdapter by lazy {
        SentMessagesAdapter(this)
    }

    private val MY_PERMISSION_REQUEST_SEND_SMS = 0

    private val smsManagerObject: SmsManager by lazy {
        requireContext().getSystemService(SmsManager::class.java) as SmsManager
    }

    companion object {
        fun newInstance() = SocialSettingsFragment()
    }

    @RequiresApi(Build.VERSION_CODES.R)
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

    @RequiresApi(Build.VERSION_CODES.R)
    private fun initListeners() {

        binding.btnDelete.setOnClickListener {
            viewModel.deleteMessage()
        }

        binding.btnCancelDelete.setOnClickListener {
            viewModel.hideDeleteDialog()
        }

        binding.btnBackToSettings.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnAddFriend.setOnClickListener {
            val action =
                SocialSettingsFragmentDirections.actionSocialSettingsFragmentToAddFriendFragment(
                    friendId = -1
                )
            findNavController().navigate(action)
        }

        binding.btnBackToSettings.setOnClickListener {
            findNavController().navigate(R.id.action_socialSettingsFragment_to_action_settings)
        }

        binding.sendMessageButton.setOnClickListener {
            sendMessage()
        }

        binding.addMessageButton.setOnClickListener {
            findNavController().navigate(R.id.action_socialSettingsFragment_to_addMessagesFragment)
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

                        hasPhone -> ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.baseline_phone_24
                        )

                        hasEmail -> ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.baseline_email_24
                        )

                        else -> null
                    }

                    setBackgroundResource(R.color.white)

                    setCompoundDrawablesWithIntrinsicBounds(null, null, drawableRight, null)
                    compoundDrawablePadding = 10
                }

                btn.setOnClickListener {
                    val action =
                        SocialSettingsFragmentDirections.actionSocialSettingsFragmentToAddFriendFragment(
                            friendId = friend.id
                        )
                    findNavController().navigate(action)
                }

                binding.linearFriends.addView(btn)
            }
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

    @RequiresApi(Build.VERSION_CODES.R)
    private fun sendMessage() {
        requestSmsPermission()
        smsManagerObject.sendTextMessage("5554", null, "Hello my friend", null, null, 0)
    }

    private fun requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.SEND_SMS),
                MY_PERMISSION_REQUEST_SEND_SMS
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "SMS permission granted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
