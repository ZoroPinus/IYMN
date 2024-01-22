package com.example.iymn.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iymn.Adapters.TableItemAdapterParent
import com.example.iymn.Models.TableItem
import com.example.iymn.R
import com.google.firebase.firestore.FirebaseFirestore

class ReportsActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()

        // Fetch donation data
        fetchDonationData()
    }
    private fun fetchDonationData() {
        // Assume "donations" is the name of your Firestore collection
        db.collection("donations")
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val donations = result.documents.map { document ->
                        val donor = document.getString("donorUID") ?: ""
                        val cropName = document.getString("vegName") ?: ""
                        val ngoPartner = document.getString("recipient") ?: ""
                        val address = document.getString("address") ?: ""
                        val quantity = document.getString("quantity") ?: ""
                        val date = document.getString("date") ?: ""
                        val status = document.getString("status") ?: ""

                        TableItem(donor, cropName,ngoPartner, address, quantity, date, status)
                    }

                    val recyclerView = findViewById<RecyclerView>(R.id.tableRecyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(this)
                    recyclerView.adapter = TableItemAdapterParent(donations)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error getting documents: ", exception)
            }
    }

}