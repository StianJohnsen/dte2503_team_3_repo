package com.example.dashcarr.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentLoginBinding
import com.example.dashcarr.databinding.FragmentSettingsBinding
import com.example.dashcarr.viewmodel.ViewModelExample
import com.google.android.material.bottomnavigation.BottomNavigationView

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    private lateinit var bottomNavigationView: BottomNavigationView
    private val viewModel: ViewModelExample by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentBinding = FragmentLoginBinding.inflate(inflater,container,false)
        binding = fragmentBinding

        bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)!!

        bottomNavigationView?.visibility = View.GONE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            loginFragment = this@LoginFragment
            loginButton.setOnClickListener { moveToMap() }
        }
    }

    fun moveToMap(){
        findNavController().navigate(R.id.action_loginFragment_to_action_map)
        bottomNavigationView.visibility = View.VISIBLE

    }
}