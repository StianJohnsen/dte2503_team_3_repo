package com.example.dashcarr.presentation.tabs.social.selectMessage

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.data.database.AppDatabase
import com.example.dashcarr.databinding.FragmentSelectMessageBinding
import com.example.dashcarr.domain.entity.FriendsEntity
import com.example.dashcarr.domain.entity.SentMessagesEntity
import com.example.dashcarr.presentation.core.BaseFragment

class SelectMessageFragment : BaseFragment<FragmentSelectMessageBinding>(
    FragmentSelectMessageBinding::inflate,
    showBottomNavBar = false
) {

    private val viewModel: SelectMessageViewModel by viewModels {
        SelectMessageViewModelFactory(AppDatabase.getInstance(requireContext()).MessagesDao())
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

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == MY_PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Got SMS permission", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun observeViewModel() {
        val contactId = arguments?.getInt("contactId")
        if (contactId != null) {
            viewModel.getContact(contactId, requireContext())
        }

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
            findNavController().navigate(R.id.action_selectMessageFragment_to_action_settings)

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

    @RequiresApi(Build.VERSION_CODES.R)
    private fun sendMessage(destinationNumer: String, message: String) {
        requestSmsPermission()

        smsManagerObject.sendTextMessage(destinationNumer, null, message, null, null, 0)

    }

    private fun insertIntoMessageHistory(messageId: Long, friendId: Int, createdTimeStamp: Long) {
        val sentMessageEntity =
            SentMessagesEntity(messageId = messageId, friendId = friendId, createdTimeStamp = createdTimeStamp)
        viewModel.insertIntoSentMessages(requireContext(), sentMessageEntity)
    }


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getAllMessages(requireContext())
        observeViewModel()
        binding.apply {
            backToSettingsFromSelectMessage.setOnClickListener {
                requireActivity().onBackPressed()
            }
        }
    }
}