package com.github.loooris.zonetasker.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import com.github.loooris.zonetasker.utils.SingleLiveEvent
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

class MainVM : ViewModel() {
    private var prevInsideGeoFence: Boolean? = null
    val showNotificationEvent by lazy { SingleLiveEvent<Void>() }

    fun checkForGeoFenceEntryorExit(
        userLocation: Location,
        geofenceLat: Double,
        geofenceLong: Double,
        radius: Double,
        selectedTrigger: String
    ) {
        val startLatLng = LatLng(userLocation.latitude, userLocation.longitude)
        val geofenceLatLng = LatLng(geofenceLat, geofenceLong)

        val distanceInMeters = SphericalUtil.computeDistanceBetween(startLatLng, geofenceLatLng)
        val isInsideGeoFence = distanceInMeters < radius

        if (prevInsideGeoFence == null) {
            // First time checking, set the initial state based on user location
            prevInsideGeoFence = isInsideGeoFence
        } else {
            if (isInsideGeoFence && !prevInsideGeoFence!! && selectedTrigger == "Entering") {
                // User has entered the geofence
                showNotificationEvent.call()
            } else if (!isInsideGeoFence && prevInsideGeoFence!! && selectedTrigger == "Exiting") {
                // User has exited the geofence
                showNotificationEvent.call()
            }
        }

        prevInsideGeoFence = isInsideGeoFence
    }
}
