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
import android.widget.AutoCompleteTextView
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.IOException

@SuppressLint("MissingPermission")
class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private lateinit var binding: ActivityMapBinding

    private var marker: Marker? = null
    private var circle: Circle? = null

    private lateinit var viewModel: MainVM
//    private var map: GoogleMap? = null
    private lateinit var map: GoogleMap // todo à voir
//    private lateinit var currentLocation : LocationCallback // todo à voir
    private val locationRequest = LocationRequest()
    private var initiateMapZoom = true
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION = 101
        private const val LOCATION_REQUEST_CODE = 102

        //Geneve
        private var GEOFENCE_LAT = 46.2043907
        private var GEOFENCE_LONG = 6.1431577


        private const val GEOFENCE_RADIUS = 1000.00
        private const val CHANNEL_ID = "200"
        private const val NOTIFICATION_ID = 103
        private const val CHANNEL_NAME = "PushNotificationChannel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this) //todo

//        getCurrentLocationUser() todo ancien


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
        if (!isLocationPermissionGranted())
        {
            //For first launch...
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION)
            }
            else
            {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION)
            }
        }
        else
        {
            prepareActivity()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode)
        {
            LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (isLocationPermissionGranted())
                    {
                        prepareActivity()
                    }
                }
                else
                {
                    askLocationPermission()
                }
                return
            }
        }
    }

//    private fun getCurrentLocationUser() { todo ancien check
//
//
//        //Get Location, the last we have (most recently)
//        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
//                location ->
//            //Check if location not null (can happen if location turn off on device)
//            if(location != null){
//                currentLocation=location
//                Toast.makeText(applicationContext, currentLocation.latitude.toString()+""+
//                        currentLocation.longitude.toString(), Toast.LENGTH_LONG).show()
//
//                //Define when the map is ready to be used.
//                val mapFragment = supportFragmentManager
//                    .findFragmentById(R.id.map) as SupportMapFragment
//                mapFragment.getMapAsync(this)
//            }
//        }
//    }

    private fun initUIComponent(){
//        todo check
//        val btnUpdateGeoFenceZone = findViewById<MaterialButton>(com.google.android.gms.location.R.id.btnUpdateGeoFenceZone)
//
//        val edtGeofenceZone = findViewById<AutoCompleteTextView>(com.google.android.gms.location.R.id.edtGeofenceZone)
//        val edtGeofenceRadius = findViewById<EditText>(com.google.android.gms.location.R.id.edtGeofenceRadius)


        // FAB button going to optionsMenu
        val fab: FloatingActionButton = findViewById(R.id.goToOptionsFAB)
        fab.setOnClickListener {
            val intent = Intent(this, OptionsMenuActivity::class.java)
            startActivity(intent)
        }
    }

    //Responsible for add marker in current location.
    private fun initFusedLocationClient(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                map?.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)).title("Current Location"))
            }
        }
    }

//    private fun addCircleToMap(input:String,radius:Double){ todo check
//        val g = Geocoder(this)
//
//        var addressList: List<Address> = listOf()
//
//        try {
//            addressList = g.getFromLocationName(input, 1)
//        }
//        catch (e: IOException) {
//            Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
//            Log.d("","error catched at addCircleToMap: $e")
//        }
//        finally {
//            //To be sure addressList not empty just in case no element at 0 index.
//            if (addressList.isNotEmpty()) {
//                val address = addressList[0]
//                if (address.hasLatitude() && address.hasLongitude()) {
//                    map?.clear()
//                    map?.addCircle(getGeofenceZone(address.latitude,address.longitude,radius))
//                }
//            }
//            else {
//                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    //It creates GeofenceZones.
    private fun getGeofenceZone(lat:Double,lon:Double,radius:Double): CircleOptions {
        return CircleOptions()
            .center(LatLng(lat, lon))
            .radius(radius)
            .strokeColor(ContextCompat.getColor(this, R.color.borderGeofenceZone))
            .fillColor(ContextCompat.getColor(this, R.color.inGeofenceZone))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode)
        {
            LOCATION_REQUEST_CODE -> when (resultCode)
            {
                Activity.RESULT_OK ->
                {
                    requestMyGpsLocation { location ->
                        if (initiateMapZoom)
                        {
                            initiateMapZoom = false
                            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 10F))
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
        val title = "In GeoFence"
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

        // Set up on map click listener to add marker on click and remove old marker
        googleMap.setOnMapClickListener(this)


//        // Set up on map click listener to add marker on click and remove old marker todo rajouter
//        googleMap.setOnMapClickListener(this)
//        val latLng= LatLng(currentLocation.latitude, currentLocation.longitude)
//        val markerOptions = MarkerOptions().position(latLng).title("Current Location")
//
//        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
//        marker = googleMap.addMarker(markerOptions)
    }

    private fun setupMap(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isScrollGesturesEnabled = true
        map.uiSettings.setAllGesturesEnabled(true)
        map.uiSettings.isMyLocationButtonEnabled = true


        val circleOptions =  CircleOptions()
            .center(LatLng(GEOFENCE_LAT, GEOFENCE_LONG))
            .radius(GEOFENCE_RADIUS)
            .fillColor(0x40ff0000)
            .strokeColor(Color.BLUE)
            .strokeWidth(2f)

        circle = map.addCircle(circleOptions)
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
            try
            {
                it.getResult(ApiException::class.java)
                requestMyGpsLocation { location ->
                    if (initiateMapZoom) {
                        initiateMapZoom = false
                        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 10F))
                    }
                }
            }
            catch (exception: ApiException)
            {
                when (exception.statusCode)
                {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        try
                        {
                            val resolvable = exception as ResolvableApiException
                            resolvable.startResolutionForResult(this, LOCATION_REQUEST_CODE)
                        }
                        catch (e: IntentSender.SendIntentException)
                        {
                            Log.d("", "exception catched at getCurrentLocation: $e")
                        }
                        catch (e: ClassCastException)
                        {
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
                    viewModel.checkForGeoFenceEntry(location, GEOFENCE_LAT, GEOFENCE_LONG, GEOFENCE_RADIUS)
                }
            }
        }, null)
    }



    // Add new marker on map click and remove old marker
    override fun onMapClick(latLng: LatLng) {

        // Marker
        marker?.remove()
        val markerOptions = MarkerOptions().position(latLng)
        marker = map.addMarker(markerOptions)

        // Geofence + Circle
        circle?.remove()
        val circleOptions =  CircleOptions()
            .center(LatLng(latLng.latitude, latLng.longitude))
            .radius(GEOFENCE_RADIUS)
            .fillColor(0x40ff0000)
            .strokeColor(Color.BLUE)
            .strokeWidth(2f)

        circle = map.addCircle(circleOptions)

    }

//    fun addMarkerAtCurrentLocation(view: View) { todo à faire
//        //Remove existing marker
//        marker?.remove()
//
//        // Create a new marker at the current location & Move the Camera Center
//        val latLng= LatLng(currentLocation.latitude, currentLocation.longitude)
//        val markerOptions = MarkerOptions().position(latLng).title("Current Location")
//
//        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
//        marker = map.addMarker(markerOptions)
//    }
}
