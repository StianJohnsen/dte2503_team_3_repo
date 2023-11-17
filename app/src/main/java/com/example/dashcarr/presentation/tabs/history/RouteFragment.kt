package com.example.dashcarr.presentation.tabs.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentRouteBinding
import com.example.dashcarr.presentation.core.BaseFragment
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


class RouteFragment : BaseFragment<FragmentRouteBinding>(
    FragmentRouteBinding::inflate,
    showBottomNavBar = false
) {
    private var map: MapView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Since we are using ViewBinding, there's no need to call super.onCreateView.
        _binding = FragmentRouteBinding.inflate(inflater, container, false)
        val view = binding.root

        Configuration.getInstance()
            .load(
                requireActivity(),
                android.preference.PreferenceManager.getDefaultSharedPreferences(requireActivity())
            )

        map = view.findViewById(R.id.map)
        map?.setTileSource(TileSourceFactory.MAPNIK)
        map?.setMultiTouchControls(true)

        val args: RouteFragmentArgs by navArgs()
        val fileName = args.selectedFileName
        val geoPoints = loadPointsFromCSV(fileName)

        if (geoPoints.isNotEmpty()) {
            val centerPoint = getCenterPoint(geoPoints)
            map?.controller?.setCenter(centerPoint)
            val line = Polyline()
            line.setPoints(geoPoints)
            map?.overlays?.add(line)
            map?.controller?.setZoom(18.0)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: RouteFragmentArgs by navArgs()

        val recordName = args.title
        val title = "Route: $recordName"
        val filePath = File(context?.filesDir, args.selectedFileName).absolutePath
        val totalDistance = calculateTotalDistanceFromCSV(filePath)
        val averageSpeed = calculateAverageSpeedFromCSV(filePath, args.elapsedTime)
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

        binding.textRecordingName.text = title
        binding.inputTotalTime.text = args.elapsedTime
        binding.inputFileDate.text = LocalDateTime.parse(args.date).format(formatter)
        binding.inputTotalDistance.text = String.format("%.2f km", totalDistance / 1000) // Convert to kilometers
        binding.inputAverageSpeed.text = String.format("%.2f km/h", averageSpeed)

        binding.imageBackDetails.setOnClickListener {
            findNavController().navigate(R.id.action_action_route_to_SavedRecordingsFragment)
        }
    }

    private fun loadPointsFromCSV(filePath: String): List<GeoPoint> {
        val geoPoints = mutableListOf<GeoPoint>()
        try {
            val file = File(context?.filesDir, filePath)
            BufferedReader(FileReader(file)).use { reader ->
                reader.readLine()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val tokens = line!!.split(",")
                    if (tokens.size >= 5) {
                        try {
                            val latitude = tokens[3].toDouble()
                            val longitude = tokens[2].toDouble()
                            geoPoints.add(GeoPoint(latitude, longitude))
                        } catch (e: NumberFormatException) {
                            Log.e("RouteFragment", "LoadGeoPoints Failed")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RouteFragment", "LoadGeoPoints Failed")
        }
        return geoPoints
    }

    private fun getCenterPoint(geoPoints: List<GeoPoint>): GeoPoint {
        val midLatitude = geoPoints.map { it.latitude }.average()
        val midLongitude = geoPoints.map { it.longitude }.average()
        return GeoPoint(midLatitude, midLongitude)
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radius = 6371e3
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return radius * c
    }

    private fun calculateTotalDistanceFromCSV(filePath: String): Double {
        val points = File(filePath).readLines().drop(1).mapNotNull { line ->
            line.split(",").let {
                if (it.size >= 5) {
                    val latitude = it[3].toDoubleOrNull()
                    val longitude = it[2].toDoubleOrNull()
                    if (latitude != null && longitude != null) Pair(latitude, longitude) else null
                } else null
            }
        }

        return points.zipWithNext { a, b -> calculateDistance(a.first, a.second, b.first, b.second) }.sum()
    }

    private fun calculateAverageSpeedFromCSV(filePath: String, elapsedTimeStr: String): Double {
        val parts = elapsedTimeStr.split(":").map { it.toIntOrNull() ?: return 0.0 }
        if (parts.size < 3) return 0.0
        val hours = parts[0]
        val minutes = parts[1]
        val seconds = parts[2]
        val elapsedTimeHours = hours + minutes / 60.0 + seconds / 3600.0

        val totalDistanceKm = calculateTotalDistanceFromCSV(filePath) / 1000.0

        return totalDistanceKm / elapsedTimeHours
    }
}