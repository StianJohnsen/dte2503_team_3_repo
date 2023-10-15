package com.example.dashcarr.presentation.mapper

import com.example.dashcarr.domain.entity.PointOfInterestEntity
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Converts a [PointOfInterestEntity] to an [org.osmdroid.views.overlay.Marker] on the provided [MapView].
 *
 * @param mapView The MapView on which the Marker will be displayed.
 * @return A Marker object representing the PointOfInterestEntity on the MapView.
 */
fun PointOfInterestEntity.toMarker(mapView: MapView): Marker {
    val pointId = this.id
    return Marker(mapView).apply {
        id = pointId.toString()
        position = GeoPoint(latitude, longitude)
        title = name
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    }
}