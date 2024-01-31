package com.example.iymn.Fragments

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.iymn.R
import com.example.iymn.databinding.FragmentChooseLocationBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import java.io.IOException
import java.util.Locale

class ChooseLocationFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private var selectedLocation: LatLng? = null
    private lateinit var binding: FragmentChooseLocationBinding
    private var currentQuery: String = ""
    private var popupWindow: PopupWindow? = null
    private var placeName: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChooseLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Places.initialize(requireContext(), getString(R.string.google_maps_api_key))
        placesClient = Places.createClient(requireContext())
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.chooseLocMap) as? SupportMapFragment
                ?: SupportMapFragment.newInstance().also {
                    childFragmentManager.beginTransaction()
                        .replace(R.id.cropListFragmentContainer, it).commit()
                }
        mapFragment.getMapAsync(this)

        binding.btnRecenter.setOnClickListener {
            recenterMap()
        }

        binding.btnSelectLocation.setOnClickListener {
            val result = Bundle()
            result.putString("placeName", placeName)
            result.putString("latlng", selectedLocation.toString())
            result.putBoolean("CAME_FROM_CHOOSE_LOCATION", true)
            Log.e(TAG, selectedLocation.toString())
            parentFragmentManager.setFragmentResult("choosenLocation", result)
            parentFragmentManager.popBackStack()
        }

        // Set up text change listener for AutoCompleteTextView
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            private val handler = Handler(Looper.getMainLooper())
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Remove existing callbacks to avoid triggering search too soon
                handler.removeCallbacksAndMessages(null)

                // Schedule a search after a delay (e.g., 500 milliseconds) if the user stops typing
                handler.postDelayed({
                    if (s.toString() != currentQuery) {
                        currentQuery = s.toString()
                        showSuggestions(currentQuery)
                    }
                }, 1000)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearch.text.toString()
            getPlaceDetails(query)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Get the fused location provider client
        getUserLocation().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let { (userLocation, address) ->
                    // Location retrieval successful, move the camera to the user's location
                    selectedLocation = userLocation
                    // Add a marker at the user's location
                    addMarker(selectedLocation!!, "Your Location")

                    placeName=address
                }
            } else {
                // Handle the case when location retrieval fails or permission is not granted
                handleLocationError(task.exception)
            }
        }

        mMap.setOnMapClickListener { latLng ->
            selectedLocation = latLng

            // Remove the existing marker
            mMap.clear()

            addMarker(selectedLocation!!, "Your Location")
            placeName = getAddressFromLocation(selectedLocation!!)
            // Optionally, you can animate the camera to the selected location
        }
    }
    // Helper function to add a marker at a given location
    private fun addMarker(location: LatLng, title: String) {
        mMap.addMarker(MarkerOptions().position(location).title(title))
        mMap.animateCamera(CameraUpdateFactory.newLatLng(location))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }
    // Function to get the address from a given location (latitude and longitude)
    private fun getAddressFromLocation(location: LatLng): String? {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    return addresses[0].getAddressLine(0)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
    // Helper function to handle location retrieval errors
    private fun handleLocationError(exception: Exception?) {
        Log.e(TAG, "Error getting user location: $exception")
        // Handle the error as per your application's requirements
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
                        selectedLocation = currentLocation
                        addMarker(selectedLocation!!, "Your Location")
                    }
                }
        } else {
            // Handle the case when location permission is not granted
            // You may want to request permission again or show a message to the user
        }
    }

    private fun getUserLocation(): Task<Pair<LatLng, String?>> {
        val locationTask = TaskCompletionSource<Pair<LatLng, String?>>()

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Get the last known location and move the camera to that position
            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    mMap.clear()
                    if (location != null) {
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        val address = getAddressFromLocation(currentLocation)
                        locationTask.setResult(Pair(currentLocation, address))
                    } else {
                        locationTask.setResult(null)
                    }
                }
                .addOnFailureListener {
                    locationTask.setResult(null)
                }
        } else {
            // Handle the case when location permission is not granted
            // You may want to request permission again or show a message to the user
            locationTask.setResult(null)
        }

        return locationTask.task
    }
    private fun getPlaceDetails(searchQuery: String?) {
        if (searchQuery.isNullOrBlank()) {
            // Handle the case when the search query is null or blank
            return
        }

        val placeFields: List<Place.Field> = listOf(Place.Field.LAT_LNG, Place.Field.NAME)

        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(searchQuery)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                if (response.autocompletePredictions.isNotEmpty()) {
                    val placeId = response.autocompletePredictions[0].placeId

                    val placeDetailsRequest = FetchPlaceRequest.builder(placeId, placeFields)
                        .build()

                    placesClient.fetchPlace(placeDetailsRequest)
                        .addOnSuccessListener { placeResponse ->
                            val place = placeResponse.place
                            val latLng = place.latLng
                            placeName = place.name
                            selectedLocation = latLng

                            addMarker(latLng, "Your Location")
                        }
                        .addOnFailureListener { exception ->
                            // Handle errors
                            exception.printStackTrace()
                        }
                } else {
                    // Handle the case when no predictions are available
                    Log.e(TAG, "No predictions found for the given query: $searchQuery")
                }
            }
            .addOnFailureListener { exception ->
                // Handle errors
                exception.printStackTrace()
            }
    }
    private fun showSuggestions(query: String) {
        getUserLocation().addOnSuccessListener { userLocation ->
            // Build the FindAutocompletePredictionsRequest
            val requestBuilder = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)

            getUserLocation().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userLocation = task.result?.first

                    userLocation?.let {
                        val biasPadding = 0.1
                        val southWest = LatLng(it.latitude - biasPadding, it.longitude - biasPadding)
                        val northEast = LatLng(it.latitude + biasPadding, it.longitude + biasPadding)
                        val bounds = RectangularBounds.newInstance(southWest, northEast)
                        requestBuilder.setLocationBias(bounds)
                    }
                } else {
                    // Handle the case when getting user location fails
                    Log.e(TAG, "Error getting user location: ${task.exception}")
                }
            }

            val request = requestBuilder.build()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    val predictions = response.autocompletePredictions
                    val suggestionList = predictions.map { it.getFullText(null).toString() }

                    val dropDownView: View = LayoutInflater.from(requireContext())
                        .inflate(R.layout.dropdown_layout, null)
                    val listView = dropDownView.findViewById<ListView>(R.id.listView)

// Remove the listView from its parent if it has one
                    if (listView.parent != null) {
                        (listView.parent as? ViewGroup)?.removeView(listView)
                    }

                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        suggestionList
                    )

                    listView.adapter = adapter

// Set up item click listener for suggestions
                    listView.setOnItemClickListener { _, _, position, _ ->
                        val selectedPrediction = predictions[position]
                        val selectedItem = selectedPrediction.getFullText(null).toString()
                        binding.etSearch.setText(selectedItem)

                        // Pass the place ID to getPlaceDetails
//                        val placeId = selectedPrediction.placeId
                        // Dismiss the popup window
                        popupWindow?.dismiss()
                    }
                    // Dismiss any existing popup window
                    popupWindow?.dismiss()

                    // Set up the PopupWindow
                    popupWindow = PopupWindow(
                        listView,
                        binding.etSearch.width,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        true
                    )

                    // Show the PopupWindow below the AutoCompleteTextView
                    popupWindow?.showAsDropDown(binding.etSearch)

                }
                .addOnFailureListener { exception ->
                    // Handle errors
                    exception.printStackTrace()
                }
        }
    }
}