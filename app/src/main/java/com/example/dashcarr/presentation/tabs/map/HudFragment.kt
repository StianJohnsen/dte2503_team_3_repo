package com.example.dashcarr.presentation.tabs.map


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.getSystemService
import com.example.dashcarr.databinding.FragmentHudBinding
import com.example.dashcarr.extensions.setHeightSmooth
import com.example.dashcarr.presentation.core.BaseFragment

/**
 * The Fragment for displaying live data of the current drive. This fragment behaves diffently according to its parent.
 * Look into [onViewCreated] for details.
 */
class HudFragment : BaseFragment<FragmentHudBinding>(
    FragmentHudBinding::inflate,
    showBottomNavBar = true
) {
    companion object {
        const val MIN_DISTANCE_CHANGE_FOR_UPDATES = 1F
        const val MIN_TIME_BETWEEN_UPDATES = 1000L
    }

    private lateinit var textCanvas: SessionInformationDrawable

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (parentFragment == null) {
            // Display the data rotated, mirrored and with black background for head up display usage.
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                view.windowInsetsController?.hide(
                    WindowInsets.Type.systemBars()
                )
            } else {
                @Suppress("DEPRECATION")
                view.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            }
            showBottomNavigation(false)
            textCanvas =
                SessionInformationDrawable(requireContext(), 300F, 150F, true) {}
            binding.hudImage.setHeightSmooth(0, view.height, true)
            binding.hudImage.scaleY = -1F
            binding.imageContainer.setBackgroundColor(Color.BLACK)
        } else {
            textCanvas =
                SessionInformationDrawable(
                    requireContext(),
                    130F, 70F,
                    false
                ) {
                    binding.hudImage.setHeightSmooth(0, it, true)
                }
        }
        binding.hudImage.setImageDrawable(textCanvas)

        permissionHandling()
    }

    private fun permissionHandling() {
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                locationHandler()
            } else {
                Toast.makeText(requireActivity(), "Could not access current location", Toast.LENGTH_LONG).show()
                if (parentFragment == null) {
                    requireActivity().onBackPressed()
                }
            }
        }.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @SuppressLint("MissingPermission")
    private fun locationHandler() {
        val locationManager = requireActivity().getSystemService<LocationManager>()
        val isGPSEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (isGPSEnabled == true) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BETWEEN_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        textCanvas.updateLocation(
                            location
                        )
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