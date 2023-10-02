package com.example.dashcarr.presentation.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.dashcarr.R
import com.google.android.material.bottomnavigation.BottomNavigationView

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T
open class BaseFragment<VB: ViewBinding>(
    private val inflate: Inflate<VB>
): Fragment() {

    private var _binding: VB? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        showBottomNavigation(true)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    protected fun showBottomNavigation(show: Boolean) {
        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)!!
        bottomNavigationView?.visibility =
            if (show) View.VISIBLE
            else View.GONE
    }

    abstract fun observeViewModel()
    abstract fun initListeners()

}