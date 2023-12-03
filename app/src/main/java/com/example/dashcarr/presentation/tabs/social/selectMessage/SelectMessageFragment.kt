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
import com.example.dashcarr.data.database.AppDatabase
import com.example.dashcarr.databinding.FragmentSelectMessageBinding
import com.example.dashcarr.domain.entity.FriendsEntity
import com.example.dashcarr.domain.entity.SentMessagesEntity
import com.example.dashcarr.presentation.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executors
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

private const val MY_PERMISSION_REQUEST_SEND_SMS = 0


@AndroidEntryPoint
class SelectMessageFragment : BaseFragment<FragmentSelectMessageBinding>(
    FragmentSelectMessageBinding::inflate,
    showBottomNavBar = false
) {


    private val viewModel: SelectMessageViewModel by viewModels {
        SelectMessageViewModelFactory(AppDatabase.getInstance(requireContext()).MessagesDao())
    }
    private var messageList = mutableListOf<SelectMessage>()


    private lateinit var currentContact: FriendsEntity

    private val userViewModel: SelectMessageUserViewModel by viewModels()
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

    private fun setAdapter(
        friendId: Int,
        phoneNumber: String,
        emailAddress: String,
        messageList: MutableList<SelectMessage>
    ) {
        val action = SelectMessageFragmentDirections.actionSelectMessageFragmentToActionMap(true)
        val adapter = SelectMessageAdapter(
            phoneNumber = phoneNumber,
            emailAddress = emailAddress,
            onMessageButtonClicked = {
                try {
                    sendMessage(phoneNumber, it.content)
                    insertIntoMessageHistory(it.id, friendId, System.currentTimeMillis(), true)
                    Toast.makeText(requireContext(), "Sent message to: $phoneNumber", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(action)
                } catch (e: Exception) {
                    Log.e(this::class.simpleName, "Message exception: ${e.stackTraceToString()}")
                    Toast.makeText(requireContext(), "SMS could not be sent", Toast.LENGTH_LONG).show()
                }
            },
            onEmailButtonClicked = {
                try {
                    sendEmail(emailAddress, it.content)
                    insertIntoMessageHistory(it.id, friendId, System.currentTimeMillis(), false)
                    Toast.makeText(requireContext(), "Sent email to: $emailAddress", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e(this::class.simpleName, "Email exception: ${e.stackTraceToString()}")
                    Toast.makeText(requireContext(), "Email could not be sent", Toast.LENGTH_LONG).show()

                }
                findNavController().navigate(action)


            }
        )
        adapter.submitList(messageList)
        binding.selectMessageRecycler.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.messagesList.observe(viewLifecycleOwner) {
            it.forEach { messagesEntity ->
                messageList.add(SelectMessage(messagesEntity.id, messagesEntity.content, messagesEntity.isPhone))
            }
        }

        viewModel.currentContact.observe(viewLifecycleOwner) {
            setAdapter(it.id, it.phone, it.email, messageList)

            if (it != null) {
                currentContact = it
            }
        }
    }


    private fun sendMessage(destinationNumber: String, message: String) {
        requestSmsPermission()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            sendTextMessageAndroid12AndAbove(destinationNumber, message)
        } else {
            sendTextMessageBelowAndroid12(destinationNumber, message)
        }


    }

    private fun sendEmail(destinationEmail: String, message: String) {
        val stringSenderEmail = "dashcarr.business@gmail.com"
        val stringPasswordSenderEmail = getString(R.string.email_smtp_app_app_password)


        Executors.newSingleThreadExecutor().execute {
            val props = System.getProperties()
            props["mail.smtp.host"] = "smtp.gmail.com"
            props["mail.smtp.socketFactory.port"] = "465"
            props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
            props["mail.smtp.auth"] = "true"
            props["mail.smtp.port"] = "465"
            val session = Session.getInstance(props,
                object : javax.mail.Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(stringSenderEmail, stringPasswordSenderEmail)
                    }
                }
            )
            val mm = MimeMessage(session)
            mm.setFrom(InternetAddress(stringSenderEmail))
            mm.addRecipient(
                Message.RecipientType.TO,
                InternetAddress(destinationEmail)
            )
            mm.subject = "DashCarr Message from: ${userViewModel.getUser()?.email}"
            mm.setText("You have gotten a message from ${userViewModel.getUser()?.email}: \n\n$message\n\nSent from DashCarr")

            Transport.send(mm)

        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun sendTextMessageAndroid12AndAbove(destinationNumber: String, message: String) {
        smsManagerObject.sendTextMessage(destinationNumber, null, message, null, null, 0)
    }

    @Suppress("DEPRECATION")
    private fun sendTextMessageBelowAndroid12(destinationNumber: String, message: String) {
        SmsManager.getDefault().sendTextMessage(destinationNumber, null, message, null, null)
    }

    private fun insertIntoMessageHistory(messageId: Long, friendId: Int, createdTimeStamp: Long, isSms: Boolean) {
        val sentMessageEntity =
            SentMessagesEntity(messageId = messageId, friendId = friendId, createdTimeStamp = createdTimeStamp, isSms = isSms)
        viewModel.insertIntoSentMessages(requireContext(), sentMessageEntity)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getAllMessages(requireContext())
        viewModel.getContact(requireArguments().getInt("contactId"), requireContext())
        observeViewModel()
        requestPermission.launch(Manifest.permission.SEND_SMS)
        binding.apply {
            backToSettingsFromSelectMessage.setOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}