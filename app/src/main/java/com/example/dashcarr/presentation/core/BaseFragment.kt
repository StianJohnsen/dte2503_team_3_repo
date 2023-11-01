package com.example.dashcarr.presentation.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.Group
import androidx.navigation.fragment.NavHostFragment
import androidx.viewbinding.ViewBinding
import com.example.dashcarr.R

/**
 * A base fragment class providing common functionalities for other fragments in the application.
 *
 * @param VB The type of ViewBinding used by the fragment.
 * @property inflate Function to inflate the ViewBinding.
 * @property showBottomNavBar Boolean flag indicating whether to show the bottom navigation bar.
 */

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseFragment<VB : ViewBinding>(
    private val inflate: Inflate<VB>,
    private val showBottomNavBar: Boolean?
) : NavHostFragment() {

    /**
     * ViewBinding instance for accessing the views in the fragment.
     */
    private var _binding: VB? = null
    val binding get() = _binding!!

    /**
     * Called to create the view for this fragment.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        showBottomNavBar?.let { showBottomNavigation(it) }
        return binding.root
    }

    /**
     * Called when the view previously created by [onCreateView] has been detached from the fragment.
     */
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    /**
     * Set the visibility of the bottom navigation bar.
     *
     * @param show Boolean flag indicating whether to show the bottom navigation bar.
     */
    protected fun showBottomNavigation(show: Boolean) {
        val bottomNavigationView = requireActivity().findViewById<Group>(R.id.nav_bar_group)
        bottomNavigationView.visibility =
            if (show) View.VISIBLE
            else View.GONE
    }
}