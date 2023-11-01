package com.example.dashcarr.presentation

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Animatable
import android.content.res.ColorStateList
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.os.PowerManager
import android.view.View
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.dashcarr.NavGraphDirections
import com.example.dashcarr.R
import com.example.dashcarr.extensions.collectWithLifecycle
import com.example.dashcarr.extensions.setHeightSmooth
import com.example.dashcarr.presentation.tabs.camera.dashcam.DashcamFragment
import com.example.dashcarr.presentation.tabs.camera.security.SecurityCameraFragment
import com.example.dashcarr.presentation.tabs.settings.PowerSavingMode
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val navHostFragment by lazy { supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment }
    private val navController by lazy { navHostFragment.navController }
    private val slidingBox by lazy { findViewById<View>(R.id.sliding_box) }

    private val powerSaveReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == PowerManager.ACTION_POWER_SAVE_MODE_CHANGED) {
                val powerSaveMode = context?.let { isPowerSaveMode(it) }
                if (powerSaveMode != null) {
                    PowerSavingMode.setPhonePowerMode(powerSaveMode)
                }
            }
        }
    }

    fun isPowerSaveMode(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isPowerSaveMode ?: false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initRedButton()

        val batteryManager: BatteryManager = baseContext.getSystemService(BATTERY_SERVICE) as BatteryManager
        PowerSavingMode.setInitialBatteryCapacity(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY))

        findViewById<BottomNavigationView>(R.id.bottom_nav).setupWithNavController(navController)

        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavBar.setupWithNavController(navController)

        // Define colors for different states
        val states = arrayOf(
            intArrayOf(android.R.attr.state_selected), // selected state
            intArrayOf(-android.R.attr.state_selected)  // normal state
        )

        // Define colors associated with each state
        val iconColors = intArrayOf(
            ContextCompat.getColor(this, R.color.mainBlue), // selected state: blue
            ContextCompat.getColor(this, R.color.gray2)  // normal state: gray
        )

        // Define text colors
        val textColors = intArrayOf(
            ContextCompat.getColor(this, R.color.mainBlue), // selected state: blue
            ContextCompat.getColor(this, android.R.color.transparent) // normal state: transparent (no text)
        )

        // Apply the colors to the BottomNavigationView
        bottomNavBar.itemIconTintList = ColorStateList(states, iconColors)
        bottomNavBar.itemTextColor = ColorStateList(states, textColors)

        initViewModels()
    }

    override fun onResume() {
        super.onResume()

        val intentFilter = IntentFilter()
        intentFilter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)

        registerReceiver(powerSaveReceiver, intentFilter)
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
    private fun initRedButton() {
        val cameraButton = findViewById<FloatingActionButton>(R.id.camera_button)

        cameraButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.mainBlue))

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

        val redButton = findViewById<ImageButton>(R.id.big_red_button)
        (redButton.drawable as Animatable).start()

        val hideCameraButtons = {
            redButton.animate()
                .setDuration(2000)
                .translationY(2000F)
                .withEndAction { redButton.visibility = View.GONE }
                .start()
            slidingBox.setHeightSmooth(
                500,
                0,
                doOnEnd = {
                    slidingBox.visibility = View.GONE

                })
            hideIconSwitch { cameraButton.setImageResource(R.drawable.ic_car_100) }
        }
        hideCameraButtons()
        val list =
            NavController.OnDestinationChangedListener { _, _, _ ->
                if (slidingBox.isVisible) {
                    hideCameraButtons()
                }
            }
        navController.addOnDestinationChangedListener(list)


        cameraButton.setOnClickListener {
            if (slidingBox.isVisible) {
                hideCameraButtons()
            } else {
                redButton.animate()
                    .withStartAction { redButton.visibility = View.VISIBLE }
                    .setDuration(1000)
                    .translationY(0F)
                    .withEndAction { }
                    .start()
                slidingBox.setHeightSmooth(
                    190,
                    260,
                    doOnStart = { slidingBox.visibility = View.VISIBLE },
                    doOnEnd = {})
                hideIconSwitch {
                    cameraButton.setImageResource(R.drawable.ic_down_96)
                }
            }
        }
        redButton.setOnClickListener {
            findViewById<Group>(R.id.nav_bar_group).visibility = View.GONE
            val action = NavGraphDirections.actionGlobalActionMap(true)
            navController.navigate(action)
        }

    }
}
