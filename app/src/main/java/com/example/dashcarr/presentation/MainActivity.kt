package com.example.dashcarr.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.dashcarr.R
import com.example.dashcarr.extensions.collectWithLifecycle
import com.example.dashcarr.extensions.setHeightSmooth
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
    private val floatingCameraButtons by lazy { findViewById<ConstraintLayout>(R.id.floating_buttons) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        addCameraListener()

        findViewById<BottomNavigationView>(R.id.bottom_nav).setupWithNavController(navController)

        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavBar.setupWithNavController(navController)

        initViewModels()
    }

    /**
     * Initializes the [MainViewModel] and observes the user login status.
     */
    private fun initViewModels() {
        viewModel.isUserLoggedIn.collectWithLifecycle(this) {
            if (it) navController.navigate(R.id.action_loginFragment_to_action_map)
        }
    }

    @SuppressLint("ResourceType")
    private fun addCameraListener() {
        val cameraButton = findViewById<FloatingActionButton>(R.id.camera_button)
        fun hideIconSwitch(middleEvent: () -> Unit) {
            cameraButton.animate().apply {
                duration = 200L
                rotationYBy(90F)
            }.withEndAction {
                middleEvent()
                cameraButton.animate().apply {
                    duration = 200L
                    rotationYBy(-90F)
                }
            }
        }

        val hideCameraButtons = {
            floatingCameraButtons.setHeightSmooth(
                500,
                0,
                doOnEnd = {
                    floatingCameraButtons.visibility = View.GONE

                })
            hideIconSwitch { cameraButton.setImageResource(R.drawable.camera_icon) }
        }
        val list =
            NavController.OnDestinationChangedListener { _, _, _ ->
                if (floatingCameraButtons.isVisible) {
                    hideCameraButtons()
                }
            }
        navController.addOnDestinationChangedListener(list)

        cameraButton.setOnClickListener {
            if (floatingCameraButtons.isVisible) {
                hideCameraButtons()
            } else {
                floatingCameraButtons.setHeightSmooth(
                    190,
                    260,
                    doOnStart = { floatingCameraButtons.visibility = View.VISIBLE },
                    doOnEnd = { })
                hideIconSwitch {
                    cameraButton.setImageResource(R.drawable.ic_down_96)
                }
            }
        }

        findViewById<ImageButton>(R.id.security_camera_button).setOnClickListener {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.addToBackStack(null)
            transaction.replace(R.id.nav_host_container, SecurityCameraFragment())
            transaction.commit()
            findViewById<Group>(R.id.nav_bar_group).visibility = View.GONE
            floatingCameraButtons.visibility = View.GONE
            supportFragmentManager.addOnBackStackChangedListener {
                if (supportFragmentManager.fragments[0] !is SecurityCameraFragment) {
                    findViewById<Group>(R.id.nav_bar_group).visibility = View.VISIBLE
                    floatingCameraButtons.visibility = View.VISIBLE
                }
            }
        }

        findViewById<ImageButton>(R.id.dashcam_button).setOnClickListener {
            if (!DashcamFragment.exists()) {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.add(R.id.nav_host_container, DashcamFragment.getInstance())
                transaction.disallowAddToBackStack()
                transaction.commit()
            } else {
                DashcamFragment.getInstance().saveRecording()
            }
        }

        findViewById<ImageButton>(R.id.saved_recordings_button).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(
                Uri.parse(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString() + "/Dashcarr"
                ), "resource/folder"
            )
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "Could not find ES File Explorer", Toast.LENGTH_SHORT).show()
            }

        }

    }
}
