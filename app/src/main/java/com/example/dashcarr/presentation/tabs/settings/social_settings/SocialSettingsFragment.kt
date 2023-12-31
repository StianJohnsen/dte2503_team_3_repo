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
import com.example.dashcarr.R
import com.example.dashcarr.data.database.AppDatabase
import com.example.dashcarr.databinding.FragmentSocialSettingsBinding
import com.example.dashcarr.presentation.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment for social settings, allowing users to manage friends and send messages.
 * Manages SMS permissions and leverages an SMS manager for message functionalities.
 * Provides navigation to add friends and view friend details.
 */
@AndroidEntryPoint
class SocialSettingsFragment : BaseFragment<FragmentSocialSettingsBinding>(
    FragmentSocialSettingsBinding::inflate,
    showBottomNavBar = false
) {
    private val viewModel: SocialSettingsViewModel by viewModels()

    private val MY_PERMISSION_REQUEST_SEND_SMS = 0

    private val smsManagerObject: SmsManager by lazy {
        requireContext().getSystemService(SmsManager::class.java) as SmsManager
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBackToSettings.setOnClickListener {
            findNavController().popBackStack()
        }
        initListeners()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun initListeners() {

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

                    setBackgroundResource(R.color.secondary_color)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color))

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
                Toast.makeText(context, context?.getString(R.string.sms_permission), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
