package com.github.loooris.zonetasker

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Marker
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainMenu : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private lateinit var binding: ActivityMainMenuBinding

    private lateinit var currentLocation : Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101

    private lateinit var googleMap: GoogleMap
    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocationUser()

        // FAB button going to optionsMenu
        val fab: FloatingActionButton = findViewById(R.id.goToOptionsFAB)
        fab.setOnClickListener {
            val intent = Intent(this, OptionsMenuActivity::class.java)
            startActivity(intent)
        }

    }

    private fun getCurrentLocationUser() {
        //we check if permission granted
        if(ActivityCompat.checkSelfPermission(
                this,android.Manifest.permission.ACCESS_FINE_LOCATION)!=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=
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
                currentLocation=location
                Toast.makeText(applicationContext, currentLocation.latitude.toString()+""+
                        currentLocation.longitude.toString(), Toast.LENGTH_LONG).show()

                //Define when the map is ready to be used.
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }
        }
    }

    //We request for permission, if permission not granted
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            permissionCode -> if(grantResults.isNotEmpty() && grantResults[0]==
                PackageManager.PERMISSION_GRANTED){
                getCurrentLocationUser()
            }
        }
    }


    //When map is ready to be used, obtain an instance of google map with current location (latitude/longitude) + Add Marker on it
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        // Set up on map click listener to add marker on click and remove old marker
        googleMap.setOnMapClickListener(this)

        val latLng= LatLng(currentLocation.latitude, currentLocation.longitude)
        val markerOptions = MarkerOptions().position(latLng).title("Current Location")

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        marker = googleMap.addMarker(markerOptions)
    }

    // Add new marker on map click and remove old marker
    override fun onMapClick(latLng: LatLng) {
        marker?.remove()
        val markerOptions = MarkerOptions().position(latLng)
        marker = googleMap.addMarker(markerOptions)
    }

    fun addMarkerAtCurrentLocation(view: View) {
        //Remove existing marker
        marker?.remove()

        // Create a new marker at the current location & Move the Camera Center
        val latLng= LatLng(currentLocation.latitude, currentLocation.longitude)
        val markerOptions = MarkerOptions().position(latLng).title("Current Location")

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        marker = googleMap.addMarker(markerOptions)
    }
}
