package com.example.dashcarr.presentation.tabs.social.selectMessage

import android.Manifest
import android.content.Context
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
import com.example.dashcarr.BuildConfig
import com.example.dashcarr.data.database.AppDatabase
import com.example.dashcarr.databinding.FragmentSelectMessageBinding
import com.example.dashcarr.domain.entity.FriendsEntity
import com.example.dashcarr.domain.entity.SentMessagesEntity
import com.example.dashcarr.presentation.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

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
    lateinit var appExecutors: AppExecutors
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

    fun setAdapter(
        friendId: Int,
        isPhoneNumer: String,
        isEmailAdress: String,
        messageList: MutableList<SelectMessage>
    ) {
        val action = SelectMessageFragmentDirections.actionSelectMessageFragmentToActionMap(true)
        val adapter = SelectMessageAdapter(
            isPhoneNumber = isPhoneNumer,
            isEmailAdress = isEmailAdress,
            onMessageButtonClicked = {
                sendMessage(isPhoneNumer, it.content)
                insertIntoMessageHistory(it.id, friendId, System.currentTimeMillis())
                Toast.makeText(requireContext(), "Sent message to: $isPhoneNumer", Toast.LENGTH_SHORT).show()
                findNavController().navigate(action)
            },
            onEmailButtonClicked = {
                sendEmail(isEmailAdress, it.content)
                insertIntoMessageHistory(it.id, friendId, System.currentTimeMillis())
                Toast.makeText(requireContext(), "Sent email to: $isEmailAdress", Toast.LENGTH_SHORT).show()
                findNavController().navigate(action)

            }
        )
        adapter.submitList(messageList)
        binding.selectMessageRecycler.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.messagesList.observe(viewLifecycleOwner) {
            it.forEach {
                messageList.add(SelectMessage(it.id, it.content, it.isPhone))
            }
        }

        viewModel.currentContact.observe(viewLifecycleOwner) {
            setAdapter(it.id, it.phone, it.email, messageList)

            if (it != null) {
                currentContact = it
            }
        }
    }

    override fun onAttach(context: Context) {
        appExecutors = AppExecutors()
        super.onAttach(context)

    }

    private fun sendMessage(destinationNumer: String, message: String) {
        requestSmsPermission()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            sendTextMessageAndroid12AndAbove(destinationNumer, message)
        } else {
            sendTextMessageBelowAndroid12(destinationNumer, message)
        }


    }

    fun sendEmail(destinationEmail: String, message: String) {
        val stringSenderEmail = "dashcarr.business@gmail.com"
        val stringRecieverMail = destinationEmail
        val stringPasswordSenderEmail = BuildConfig.DASHCARR_SMTP_PASSWORD


        appExecutors.diskIO().execute {
            val props = System.getProperties()
            props.put("mail.smtp.host", "smtp.gmail.com")
            props.put("mail.smtp.socketFactory.port", "465")
            props.put(
                "mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory"
            )
            props.put("mail.smtp.auth", "true")
            props.put("mail.smtp.port", "465")
            val session = Session.getInstance(props,
                object : javax.mail.Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(stringSenderEmail, stringPasswordSenderEmail)
                    }
                }
            )
            try {
                val mm = MimeMessage(session)
                mm.setFrom(InternetAddress(stringSenderEmail))
                mm.addRecipient(
                    Message.RecipientType.TO,
                    InternetAddress(stringRecieverMail)
                )
                mm.subject = "Message from: ${userViewModel.getUser()?.email}"
                mm.setText("Hi!\n\nYou have gotten a message from: ${userViewModel.getUser()?.email}\nMessage:\n\n$message")

                Transport.send(mm)
            } catch (e: Exception) {
                throw e
            }
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
                requireActivity().onBackPressed()
            }
        }
    }
}