package com.example.iymn.Fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.iymn.R
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment(), OnMapReadyCallback  {
    private lateinit var mMap: GoogleMap
    private var selectedLocation: LatLng? = null
    private var callback: OnLocationSelectedListener? = null
    interface OnLocationSelectedListener {
        fun onLocationSelected(location: LatLng)
    }

    fun setOnLocationSelectedListener(listener: OnLocationSelectedListener) {
        this.callback = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
                ?: SupportMapFragment.newInstance().also {
                    childFragmentManager.beginTransaction().replace(R.id.mapsFragmentContainer, it).commit()
                }
        mapFragment.getMapAsync(this)

        val selectLocationButton = view.findViewById<Button>(R.id.btnSelectLocation)
        selectLocationButton.setOnClickListener {
//            if (callback != null && selectedLocation != null) {
//                callback!!.onLocationSelected(selectedLocation!!)
//            }
            recenterMap()
        }

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Get the fused location provider client
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Check if the location permission has been granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Get the last known location
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Place a marker on the current location
                    location?.let {
                        val currentLocation = LatLng(it.latitude, it.longitude)
                        mMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                    }
                }
        } else {
            // Request location permission
            val LOCATION_REQUEST_CODE = 1
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
        }

        // Set up a map click listener to get the selected location and update the marker
        mMap.setOnMapClickListener { latLng ->
            selectedLocation = latLng

            // Remove the existing marker
            mMap.clear()

            // Add a new marker at the selected location
            mMap.addMarker(MarkerOptions().position(selectedLocation!!).title("Your New Marker Title"))

            // Optionally, you can animate the camera to the selected location
            mMap.animateCamera(CameraUpdateFactory.newLatLng(selectedLocation!!))
        }
    }
    private fun recenterMap() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Get the last known location and move the camera to that position
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    mMap.clear()
                    if (location != null) {
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        mMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation))
                    }
                }
        } else {
            // Handle the case when location permission is not granted
            // You may want to request permission again or show a message to the user
        }
    }
}