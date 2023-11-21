package com.example.dashcarr.presentation.tabs.social.selectMessage

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentSelectMessageBinding
import com.example.dashcarr.domain.entity.FriendsEntity
import com.example.dashcarr.domain.entity.SentMessagesEntity
import com.example.dashcarr.presentation.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectMessageFragment : BaseFragment<FragmentSelectMessageBinding>(
    FragmentSelectMessageBinding::inflate,
    showBottomNavBar = false
) {

    private val viewModel: SelectMessageViewModel by viewModels()

    private val requestPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Log.d(this::class.simpleName, "Sms permission granted")

            } else {
                Log.d(this::class.simpleName, "Sms permission not granted")

            }
        }

    private val MY_PERMISSION_REQUEST_SEND_SMS = 0


    private val smsManagerObject: SmsManager by lazy {
        requireContext().getSystemService(SmsManager::class.java) as SmsManager
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

    private fun observeViewModel() {
        val contactId = arguments?.getInt("contactId")
        viewModel.getContact(contactId)

        var currentFriendsEntity: FriendsEntity? = null
        viewModel.contactsList.observe(viewLifecycleOwner) {
            currentFriendsEntity = it
        }
        val adapter = SelectMessageAdapter {
            Log.d(this::class.simpleName, it.content)
            currentFriendsEntity?.phone?.let { it1 -> sendMessage(it1, it.content) }
            if (contactId != null) {
                insertIntoMessageHistory(it.id, contactId, System.currentTimeMillis())
            }
            findNavController().navigate(R.id.action_selectMessageFragment_to_action_map)
            Toast.makeText(requireContext(), "Message sent", Toast.LENGTH_SHORT).show()
        }

        binding.selectMessageRecycler.adapter = adapter

        val selectMessageList = mutableListOf<SelectMessage>()

        viewModel.messagesList.observe(viewLifecycleOwner) {
            it.forEach {
                Log.d(this::class.simpleName, it.content)

                selectMessageList.add(SelectMessage(it.id, it.content, it.isPhone))
            }
            adapter.submitList(selectMessageList)
        }
    }

    private fun sendMessage(destinationNumer: String, message: String) {
        requestSmsPermission()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            sendTextMessageAndroid12AndAbove(destinationNumer, message)
        } else {
            sendTextMessageBelowAndroid12(destinationNumer, message)
        }

    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun sendTextMessageAndroid12AndAbove(destinationNumer: String, message: String) {
        smsManagerObject.sendTextMessage(destinationNumer, null, message, null, null, 0)
    }

    private fun sendTextMessageBelowAndroid12(destinationNumer: String, message: String) {
        SmsManager.getDefault().sendTextMessage(destinationNumer, null, message, null, null)
    }

    private fun insertIntoMessageHistory(messageId: Long, friendId: Int, createdTimeStamp: Long) {
        val sentMessageEntity =
            SentMessagesEntity(messageId = messageId, friendId = friendId, createdTimeStamp = createdTimeStamp)
        viewModel.insertIntoSentMessages(sentMessageEntity)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getAllMessages(requireContext())
        requestPermission.launch(Manifest.permission.SEND_SMS)
        observeViewModel()
        binding.apply {
            backToSettingsFromSelectMessage.setOnClickListener {
                requireActivity().onBackPressed()
            }
        }
    }
}