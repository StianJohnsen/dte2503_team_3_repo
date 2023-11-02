package com.example.dashcarr.presentation.authentication.login

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment responsible for user login functionality.
 */
@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(
    FragmentLoginBinding::inflate,
    showBottomNavBar = false
) {
    private val viewModel: LoginViewModel by viewModels()

    @SuppressLint("RestrictedApi")
    private val signInLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract()) {
        val response = it.idpResponse
        when (it.resultCode) {
            Activity.RESULT_OK -> {
                if (response == null) {
                } else {
                    if (response.user.providerId.lowercase().contains("facebook")) {
                        saveCredentialsToFirebase(response.idpToken)
                    } else findNavController().navigate(R.id.action_loginFragment_to_productFrontPage)
                }
            }
            else -> if (response != null) {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        observeViewModel()
    }

    /**
     * Observes the ViewModel's LiveData for error states and login status.
     */
    private fun observeViewModel() {
        viewModel.emailErrorState.collectWithLifecycle(viewLifecycleOwner) {
            if (it != null) {
                binding.tilEmail.isErrorEnabled = true
                binding.tilEmail.error = getString(it)
            } else {
                binding.tilEmail.isErrorEnabled = false
            }
        }

        viewModel.passwordErrorState.collectWithLifecycle(viewLifecycleOwner) {
            if (it != null) {
                binding.tilPassword.isErrorEnabled = true
                binding.tilPassword.error = getString(it)
            } else {
                binding.tilPassword.isErrorEnabled = false
            }
        }

        viewModel.loginState.collectWithLifecycle(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(R.id.action_loginFragment_to_productFrontPage)
            } else {
                Toast.makeText(requireContext(), "Login failed", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.googleLoginState.collectWithLifecycle(viewLifecycleOwner) {
            showAuth(googleAuthProvider)
        }
    }

    /**
     * Initializes listeners for user input and button clicks.
     */
    private fun initListeners() {
        binding.etEmail.doOnTextChanged { _, _, _, _ ->
            viewModel.updateEmail(binding.etEmail.text.toString())
        }

        binding.etPassword.doOnTextChanged { _, _, _, _ ->
            viewModel.updatePassword(binding.etPassword.text.toString())
        }

        binding.btnGoogleLogin.setOnClickListener {
            viewModel.showGoogleLogin()
        }
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(),
                    getString(R.string.error_enter_both_email_and_password),
                    Toast.LENGTH_SHORT).show()
            } else {
                viewModel.signIn(email, password)
            }
        }

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            loginFragment = this@LoginFragment
        }
    }

    /**
     * Shows the authentication process for the specified provider.
     *
     * @param provider The authentication provider configuration.
     */
    private fun showAuth(provider: AuthUI.IdpConfig) {
        signInLauncher.launch(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(listOf(provider))
                .build()
        )
    }

    private fun saveCredentialsToFirebase(accessToken: String?) {
        if (accessToken.isNullOrEmpty()) return
        val credentials = FacebookAuthProvider.getCredential(accessToken)
        Firebase.auth.signInWithCredential(credentials)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(R.id.action_loginFragment_to_productFrontPage)
                } else {
                    Toast.makeText(requireContext(), getString(R.string.error_unknown), Toast.LENGTH_SHORT).show()
                }
            }
    }
}