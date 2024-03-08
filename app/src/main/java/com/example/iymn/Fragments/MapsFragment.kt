package com.example.iymn.Fragments

import android.Manifest
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.iymn.Activity.DashboardActivity
import com.example.iymn.Adapters.CustomMarkerAdapter
import com.example.iymn.R
import com.example.iymn.databinding.FragmentMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MapsFragment : Fragment(), OnMapReadyCallback  {
    private lateinit var binding: FragmentMapsBinding
    private lateinit var mMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private var selectedLocation: LatLng? = null
    private var popupWindow: PopupWindow? = null
    private var currentQuery: String = ""
    private var placeName: String? = ""
    private val donationLocations = mutableListOf<GeoPoint>()
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var customMarkerAdapter: CustomMarkerAdapter
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
                ?: SupportMapFragment.newInstance().also {
                    childFragmentManager.beginTransaction().replace(R.id.mapsFragmentContainer, it).commit()
                }
        mapFragment.getMapAsync(this)
        Places.initialize(requireContext(), getString(R.string.google_maps_api_key))
        placesClient = Places.createClient(requireContext())

        binding.btnRecenter.setOnClickListener {
            recenterMap()
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

//        binding.btnTest.setOnClickListener {
//            val latitude = 37.7749
//            val longitude = -122.4194
//            val customModalFragment = MapDetailsFragment.newInstance(latitude, longitude)
//            customModalFragment.show(childFragmentManager, "CustomModalFragment")
//        }
        customMarkerAdapter = CustomMarkerAdapter(requireContext())

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setInfoWindowAdapter(customMarkerAdapter)
        // Check for location permission
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Request location updates
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        // Move camera to current location
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        mMap.addMarker(MarkerOptions().position(currentLatLng).title("Current Location"))

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13f))

                        if(DashboardActivity.userType == "Donor"){
                            loadDonorFoodMap()
                        }else if (DashboardActivity.userType == "NGO"){
                            getDonationLocations()
                        }else {
                            recenterMap()
                        }
                    }
                }
        } else {
            // Request location permission
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, request location updates again
                    onMapReady(mMap)
                } else {
                    // Permission denied, handle accordingly
                }
            }
        }
    }
    // Function to get NGO partner locations from Firestore and place markers on the map
    private fun loadDonorFoodMap() {
        val db = FirebaseFirestore.getInstance()
        val ngoPartnersCollection = db.collection("ngoPartners") // Change to your actual Firestore collection name

        ngoPartnersCollection.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Access the address field, you may need to change this based on your data model
                    val address = document.getString("address") ?: "Unknown Address"
                    val name = document.getString("ngoName") ?: "Unknown Address"
                    val image = document.getString("image") ?: "Unknown Address"
                    val contact = document.getString("contact") ?: "Unknown Address"

                    // Get LatLng from address using Geocoder
                    showPlaceDetails(address, name, image, contact)
                }
            }
            .addOnFailureListener { exception ->
                // Handle errors during Firestore data retrieval
                Log.e(TAG, "Error getting NGO partner locations: ", exception)
            }
    }

    private fun getDonationLocations() {
        val firestore = FirebaseFirestore.getInstance()

        // Example: Assuming "donations" is your collection name
        firestore.collection("donations")
            .whereEqualTo("recipient", NGODashboardFragment.ngoOrg)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val location = document.getGeoPoint("latlng")
                    val donation = document.getString("vegName")?: "Unknown Address"
                    val image = document.getString("image")?: "Unknown Address"
                    val description = document.getString("description")?: "Unknown Address"
                    val date = document.getString("donateDate")?: "Unknown Address"
                    val formattedDate = formatDate(date)
                    val fetchedQuantity = document.getString("quantity")
                    val fetchedQuantityType = document.getString("quantityType")
                    val quantity = getString(
                        R.string.formatted_quantity,
                        fetchedQuantity,
                        fetchedQuantityType
                    )
                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        addMarkerNgo(latLng, donation, image, formattedDate, quantity, description)

                    }
                }
            }
            .addOnFailureListener { exception ->
                // Handle errors
            }
    }
    private fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMMM d, yyyy, h:mm a", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        return outputFormat.format(date)
    }

    private fun showPlaceDetails(searchQuery: String?, ngoName: String, image: String, contact: String) {
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
                    for (prediction in response.autocompletePredictions) {
                        val placeId = prediction.placeId

                        val placeDetailsRequest = FetchPlaceRequest.builder(placeId, placeFields)
                            .build()

                        placesClient.fetchPlace(placeDetailsRequest)
                            .addOnSuccessListener { placeResponse ->
                                val place = placeResponse.place
                                val latLng = place.latLng
                                placeName = place.name

                                // Check if it's the user's location or NGO partner's location
                                addMarkerDonor(latLng, ngoName, image, contact)


                            }
                            .addOnFailureListener { exception ->
                                // Handle errors
                                exception.printStackTrace()
                            }
                    }
                } else {
                    // Handle the case when no predictions are available
                    Log.e(ContentValues.TAG, "No predictions found for the given query: $searchQuery")
                }
            }
            .addOnFailureListener { exception ->
                // Handle errors
                exception.printStackTrace()
            }
    }
    private fun recenterMap() {
        Log.e(TAG, "Error getting NGO partner locations: ")
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Get the last known location and move the camera to that position
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    mMap.clear()
                    if (location != null) {
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        val markerOptions = MarkerOptions()
                            .position(currentLocation)
                            .title("Current Location")
                        mMap.addMarker(markerOptions)
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation))
                    }
                }
        } else {
            Log.e(TAG, "Mapsfragmet")
        }
    }
    private fun addMarkerDonor(location: LatLng, title: String, image:String, contact: String) {
        val markerOptions = MarkerOptions()
            .position(location)
            .title(title)
            .snippet("donor|$image|$contact")
        mMap.addMarker(markerOptions)
    }

    private fun addMarkerNgo(location: LatLng, title: String, image:String, donorDate: String,quantity: String, description: String) {
        val markerOptions = MarkerOptions()
            .position(location)
            .title(title)
            .snippet("ngo/admin|$image|$donorDate|$quantity|$description")
        mMap.addMarker(markerOptions)
    }
    private fun addMarker(location: LatLng, title: String) {
        val markerOptions = MarkerOptions()
            .position(location)
            .title(title)
        mMap.addMarker(markerOptions)
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
                    Log.e(ContentValues.TAG, "No predictions found for the given query: $searchQuery")
                }
            }
            .addOnFailureListener { exception ->
                // Handle errors
                exception.printStackTrace()
            }
    }
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
                    Log.e(ContentValues.TAG, "Error getting user location: ${task.exception}")
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