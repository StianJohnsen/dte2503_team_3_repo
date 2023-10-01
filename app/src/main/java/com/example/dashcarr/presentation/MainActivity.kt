package com.example.dashcarr.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.dashcarr.R
import com.example.dashcarr.extensions.collectWithLifecycle
import com.example.dashcarr.presentation.tabs.camera.dashcam.DashcamFragment
import com.example.dashcarr.presentation.tabs.camera.security.SecurityCameraFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val navHostFragment by lazy { supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment }
    private val navController by lazy { navHostFragment.navController }
    private val dashcamButtons by lazy { findViewById<LinearLayout>(R.id.floating_buttons) }
    private val dashcamFragment = DashcamFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        addCameraListener()
        findViewById<BottomNavigationView>(R.id.bottom_nav).setupWithNavController(navController)

        initViewModels()
        viewModel.checkAuthentication()
    }

    private fun initViewModels() {
        viewModel.isUserLoggedIn.collectWithLifecycle(this) {
            Log.e("WatchingSomeStuff", "Is userlogged in = $it")
            if (!it) navController.navigate(R.id.action_global_loginFragment)
        }
    }

    @SuppressLint("ResourceType")
    private fun addCameraListener() {
        val list =
            NavController.OnDestinationChangedListener { controller, destination, arguments ->
                dashcamButtons.visibility = View.GONE
            }
        navController.addOnDestinationChangedListener(list)

        dashcamButtons.setOnClickListener {
            dashcamButtons.visibility = View.GONE
        }
        findViewById<FloatingActionButton>(R.id.camera_button).setOnClickListener {
            if (dashcamButtons.isVisible) {
                dashcamButtons.visibility = View.GONE
            } else {
                dashcamButtons.visibility = View.VISIBLE
            }
        }
        navHostFragment.view?.setOnClickListener {
            dashcamButtons.visibility = View.GONE
        }

        findViewById<ImageButton>(R.id.security_camera_button).setOnClickListener {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.addToBackStack(null)
            transaction.replace(R.id.nav_host_container, SecurityCameraFragment())
            transaction.commit()
            findViewById<Group>(R.id.nav_bar_group).visibility = View.GONE
            dashcamButtons.visibility = View.GONE
            supportFragmentManager.addOnBackStackChangedListener {
                if (supportFragmentManager.fragments[0] !is SecurityCameraFragment) {
                    findViewById<Group>(R.id.nav_bar_group).visibility = View.VISIBLE
                    dashcamButtons.visibility = View.VISIBLE
                }
            }
        }
        findViewById<ImageButton>(R.id.dashcam_button).setOnClickListener {
            val transaction = supportFragmentManager.beginTransaction()
            dashcamFragment.onStop()
            transaction.replace(R.id.main_layout, dashcamFragment)
            transaction.disallowAddToBackStack()
            transaction.commit()
        }
    }
}