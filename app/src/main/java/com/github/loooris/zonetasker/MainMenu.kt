package com.github.loooris.zonetasker

import android.Manifest
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.github.loooris.zonetasker.databinding.ActivityMainMenuBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import android.os.Handler
import android.os.Looper
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory


private const val TAG = "MainMenu"
private lateinit var geoClient: GeofencingClient
private val REQUEST_TURN_DEVICE_LOCATION_ON = 20
private val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 3
private val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 4
private val REQUEST_LOCATION_PERMISSION = 10

class MainMenu : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private lateinit var binding: ActivityMainMenuBinding

    private lateinit var currentLocation : Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101

    private lateinit var googleMap: GoogleMap
    private var marker: Marker? = null
    private var circle: Circle? = null
    private var radius = 5f
    private var latLng = LatLng(0.0,0.0)

    private lateinit var locationRequest: LocationRequest //////NEW LOCATION UPDATE MARKER BLEU
    private lateinit var locationCallback: LocationCallback //////NEW LOCATION UPDATE MARKER BLEU
    private var secondmarker: Marker? = null //////NEW LOCATION UPDATE MARKER BLEU

    private val gadgetQ = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    private val geofenceIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }


    var geofenceList = ArrayList<Geofence>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        geoClient = LocationServices.getGeofencingClient(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocationUser()

        // FAB button going to optionsMenu
        val fab: FloatingActionButton = findViewById(R.id.goToOptionsFAB)
        fab.setOnClickListener {
            val intent = Intent(this, OptionsMenuActivity::class.java)
            startActivity(intent)
        }

        val slider: Slider = findViewById(R.id.slider)

        slider.addOnChangeListener { _, value, _ ->
            radius = value
            circle?.remove()
            updateCircleOptions(latLng)

            // Update geofence
            geofenceList.clear()
            geofenceBuilder(latLng.latitude, latLng.longitude, radius)
            addGeofence() // Add the geofence

            // Print in console geofenceList contents todo remove
            Log.d(TAG, "geofenceList: $geofenceList")
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)//////NEW LOCATION UPDATE MARKER BLEU
        setupLocationUpdates() //////NEW LOCATION UPDATE MARKER BLEU


    }

    private fun getCurrentLocationUser() {

        //we check if permission granted
        if(ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION)!=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!=
            PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),permissionCode)
            return
        }

        //Get Location, the last we have (most recently)
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                location ->
            //Check if location not null (can happen if location turn off on device)
            if(location != null){
                currentLocation = location
                Toast.makeText(applicationContext, currentLocation.latitude.toString() + "" +
                        currentLocation.longitude.toString(), Toast.LENGTH_LONG).show()

                //Define when the map is ready to be used.
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }
        }
    }

    //We request for permission, if permission not granted
    // TODO check
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when(requestCode){
//            permissionCode -> if(grantResults.isNotEmpty() && grantResults[0]==
//                PackageManager.PERMISSION_GRANTED){
//                getCurrentLocationUser()
//            }
//        }
//    }


    // When map is ready to be used, obtain an instance of Google Map with current location (latitude/longitude) + Add Marker on it
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        // Set up on map click listener to add marker on click and remove old marker
        googleMap.setOnMapClickListener(this)

        latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        val markerOptions = MarkerOptions().position(latLng).title("Current Location")

        updateCircleOptions(latLng)
        geofenceBuilder(latLng.latitude, latLng.longitude, radius)


        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        marker = googleMap.addMarker(markerOptions)
    }

    private fun updateCircleOptions(latLng: LatLng) {
        circle?.remove() // Remove the old circle

        val circleOptions = CircleOptions()
            .center(latLng)
            .radius(radius.toDouble())
            .fillColor(0x40ff0000)
            .strokeColor(Color.BLUE)
            .strokeWidth(2f)

        circle = googleMap.addCircle(circleOptions) // Add the new circle
    }


    // Add new marker on map click and remove old marker
// Add new marker on map click and remove old marker
    override fun onMapClick(latLng: LatLng) {
        marker?.remove()

        val markerOptions = MarkerOptions().position(latLng)
        marker = googleMap.addMarker(markerOptions)

        // Update circle position
        circle?.remove()
        this.latLng = latLng
        updateCircleOptions(latLng)

        // Update geofence
        geofenceList.clear()
        geofenceBuilder(latLng.latitude, latLng.longitude, radius)
        addGeofence() // Add the geofence

        // Print in console geofenceList contents todo remove
        Log.d(TAG, "geofenceList: $geofenceList")
    }



    fun addMarkerAtCurrentLocation(view: View) {
        // Remove existing marker
        marker?.remove()

        // Create a new marker at the current location & Move the Camera Center
        val latLng= LatLng(currentLocation.latitude, currentLocation.longitude)
        val markerOptions = MarkerOptions().position(latLng).title("Current Location")

        circle?.remove()
        updateCircleOptions(latLng)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        marker = googleMap.addMarker(markerOptions)

        // Update circle position
        this.latLng = latLng
        updateCircleOptions(latLng)

        // Update geofence
        geofenceList.clear()
        geofenceBuilder(latLng.latitude, latLng.longitude, radius)
        addGeofence() // Add the geofence

        // Print in console geofenceList contents todo remove
        Log.d(TAG, "geofenceList: $geofenceList")
    }

//////NEW LOCATION UPDATE MARKER BLEU
    private fun setupLocationUpdates() {
        locationRequest = LocationRequest.create().apply {
            interval = 12000 // 12 seconds
            fastestInterval = 10000 // 10 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    currentLocation = location
                    updateMarkerWithCurrentLocation()
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun updateMarkerWithCurrentLocation() {
        // Remove existing marker
        secondmarker?.remove()

        // Create a new marker at the updated current location & Move the Camera Center
        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title("Current Location")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        secondmarker = googleMap.addMarker(markerOptions)
    }
    //////END LOCATION UPDATE MARKER BLEU


/////////////////////////////////////////////////////////////////////////////

    // check if background and foreground permissions are approved
    @TargetApi(29)
    private fun authorizedLocation(): Boolean {
        val formalizeForeground = (
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ))
        val formalizeBackground =
            if (gadgetQ) {
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else {
                true
            }
        return formalizeForeground && formalizeBackground
    }

    @TargetApi(29)
    private fun askLocationPermission() {
        if (authorizedLocation())
            return
        var grantingPermission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val customResult = when {
            gadgetQ -> {
                grantingPermission += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        Log.d(TAG, "askLocationPermission: ")
        ActivityCompat.requestPermissions(
            this,
            grantingPermission,
            customResult
        )

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
                getCurrentLocationUser()
        }
    }

    private fun validateGadgetAreaInitiateGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val locationResponses =
            client.checkLocationSettings(builder.build())

        locationResponses.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(
                        this,
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error geting location settings resolution: " + sendEx.message)
                }
            } else {
                Toast.makeText(this, "Enable your location", Toast.LENGTH_SHORT).show()
            }
        }
        locationResponses.addOnCompleteListener {
            if (it.isSuccessful) {
                addGeofence()

                // Print in console geofenceList contents todo remove
                Log.d(TAG, "geofenceList: $geofenceList")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        validateGadgetAreaInitiateGeofence(false)
    }

    // Fonction for creating geofences
    private fun geofenceBuilder(latitude: Double, longitude: Double, radius: Float) {
        geofenceList.add(Geofence.Builder()
            .setRequestId("entry.key")
            .setCircularRegion(latitude, longitude, radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build())
    }


    private fun seekGeofencing(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)

            // Print in console geofenceList contents todo remove
            Log.d(TAG, "geofenceList: $geofenceList")
        }.build()
    }

    private fun addGeofence() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (geofenceList.isEmpty()) {
            Toast.makeText(this@MainMenu, "No geofences to add", Toast.LENGTH_SHORT).show()
            return
        } else {
            geoClient?.addGeofences(seekGeofencing(), geofenceIntent)?.run {
                addOnSuccessListener {
                    Toast.makeText(this@MainMenu, "Geofences added", Toast.LENGTH_SHORT).show()
                }
                addOnFailureListener {
                    Toast.makeText(this@MainMenu, "Failed to add geofences", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }



    private fun removeGeofence(){
        geoClient?.removeGeofences(geofenceIntent)?.run {
            addOnSuccessListener {
                Toast.makeText(this@MainMenu, "Geofences removed", Toast.LENGTH_SHORT).show()

            }
            addOnFailureListener {
                Toast.makeText(this@MainMenu, "Failed to remove geofences", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun examinePermisionAndinitiatGeofence() {
        if (authorizedLocation()) {
            validateGadgetAreaInitiateGeofence()
        } else {
            askLocationPermission()
        }
    }

    override fun onStart() {
        super.onStart()
        examinePermisionAndinitiatGeofence()
        startLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeGeofence()
    }
}
