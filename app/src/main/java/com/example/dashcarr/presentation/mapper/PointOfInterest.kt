package com.example.dashcarr.presentation.mapper

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.dashcarr.R
import com.example.dashcarr.domain.entity.PointOfInterestEntity
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Converts this PointOfInterestEntity to a Marker for display on a MapView.
 * Sets up the marker with the entity's location, name, and custom icon.
 *
 * @param mapView The MapView instance on which the marker will be displayed.
 * @param context The current context, used to retrieve drawable resources.
 * @return Marker A Marker instance representing this point of interest on the map.
 */
fun PointOfInterestEntity.toMarker(mapView: MapView, context: Context): Marker {
    val pointId = this.id
    return Marker(mapView).apply {
        id = pointId.toString()
        position = GeoPoint(latitude, longitude)
        title = name
        icon = ContextCompat.getDrawable(context, R.drawable.ic_poi)
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    }
}
