package com.example.dashcarr.presentation.tabs.map


import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.getSystemService
import com.example.dashcarr.databinding.FragmentHudBinding
import com.example.dashcarr.presentation.core.BaseFragment
import kotlin.math.roundToInt

class HudFragment : BaseFragment<FragmentHudBinding>(
    FragmentHudBinding::inflate,
    showBottomNavBar = false
) {
    companion object {
        const val MIN_DISTANCE_CHANGE_FOR_UPDATES = 1F
        const val MIN_TIME_BETWEEN_UPDATES = 1000L
    }

    private val textCanvas = TextDrawable()



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            view.windowInsetsController?.hide(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE)
        } else {
            @Suppress("DEPRECATION")
            view.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }

        binding.hudImage.setImageDrawable(textCanvas)
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        permissionHandling()
    }

    private fun permissionHandling() {
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                speedHandler()
            } else {
                Toast.makeText(requireActivity(), "Could not access current location", Toast.LENGTH_LONG).show()
                requireActivity().onBackPressed()
            }
        }.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @SuppressLint("MissingPermission")
    private fun speedHandler() {
        val locationManager = requireActivity().getSystemService<LocationManager>()
        val isGPSEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (isGPSEnabled == true) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BETWEEN_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        textCanvas.setText("${(location.speed * 3.6).roundToInt()} km/h")
                    }

                    override fun onProviderDisabled(provider: String) {
                        Toast.makeText(requireActivity(), "GPS was disabled", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                    }
                }
            )
        } else {
            Toast.makeText(requireActivity(), "GPS is disabled", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
        }
    }

}