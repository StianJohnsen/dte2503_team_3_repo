package com.example.dashcarr.presentation

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.BatteryManager
import android.os.Bundle
import android.os.PowerManager
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.dashcarr.NavGraphDirections
import com.example.dashcarr.R
import com.example.dashcarr.extensions.collectWithLifecycle
import com.example.dashcarr.extensions.setHeightSmooth
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
    private val rideButton by lazy { findViewById<FloatingActionButton>(R.id.camera_button) }
    private val arrowView by lazy { findViewById<ImageView>(R.id.arrow) }
    private val redButton by lazy { findViewById<ImageButton>(R.id.big_red_button) }
    private val redButtonFrame by lazy { findViewById<ConstraintLayout>(R.id.cl_height_adjustment) }

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
        initRedButtonListeners()

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

        // Define colors associated with each icon
        val iconColors = intArrayOf(
            ContextCompat.getColor(this, R.color.ride_button), // selected state: blue
            ContextCompat.getColor(this, R.color.icon_color)  // normal state: gray
        )

        // Define text colors
        val textColors = intArrayOf(
            ContextCompat.getColor(this, R.color.text_color), // selected state: blue
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
            if (it) navController.navigate(R.id.action_loginFragment_to_productFrontPage)
        }
        viewModel.userState.collectWithLifecycle(this) {
            it?.let {
                viewModel.syncRemoteAndLocalDB()
            }
        }
    }

    fun showRedButton(startDelay: Long = 0) {
        (redButton.drawable as AnimatedVectorDrawable).reset()

        redButtonFrame.animate()
            .withStartAction {
                arrowView.visibility = View.VISIBLE
                redButton.visibility = View.VISIBLE
            }
            .setDuration(1000)
            .setStartDelay(startDelay)
            .translationY(0F)
            .start()
        slidingBox.setHeightSmooth(
            190,
            260,
            doOnStart = { slidingBox.visibility = View.VISIBLE },
            doOnEnd = {},
            startDelay = startDelay
        )
        hideIconSwitch {
            rideButton.setImageResource(R.drawable.ic_down_96)
        }
    }

    fun hideRedButton(startDelay: Long = 0, endAction: () -> Unit = {}) {
        redButtonFrame.animate()
            .setDuration(1800)
            .setStartDelay(startDelay)
            .translationY(2000F)
            .withEndAction {
                arrowView.visibility = View.GONE
                redButton.visibility = View.GONE
            }
            .start()
        slidingBox.setHeightSmooth(
            500,
            0,
            doOnEnd = {
                slidingBox.visibility = View.GONE
                endAction()
            },
            startDelay = startDelay
        )
        hideIconSwitch(startDelay = startDelay, middleEvent = { rideButton.setImageResource(R.drawable.ic_car_100) })
    }

    private fun hideIconSwitch(startDelay: Long = 0, middleEvent: () -> Unit) {
        rideButton.animate().apply {
            duration = 200L
            rotationY(90F)
        }.withEndAction {
            middleEvent()
            rideButton.animate().apply {
                duration = 200L
                rotationY(0F)
            }
        }.setStartDelay(startDelay)
            .start()
    }

    @SuppressLint("ResourceType")
    private fun initRedButtonListeners() {
        (arrowView.drawable as Animatable).start()
        hideRedButton()
        val destinationChangedListener =
            NavController.OnDestinationChangedListener { _, destination, args ->
                val isRideStarting = args?.getBoolean("isRideActivated", false) ?: false
                if (slidingBox.isVisible && !isRideStarting) {
                    hideRedButton()
                }
            }
        navController.addOnDestinationChangedListener(destinationChangedListener)

        rideButton.setOnClickListener {
            if (slidingBox.isVisible) {
                hideRedButton()
            } else {
                showRedButton()
            }
        }
        redButton.setOnClickListener {
            (redButton.drawable as Animatable).start()
            hideRedButton(200L) {
                val action = NavGraphDirections.actionGlobalActionMap(true)
                navController.navigate(action)
            }
        }

    }
}
