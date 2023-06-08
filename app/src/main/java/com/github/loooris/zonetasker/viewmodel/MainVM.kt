package com.github.loooris.zonetasker.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import com.github.loooris.zonetasker.utils.SingleLiveEvent
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

class MainVM:ViewModel() {
    val showNotificationEvent by lazy { SingleLiveEvent<Void>() }

    fun checkForGeoFenceEntryorExit(userLocation: Location, geofenceLat: Double, geofenceLong: Double, radius: Double, selectedTrigger: String) {
        val startLatLng = LatLng(userLocation.latitude, userLocation.longitude)
        val geofenceLatLng = LatLng(geofenceLat, geofenceLong)

        val distanceInMeters = SphericalUtil.computeDistanceBetween(startLatLng, geofenceLatLng)

        if (distanceInMeters < radius && selectedTrigger == "Entering") {
            // User is inside the Geo-fence
            showNotificationEvent.call()
        } else if (distanceInMeters > radius && selectedTrigger == "Exiting"){
            // User is outside the Geo-fence
            showNotificationEvent.call()
        }

    }

}