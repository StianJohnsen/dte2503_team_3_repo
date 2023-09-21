package com.example.dashcarr.presentation

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.dashcarr.R
import com.example.dashcarr.extensions.collectWithLifecycle
import com.example.dashcarr.presentation.tabs.dashcam.DashCamViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val cameraModel: DashCamViewModel by viewModels()
    private val navHostFragment by lazy { supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment }
    private val navController by lazy { navHostFragment.navController }
    private val dashcamButtons by lazy { findViewById<LinearLayout>(R.id.floating_buttons) }
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
        cameraModel.initViewModel(this)
    }

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

        val button = findViewById<ImageButton>(R.id.camera_recording_button)
        button.setOnClickListener {
            cameraModel.startRecording(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraModel.startRecording(this)
    }
}