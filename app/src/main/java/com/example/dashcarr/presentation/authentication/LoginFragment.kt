package com.example.dashcarr.presentation.authentication

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentLoginBinding
import com.example.dashcarr.extensions.collectWithLifecycle
import com.example.dashcarr.presentation.core.BaseFragment
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(
    FragmentLoginBinding::inflate
) {
    private lateinit var bottomNavigationView: BottomNavigationView
    private val viewModel: LoginViewModel by viewModels()


    private val signInLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract()) {
        val response = it.idpResponse
        when (it.resultCode) {
            Activity.RESULT_OK -> {
                if (response == null) {
                    Log.d("WatchingSomeStuff", "Sign in failure. response = $response")
                    //toastL(R.string.error_something_wrong)
                    //Log.e(TAG, "Sign in failed. response = $response")
                } else {
                    Log.d("WatchingSomeStuff", "Sign in success. response = $response")
                    //viewModel.syncRemoteBookmarks()
                }
            }
            else -> if (response != null) {
                Log.d("WatchingSomeStuff", "Sign in WRONG. response = $response")
//                Log.e(TAG, "Result Error. response = $response")
//                toastL(
//                    if (response.error?.errorCode == NO_NETWORK)
//                        R.string.error_internet_connection else R.string.error_something_wrong
//                )
            }
        }
    }

    private val googleAuthProvider by lazy {
        AuthUI.IdpConfig.GoogleBuilder().setSignInOptions(
            GoogleSignInOptions.Builder()
                .requestIdToken(getString(com.firebase.ui.auth.R.string.default_web_client_id))
                .requestEmail()
                .build()
        ).build()
    }

    private val appleAuthProvider by lazy {
        AuthUI.IdpConfig.AppleBuilder().build()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)!!
        bottomNavigationView?.visibility = View.GONE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        observeViewModel()
        //showAuth(googleAuthProvider)
    }

    override fun observeViewModel() {
        viewModel.emailErrorState.collectWithLifecycle(viewLifecycleOwner) {
            if (it != null) {
                binding.tilEmail.isErrorEnabled = true
                binding.tilEmail.error = getString(it)
            }
            else {
                binding.tilEmail.isErrorEnabled = false
            }
        }
    }

    override fun initListeners() {
        binding.etEmail.doOnTextChanged { _, _, _, _ ->
            viewModel.updateEmail(binding.etEmail.text.toString())
        }
    }

    fun moveToMap(){
        findNavController().navigate(R.id.action_loginFragment_to_action_map)
        bottomNavigationView.visibility = View.VISIBLE
    }

    private fun showAuth(provider: AuthUI.IdpConfig) {
        //showLoading(true)
        signInLauncher.launch(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(listOf(provider))
                .build()
        )
    }
}