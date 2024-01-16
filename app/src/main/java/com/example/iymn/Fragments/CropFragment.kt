package com.example.iymn.Fragments

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class CropFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var cropsAdapter: CropItemAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crop, container, false)
        recyclerView = view.findViewById(R.id.cropRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val btnAddCrop: FloatingActionButton = view.findViewById(R.id.btnAddCrop)

        // Initialize the adapter
        cropsAdapter = CropItemAdapter()

        // Set item click listener
        cropsAdapter.setOnEditButtonClickListener { clickedCrop ->
            adapterOnClick(clickedCrop)
        }

        cropsAdapter.setOnDeleteButtonClickListener { clickedCrop ->
            deleteCropFromFirestore(clickedCrop)
        }

        recyclerView.adapter = cropsAdapter

        fetchCropsFromFirestore()

        btnAddCrop.setOnClickListener {
            // Replace 'YourNewFragment()' with the fragment you want to open
            val newFragment = AddCropFragment()

            // Begin fragment transaction
            val fragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()

            // Replace the current fragment with the new fragment
            fragmentTransaction.replace(R.id.fragmentContainer, newFragment)

            // Optional: Add to back stack for handling back navigation
            fragmentTransaction.addToBackStack(null)

            // Commit the transaction
            fragmentTransaction.commit()
        }

        return view

    }

    private fun fetchCropsFromFirestore() {
        // Reference to the "crops" collection
        db.collection("crops")
            .get()
            .addOnSuccessListener { result ->
                val cropList = mutableListOf<CropItemViewModel>()

                for (document in result) {
                    // Extract the data and create CropItemViewModel objects
                    val id = document.id
                    val name = document.getString("name") ?: ""
                    val crop = CropItemViewModel(id, name)
                    cropList.add(crop)
                }

                // Pass the retrieved cropList to the adapter
                cropsAdapter.submitList(cropList)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching document", e)
            }
    }

    private fun adapterOnClick(crop: CropItemViewModel) {
        val result = Bundle()
        result.putString("selectedCropName", crop.cropName)
        parentFragmentManager.setFragmentResult("cropSelection", result)
        parentFragmentManager.popBackStack()
    }

    private fun deleteCropFromFirestore(crop: CropItemViewModel) {
        // Implement the code to delete the item from Firestore
        // For example, assuming you have a Firestore collection called "crops":
        val collectionReference = FirebaseFirestore.getInstance().collection("crops")
        collectionReference.document(crop.id).delete()
            .addOnSuccessListener {
                fetchCropsFromFirestore()
            }
            .addOnFailureListener { e ->
                // Handle failure
                Log.e(TAG, "Error deleting document", e)
            }
    }

}