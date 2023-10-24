package com.example.dashcarr.presentation

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
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
import com.example.dashcarr.presentation.tabs.map.HudFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val navHostFragment by lazy { supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment }
    private val navController by lazy { navHostFragment.navController }
    private val floatingCameraButtons by lazy { findViewById<ConstraintLayout>(R.id.floating_buttons) }

    private val powerSaveReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == PowerManager.ACTION_POWER_SAVE_MODE_CHANGED){
                val powerSaveMode = context?.let { isPowerSaveMode(it) }
                if (powerSaveMode == true){
                    Log.d(this::class.simpleName,"PowerSaveMode is on")
                }
                else {
                    Log.d(this::class.simpleName,"PowerSaveMode is off")

                }

            }
        }
    }





    fun isPowerSaveMode(context: Context): Boolean{
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isPowerSaveMode ?: false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        addCameraListener()


        /*
                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

                if (powerManager.isPowerSaveMode){
            Log.d("isPowerModeOn","PowerSaveMode is on")
        }
        else{
            Log.d("isPowerModeOn","PowerSaveMode is off")

        }
         */




        findViewById<BottomNavigationView>(R.id.bottom_nav).setupWithNavController(navController)

        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavBar.setupWithNavController(navController)

        bottomNavBar.findViewById<View>(R.id.action_map).setOnLongClickListener {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.nav_host_container, HudFragment())
            transaction.addToBackStack(null)
            transaction.commit()
            true
        }

        initViewModels()
    }

    override fun onResume() {
        super.onResume()

        val intentFilter = IntentFilter()
        intentFilter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)

        registerReceiver(powerSaveReceiver,intentFilter)
    }


    override fun onPause() {
        super.onPause()
        unregisterReceiver(powerSaveReceiver)
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
        val buttonShowAnimation = ObjectAnimator.ofFloat(floatingCameraButtons, "translationY", 150F, 0F).apply {
            duration = 600L
            interpolator = BounceInterpolator()
        }
        buttonShowAnimation.doOnStart { floatingCameraButtons.visibility = View.VISIBLE }
        val buttonHideAnimation = ObjectAnimator.ofFloat(floatingCameraButtons, "translationY", 0F, 150F).apply {
            duration = 300L
            interpolator = LinearInterpolator()
        }
        buttonHideAnimation.doOnEnd { floatingCameraButtons.visibility = View.GONE }
        val list =
            NavController.OnDestinationChangedListener { _, _, _ ->
                buttonHideAnimation.start()
            }
        navController.addOnDestinationChangedListener(list)

        findViewById<FloatingActionButton>(R.id.camera_button).setOnClickListener {
            if (floatingCameraButtons.isVisible) {
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
                DashcamFragment.getInstance().update()
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
