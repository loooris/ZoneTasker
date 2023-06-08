package com.github.loooris.zonetasker

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.github.loooris.viewmodel.MainVM
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.github.loooris.zonetasker.databinding.ActivityMapBinding
import com.github.loooris.zonetasker.databinding.FragmentReminderBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import java.io.IOException

@SuppressLint("MissingPermission")
class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private lateinit var binding: ActivityMapBinding

    private var marker: Marker? = null
    private var circle: Circle? = null
    private var latLng = LatLng(46.2043907,6.1431577)

    private lateinit var viewModel: MainVM
    private lateinit var map: GoogleMap
    private val locationRequest = LocationRequest()
    private var initiateMapZoom = true
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var GEOFENCE_RADIUS = 50.00

    private var currentLocation: Location? = null

    // Add a flag variable to track the map clickability
    private var isMapClickable = true

    companion object {
        private const val LOCATION_PERMISSION = 101
        private const val LOCATION_REQUEST_CODE = 102

        //Geneve
        private var GEOFENCE_LAT = 46.2043907
        private var GEOFENCE_LONG = 6.1431577


        private const val CHANNEL_ID = "200"
        private const val NOTIFICATION_ID = 103
        private const val CHANNEL_NAME = "PushNotificationChannel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TopAppBar Handling
        val mapTopAppBar = findViewById<MaterialToolbar>(R.id.MapToolbar)
        mapTopAppBar.setNavigationOnClickListener {
            // Handle navigation icon press
            finish()
        }

        //As soon as the application starts, we get the location permission and create the map.
        askLocationPermission()
    }

    private fun prepareActivity() {
        viewModel = ViewModelProvider(this).get(MainVM::class.java)
        initObservers()
        initMap()
        initFusedLocationClient()
        initUIComponent()
    }

    private fun isLocationPermissionGranted():Boolean{
        return  ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun askLocationPermission() {
        if (!isLocationPermissionGranted()) {
            //For first launch...
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION)
            }
        } else {
            prepareActivity()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode)
        {
            LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (isLocationPermissionGranted()) {
                        prepareActivity()
                    }
                } else {
                    askLocationPermission()
                }
                return
            }
        }
    }


    private fun initUIComponent() {
        // Find the BottomAppBar
        val bottomAppBar: BottomAppBar = findViewById(R.id.bottomAppBar)

        // Find the Slider
        val slider: Slider = findViewById(R.id.slider)

        // Find the FloatingActionButton for going to the geofence map
        val goToGeofenceMapFAB: FloatingActionButton = findViewById(R.id.goToGeofenceMapFAB)

        // Find the Button for adding a marker
        val btnAddMarker: Button = findViewById(R.id.btn_add_marker)

        // Find the ExtendedFloatingActionButton for going to the main menu
        val goToMainMenuFAB: ExtendedFloatingActionButton = findViewById(R.id.goToMainMenuFAB)

        // Find the AppBarLayout to be initially hidden
        val geofenceMapAppBarLayout: AppBarLayout = findViewById(R.id.geofenceMapAppBarLayout)

        // Set the AppBarLayout visibility to GONE initially
        geofenceMapAppBarLayout.visibility = View.GONE

        // Hide the goToMainMenuFAB initially
        goToMainMenuFAB.hide()

        // Set a click listener for the FloatingActionButton
        goToMainMenuFAB.setOnClickListener {
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
        }

        goToGeofenceMapFAB.setOnClickListener {

            // Disable map click
            isMapClickable = false

            // Remove the Slider
            val parentViewSlider: ViewGroup = slider.parent as ViewGroup
            parentViewSlider.removeView(slider)

            // Remove the goToGeofenceMapFAB button
            val parentViewGoToFAB: ViewGroup = goToGeofenceMapFAB.parent as ViewGroup
            parentViewGoToFAB.removeView(goToGeofenceMapFAB)

            // Remove the btn_add_marker button
            val parentViewAddMarker: ViewGroup = btnAddMarker.parent as ViewGroup
            parentViewAddMarker.removeView(btnAddMarker)

            // Remove the BottomAppBar
            val parentView: ViewGroup = bottomAppBar.parent as ViewGroup
            parentView.removeView(bottomAppBar)

            // Show the AppBarLayout
            geofenceMapAppBarLayout.visibility = View.VISIBLE

            // Show the goToMainMenuFAB
            goToMainMenuFAB.show()

        }
    }



    //Responsible for add marker in current location.
    private fun initFusedLocationClient(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                val markerOptions = MarkerOptions().position(LatLng(location.latitude, location.longitude)).title("Current Location")
                marker = map.addMarker(markerOptions)
            }
        }
    }

    //It creates GeofenceZones.
    private fun getGeofenceZone(latLng:LatLng,radius:Double): CircleOptions {
        return CircleOptions()
            .center(latLng)
            .radius(radius)
            .fillColor(0x40ff0000)
            .strokeColor(Color.BLUE)
            .strokeWidth(2f)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LOCATION_REQUEST_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    requestMyGpsLocation { location ->
                        if (initiateMapZoom) {
                            initiateMapZoom = false
                            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15.0F))
                        }
                    }
                }
            }
        }
    }

    private fun initObservers() {
        viewModel.showNotificationEvent.observe(this) { showNotification() }
    }

    private fun initMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun showNotification() {
        val title = ReminderFragment.message
        //val title = "In Geofence"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel: NotificationChannel?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setAutoCancel(true)
            .setSmallIcon(R.mipmap.ic_launcher)
        builder.priority = NotificationCompat.PRIORITY_HIGH
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    override fun onMapReady(googleMap: GoogleMap) {
        setupMap(googleMap)
        getCurrentLocation()

        // Fetch the current location and set the marker + geofence
        setupMapAfterGettingLocation()

        // Set up on map click listener to add marker on click and remove old marker
        googleMap.setOnMapClickListener(this)
    }


    @SuppressLint("MissingPermission")
    private fun setupMapAfterGettingLocation() {
        val client = LocationServices.getFusedLocationProviderClient(this)
        client.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                map.clear() // Clear any existing markers or circles

                // Marker
                val markerOptions = MarkerOptions().position(currentLatLng).title("Current Location")
                marker = map.addMarker(markerOptions)

                // Geofence
                circle = map.addCircle(getGeofenceZone(currentLatLng, GEOFENCE_RADIUS))
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15.0F))

                //Slider
                val slider: Slider = findViewById(R.id.slider)
                latLng = currentLatLng
                slider.addOnChangeListener { _, value, _ ->
                    GEOFENCE_RADIUS = value.toDouble()
                    circle?.remove()
                    circle = map.addCircle(getGeofenceZone(latLng, GEOFENCE_RADIUS))
                }
            }
        }
    }


    private fun setupMap(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isScrollGesturesEnabled = true
        map.uiSettings.setAllGesturesEnabled(true)
        map.uiSettings.isMyLocationButtonEnabled = true
        map.setPadding(0, 175, 0, 0)

        circle = map.addCircle(getGeofenceZone(latLng, GEOFENCE_RADIUS))

    }

    private fun getCurrentLocation() {
        map?.isMyLocationEnabled = true
        locationRequest.interval = 5000
        locationRequest.smallestDisplacement = 10f
        locationRequest.fastestInterval = 3000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)

        val task = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())

        task.addOnCompleteListener {
            try {
                val result = it.getResult(ApiException::class.java)
                if (result?.locationSettingsStates?.isLocationPresent == true) {
                    requestMyGpsLocation { location ->
                        currentLocation = location // Update currentLocation field
                        if (initiateMapZoom) {
                            initiateMapZoom = false
                            map?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(location.latitude, location.longitude),
                                    12F
                                )
                            )
                        }
                    }
                }
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        try {
                            val resolvable = exception as ResolvableApiException
                            resolvable.startResolutionForResult(this, LOCATION_REQUEST_CODE)
                        }
                        catch (e: IntentSender.SendIntentException) {
                            Log.d("", "exception catched at getCurrentLocation: $e")
                        }
                        catch (e: ClassCastException) {
                            Log.d("", "exception catched at getCurrentLocation: $e")
                        }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> { }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestMyGpsLocation(callback: (location: Location) -> Unit) {
        val client = LocationServices.getFusedLocationProviderClient(this)
        client.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    callback.invoke(location)
                    viewModel.checkForGeoFenceEntry(location, latLng.latitude, latLng.longitude, GEOFENCE_RADIUS)
                }
            }
        }, null)
    }


    // Add new marker on map click and remove old marker
    override fun onMapClick(latLng: LatLng) {

        // Check if the map is clickable
        if (isMapClickable) {
        // Marker
        marker?.remove()
        val markerOptions = MarkerOptions().position(latLng)
        marker = map.addMarker(markerOptions)

        this.latLng = latLng

        // Geofence + Circle
        circle?.remove()
        circle = map.addCircle(getGeofenceZone(latLng, GEOFENCE_RADIUS))
        }

    }

    fun addMarkerAtCurrentLocation(view: View) {
        // Remove existing marker
        marker?.remove()

        // Check if currentLocation is not null
        currentLocation?.let { location ->

            // Create a new marker at the current location & Move the Camera Center
            latLng = LatLng(location.latitude, location.longitude)
            val markerOptions = MarkerOptions().position(latLng).title("Current Location")
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0F))
            marker = map.addMarker(markerOptions)

            // Geofence + Circle
            circle?.remove()
            circle = map.addCircle(getGeofenceZone(latLng, GEOFENCE_RADIUS))
        }
    }

}
