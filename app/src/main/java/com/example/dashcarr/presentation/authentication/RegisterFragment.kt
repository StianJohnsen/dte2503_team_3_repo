//package com.example.dashcarr.presentation.authentication
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.viewModels
//import com.example.dashcarr.R
//import com.example.dashcarr.presentation.core.BaseFragment
//import com.google.android.material.bottomnavigation.BottomNavigationView
//import dagger.hilt.android.AndroidEntryPoint
//
//@AndroidEntryPoint
//class RegisterFragment : BaseFragment<FragmentRegisterBinding>(
//    FragmentRegisterBinding::inflate
//) {
//    private lateinit var bottomNavigationView: BottomNavigationView
//    private val viewModel: RegisterViewModel by viewModels()
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_register, container, false)
//    }
//
//    override fun observeViewModel() {
//        TODO("Not yet implemented")
//    }
//
//    override fun initListeners() {
//        TODO("Not yet implemented")
//    }
//
//}