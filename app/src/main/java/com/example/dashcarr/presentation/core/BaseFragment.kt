package com.example.dashcarr.presentation.core

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.dashcarr.R

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseFragment<VB: ViewBinding>(
    private val inflate: Inflate<VB>,
    private val showBottomNavBar: Boolean?
): Fragment() {

    private var _binding: VB? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        Log.e("WatchingSomeStuff", "show = $showBottomNavBar")
        showBottomNavBar?.let { showBottomNavigation(it) }
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    protected fun showBottomNavigation(show: Boolean) {
        Log.e("BottomStuff", "show bottom func show = $show")
        val bottomNavigationView = activity?.findViewById<Group>(R.id.nav_bar_group)!!
        Log.e("BottomStuff", "bottomNavView = $bottomNavigationView")
        bottomNavigationView?.visibility =
            if (show) View.VISIBLE
            else View.GONE
    }

    abstract fun observeViewModel()
    abstract fun initListeners()

}