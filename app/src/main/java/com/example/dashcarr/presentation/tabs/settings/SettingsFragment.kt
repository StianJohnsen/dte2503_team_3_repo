package com.example.dashcarr.presentation.tabs.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.BuildConfig
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentSettingsBinding
import com.example.dashcarr.extensions.collectWithLifecycle
import com.example.dashcarr.presentation.core.BaseFragment
import com.example.dashcarr.presentation.tabs.map.MapViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(
    FragmentSettingsBinding::inflate,
    showBottomNavBar = true
) {
    private val viewModel: SettingsViewModel by viewModels()
    private val mapViewModel: MapViewModel by viewModels()

    lateinit var appExecutors: AppExecutors

    fun observeViewModel() {
        viewModel.logOutState.collectWithLifecycle(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_global_loginFragment)
        }
    }


    private fun initListeners() {
        binding.btnAboutApp.setOnClickListener {
            showBottomNavigation(false)
            findNavController().navigate(R.id.action_action_settings_to_animationSampleFragment)
        }

        binding.btnMapsSettings.setOnClickListener {
            showBottomNavigation(false)
            findNavController().navigate(R.id.action_action_settings_to_mapsSettingsFragment)
        }

        binding.btnNewFeatures.setOnClickListener {
            findNavController().navigate(R.id.action_action_settings_to_productFrontPage)
        }

        binding.btnSocialSettings.setOnClickListener {
            findNavController().navigate(R.id.action_action_settings_to_socialSettingsFragment)
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logOut()
            mapViewModel.updateAppPreferences(false)
        }

        binding.btnPowerSettings.setOnClickListener {
            findNavController().navigate(R.id.action_action_settings_to_powerSettings)
        }

        binding.btnSocialSettings.setOnClickListener {
            findNavController().navigate(R.id.action_action_settings_to_socialSettingsFragment)
        }

        binding.btnHistorySettings.setOnClickListener {
            findNavController().navigate(R.id.action_action_settings_to_selectContactFragment)
        }

        binding.sendEmailButton.setOnClickListener {
            sendEmail()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onAttach(context: Context) {
        appExecutors = AppExecutors()
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        observeViewModel()

    }


    fun sendEmail() {
        // Set up mail server properties

        val stringSenderEmail = "dashcarr.business@gmail.com"
        val stringRecieverMail = "stianjohnzz1@gmail.com" // INSERT ANY EMAIL ADDRESS HERE
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
                mm.subject = "Email from dashcarr"
                mm.setText("Hello there from us in the DashCarr business \n\n Hope you're doing fine today!")

                Transport.send(mm)
            } catch (e: Exception) {
                println("exception: ${e.message.toString()}")
            }
        }
        Toast.makeText(context, "The email has been sent!", Toast.LENGTH_LONG).show()
    }

}