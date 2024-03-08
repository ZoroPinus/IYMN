package com.example.iymn.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.iymn.R
import com.example.iymn.databinding.FragmentMapDetailsBinding
import com.example.iymn.databinding.FragmentMapsBinding


class MapDetailsFragment : DialogFragment() {
    private lateinit var binding: FragmentMapDetailsBinding
    companion object {
        private const val ARG_LATITUDE = "latitude"
        private const val ARG_LONGITUDE = "longitude"

        fun newInstance(latitude: Double, longitude: Double): MapDetailsFragment {
            val fragment = MapDetailsFragment()
            val args = Bundle()
            args.putDouble(ARG_LATITUDE, latitude)
            args.putDouble(ARG_LONGITUDE, longitude)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMapDetailsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve location details from arguments
        val latitude = arguments?.getDouble(ARG_LATITUDE, 0.0) ?: 0.0
        val longitude = arguments?.getDouble(ARG_LONGITUDE, 0.0) ?: 0.0

        // Display location details
        binding.textViewLatitude.text = "Latitude: $latitude"
        binding.textViewLongitude.text = "Longitude: $longitude"

        // Set click listener for close button
        binding.buttonClose.setOnClickListener {
            dismiss() // Dismiss the dialog
        }
    }
}