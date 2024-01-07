package com.example.iymn.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.example.iymn.R
import com.google.firebase.firestore.FirebaseFirestore

class AddCropFragment : Fragment() {
    private lateinit var etCropName: EditText
    private lateinit var btnAddCropToCloud: Button
    private val db = FirebaseFirestore.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_crop, container, false)

        // Initialize views
        etCropName = view.findViewById(R.id.etCropName)
        btnAddCropToCloud = view.findViewById(R.id.btnAddCropToCloud)

        // Handle button click to add crop to Firestore
        btnAddCropToCloud.setOnClickListener {
            val cropName = etCropName.text.toString().trim()

            // Check if crop name is not empty
            if (cropName.isNotEmpty()) {
                addCropToFirestore(cropName)
            } else {
                // Handle empty crop name case
                // You can show an error message or handle it as per your app logic
            }
        }

        return view
    }

    private fun addCropToFirestore(cropName: String) {
        // Create a new document with a generated ID
        val cropDocument = db.collection("crops").document()

        // Create a map with the crop name
        val cropData = hashMapOf(
            "name" to cropName
        )

        // Set the data to Firestore
        cropDocument.set(cropData)
            .addOnSuccessListener {
                showSuccessDialog()
                etCropName.setText("")
            }
            .addOnFailureListener { e ->
                // Handle failure, log the error, show an error message, etc.
            }
    }

    private fun showSuccessDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Crop Added Successfully")
            .setMessage("Your crop has been added to the crop list.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

}