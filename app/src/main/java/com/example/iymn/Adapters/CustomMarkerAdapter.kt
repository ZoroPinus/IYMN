package com.example.iymn.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.iymn.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomMarkerAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoWindow(marker: Marker): View? {
        return null // Return null to use the default info window
    }

    override fun getInfoContents(marker: Marker): View {
//        layout for donor
        val snippetParts = marker.snippet?.split("|")
        val markerType = snippetParts?.get(0)
        val view = when (markerType) {
            "donor" -> LayoutInflater.from(context).inflate(R.layout.custom_marker_info_window, null)
            "ngo/admin" -> LayoutInflater.from(context).inflate(R.layout.custom_marker_info_window_ngo, null)
            else -> LayoutInflater.from(context).inflate(R.layout.custom_marker_info_window_default, null) // fallback layout
        }

        val imageUrl = snippetParts?.getOrNull(1)
        when (markerType) {
            "donor" -> {
                val placeNameTextView = view.findViewById<TextView>(R.id.placeNameTextView)
                val placeDetailsTextView = view.findViewById<TextView>(R.id.placeDetailsTextView)
                val imageView = view.findViewById<ImageView>(R.id.imageMarker)

                val placeDetails = snippetParts[2]
                // Set marker data to the views
                placeNameTextView.text = marker.title
                placeDetailsTextView.text = placeDetails

                imageUrl?.let {
                    // Load image using your preferred image loading library (e.g., Picasso, Glide)
                    // Example using Glide:
                    Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_folder) // Placeholder image while loading
                        .error(R.drawable.ic_insert_img) // Image to show if loading fails
                        .into(imageView)
                }
            }
            "ngo/admin" -> {
                val placeNameTextView = view.findViewById<TextView>(R.id.placeNameTextView)
                val markerProduct = view.findViewById<TextView>(R.id.markerProduct)
                val markerDonateDate = view.findViewById<TextView>(R.id.markerDonateDate)
                val markerQuantity = view.findViewById<TextView>(R.id.markerQuantity)
                val markerDescription = view.findViewById<TextView>(R.id.markerDescription)
                val imageView = view.findViewById<ImageView>(R.id.imageMarker)

                // Set marker data to the views
                placeNameTextView.text = marker.title
                markerDonateDate.text = snippetParts[2]
                markerQuantity.text = snippetParts[3]
                markerDescription.text = snippetParts[4]
                markerProduct.text = snippetParts[5]

                imageUrl?.let {
                    // Load image using your preferred image loading library (e.g., Picasso, Glide)
                    // Example using Glide:
                    Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_folder) // Placeholder image while loading
                        .error(R.drawable.ic_insert_img) // Image to show if loading fails
                        .into(imageView)
                }
            }
            // Add additional cases for other layouts if needed
        }


        return view
    }
}
