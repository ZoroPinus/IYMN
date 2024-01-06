package com.example.iymn.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iymn.Adapters.CropItemAdapter
import com.example.iymn.Adapters.VegItemAdapter
import com.example.iymn.Models.CropItemViewModel
import com.example.iymn.Models.VegItemViewModel
import com.example.iymn.R
class CropFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crop, container, false)
        val cropsAdapter = CropItemAdapter { crop -> adapterOnClick(crop) }
        // Fetch crop items from Firestore and populate the RecyclerView
        val cropList = listOf(
            CropItemViewModel("1", "Wheat"),
            CropItemViewModel("2", "Corn"),
            CropItemViewModel("3", "Rice"),
            CropItemViewModel("4", "Barley"),
            CropItemViewModel("5", "Soybeans")
        )

        val recyclerView: RecyclerView = view.findViewById(R.id.cropRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Set the adapter to the RecyclerView
        recyclerView.adapter = cropsAdapter

        // Pass your cropList to the adapter to display the data
        cropsAdapter.submitList(cropList)

        return view

    }

    /* Opens FlowerDetailActivity when RecyclerView item is clicked. */
    private fun adapterOnClick(crop: CropItemViewModel) {
        val intent = Intent(context, CropFragment()::class.java)
        intent.putExtra("cropId", crop.id)
        startActivity(intent)
    }

}