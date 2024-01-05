package com.example.iymn.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iymn.Adapters.CropItemAdapter
import com.example.iymn.R

class CropFragment : Fragment() {
    private lateinit var cropSelectedListener: (String) -> Unit
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crop, container, false)

        // Fetch crop items from Firestore and populate the RecyclerView
        val cropList = listOf("Wheat", "Corn", "Rice", "Barley", "Soybeans")

        val recyclerView: RecyclerView = view.findViewById(R.id.cropRecyclerView)
        val adapter = CropItemAdapter(cropList) { selectedCrop ->
            cropSelectedListener.invoke(selectedCrop)
            fragmentManager?.popBackStack()
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return view
    }

    fun setCropSelectedListener(listener: (String) -> Unit) {
        cropSelectedListener = listener
    }

}