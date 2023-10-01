package com.example.dashcarr.presentation

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
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
    private val dashcamButtons by lazy { findViewById<ConstraintLayout>(R.id.floating_buttons) }
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
        val buttonShowAnimation = ObjectAnimator.ofFloat(dashcamButtons, "translationY", 150F, 0F).apply {
            duration = 600L
            interpolator = BounceInterpolator()
        }
        buttonShowAnimation.doOnStart { dashcamButtons.visibility = View.VISIBLE }
        val buttonHideAnimation = ObjectAnimator.ofFloat(dashcamButtons, "translationY", 0F, 150F).apply {
            duration = 300L
            interpolator = LinearInterpolator()
        }
        buttonHideAnimation.doOnEnd { dashcamButtons.visibility = View.GONE }
        val list =
            NavController.OnDestinationChangedListener { controller, destination, arguments ->
                buttonHideAnimation.start()
            }
        navController.addOnDestinationChangedListener(list)

        findViewById<FloatingActionButton>(R.id.camera_button).setOnClickListener {
            if (dashcamButtons.isVisible) {
                buttonHideAnimation.start()
            } else {
                buttonShowAnimation.start()
            }
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

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.nav_host_container, DashcamFragment.getInstance())
        transaction.disallowAddToBackStack()
        transaction.commit()
        findViewById<ImageButton>(R.id.dashcam_button).setOnClickListener {
            DashcamFragment.getInstance().update()
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
                Toast.makeText(this, "No valid file explorer found", Toast.LENGTH_SHORT).show()
            }

        }

    }
}
