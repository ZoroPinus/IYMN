package com.example.iymn.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iymn.Adapters.VegItemAdapter
import com.example.iymn.Models.VegItemViewModel
import com.example.iymn.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DonationHistoryActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: VegItemAdapter
    val dataList: MutableList<VegItemViewModel> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_history)

        // getting the recyclerview by its id
        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)

        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(this)

        adapter = VegItemAdapter(dataList)
        recyclerview.adapter = adapter

        // Fetch data from Firestore and update the adapter's data list
        fetchDataFromFirestore()
        // Set item click listener in the activity
        adapter.setOnItemClickListener(object : VegItemAdapter.OnItemClickListener {
            override fun onItemClick(documentId: String) {
                // Handle item click here, navigate to details activity passing document ID
                val intent =
                    Intent(this@DonationHistoryActivity, DonationDetailsActivity::class.java)
                intent.putExtra("donationRef", documentId)
                startActivity(intent)
            }
        })
    }
    private fun fetchDataFromFirestore() {
        // Get Firebase Authentication instance
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser?.uid
        db.collection("donations")
            .whereEqualTo("donorUID", currentUser)// Replace with your collection name
            .get()
            .addOnSuccessListener { documents ->
                val dataList: MutableList<VegItemViewModel> = mutableListOf()

                for (document in documents) {
                    val docId = document.id
                    val imagePath = document.getString("image") ?: ""
                    val itemName = document.getString("vegName") ?: "Default Item Name"

                    dataList.add(VegItemViewModel(docId, imagePath, itemName))
                }

                adapter.updateData(dataList) // Update adapter data with fetched items
            }
            .addOnFailureListener { e ->
                Log.w("DonationHistoryActivity", "Error fetching data", e)
                Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show()
            }
    }
}