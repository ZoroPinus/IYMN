package com.example.iymn.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iymn.Adapters.CropItemAdapter
import com.example.iymn.Adapters.NGOItemAdapter
import com.example.iymn.Fragments.AddNGOPartnerFragment
import com.example.iymn.Fragments.CropFragment
import com.example.iymn.Models.CropItemViewModel
import com.example.iymn.Models.NGOItemViewModel
import com.example.iymn.R
import com.example.iymn.databinding.ActivityNgopartnersBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NGOPartnersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNgopartnersBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var NGOItemAdapter: NGOItemAdapter
    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNgopartnersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = findViewById(R.id.NGOrecyclerview)
        NGOItemAdapter = NGOItemAdapter()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = NGOItemAdapter

        fetchDataFromFirestore()
        checkUserAccountType()

        binding.btnAddNgoPartner.setOnClickListener {
            binding.NGOfragmentContainer.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE // Hide RecyclerView

            val fragment = AddNGOPartnerFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.NGOfragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
    override fun onBackPressed() {
        if (binding.NGOfragmentContainer.visibility == View.VISIBLE) {
            binding.NGOfragmentContainer.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE // Show RecyclerView
        } else {
            super.onBackPressed()
        }
    }

    private fun fetchDataFromFirestore() {
        val collectionRef = db.collection("ngoPartners")

        collectionRef.get()
            .addOnSuccessListener { documents ->
                val itemList = ArrayList<NGOItemViewModel>()

                for (document in documents) {
                    val id = document.id
                    val ngoName = document.getString("ngoName") ?: ""
                    val image = document.getString("image") ?: ""
                    val contact = document.getString("contact") ?: ""
                    val address = document.getString("address") ?: ""
                    val ngo = NGOItemViewModel(id, image, ngoName, contact, address )
                    itemList.add(ngo)
                }

                if (itemList.isEmpty()) {
                    // Show default UI or message when there are no results
                    showDefaultUI()
                } else {
                    // Update the adapter with fetched data

                    NGOItemAdapter.submitList(itemList)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("NGOPartnersActivity", "Error getting documents: ", exception)
                // Handle failure scenario, show error message, etc.
            }
    }
    private fun showDefaultUI() {
        // Inflate the empty state layout
        // Hide the RecyclerView
        recyclerView.visibility = View.GONE

        val emptyStateView = layoutInflater.inflate(R.layout.empty_state_layout, null)
        // Add the empty state layout to the parent view
        val parentLayout = findViewById<ViewGroup>(android.R.id.content)
        parentLayout.addView(emptyStateView)
    }

    private fun checkUserAccountType() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = user.uid

            val userRef = db.collection("users").document(userId)
            userRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val accountType = documentSnapshot.getString("accountType")
                        if (accountType != null && accountType == "Admin") {
                            // User is an admin, show the button
                            binding.btnAddNgoPartner.visibility = View.VISIBLE
                        } else {
                            // User is not an admin, hide the button
                            binding.btnAddNgoPartner.visibility = View.GONE
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("NGOPartnersActivity", "Error fetching user document: ", exception)
                }
        }
    }
}