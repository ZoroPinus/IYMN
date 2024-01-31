package com.example.iymn.Models

import com.google.android.gms.maps.model.LatLng

data class LocationData(
    val latLng: LatLng,
    val placeName: String?
)
