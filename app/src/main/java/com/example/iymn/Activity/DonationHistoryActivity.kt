package com.example.iymn.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iymn.Adapters.VegItemAdapter
import com.example.iymn.Models.VegItemViewModel
import com.example.iymn.R
import com.example.iymn.databinding.ActivityDonationHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class DonationHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDonationHistoryBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: VegItemAdapter
    val dataList: MutableList<VegItemViewModel> = ArrayList()
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonationHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val headerIcon: ImageView = findViewById(R.id.customHeaderIcon)
        val headerText: TextView = findViewById(R.id.customHeaderText)

        headerIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        headerText.setText("Donation History")


        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        // getting the recyclerview by its id
        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)

        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(this)

        adapter = VegItemAdapter(dataList)
        recyclerview.adapter = adapter

        // Fetch data from Firestore and update the adapter's data list
        fetchDataBasedOnUserType()
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

    private fun fetchDataBasedOnUserType() {
        if (currentUser == null) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        } else {
            val uid = currentUser!!.uid
            db.collection("users").document(uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    val accountType = documentSnapshot.getString("accountType")
                    when (accountType) {
                        "NGO", "Admin" -> fetchForAdminAndNgo()
                        else -> fetchForDonor()
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("DonationHistoryActivity", "Error fetching user data", e)
                    Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchForDonor() {
        db.collection("donations")
            .whereEqualTo("donorUID", currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                val dataList: MutableList<VegItemViewModel> = mutableListOf()
                for (document in documents) {
                    val docId = document.id
                    val imagePath = document.getString("image") ?: ""
                    val date = document.getString("donateDate")?.let { formatDate(it) }
                    val vegName = document.getString("vegName") ?: "Default Item Name"
                    val status = document.getString("status") ?: "Default Item Name"
                    val recipient = document.getString("recipient") ?: "Default Item Name"
                    val fetchedQuantity = document.getString("quantity")
                    val fetchedQuantityType = document.getString("quantityType")
                    val formattedQuantity = getString(
                        R.string.formatted_quantity,
                        fetchedQuantity,
                        fetchedQuantityType
                    )
                    val itemName = vegName.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
                    dataList.add(VegItemViewModel(docId, imagePath, itemName,formattedQuantity,date, recipient, status))
                }
                adapter.updateData(dataList)
            }
            .addOnFailureListener { e ->
                handleFetchDataFailure(e)
            }
    }

    private fun fetchForAdminAndNgo() {
        // Example: Fetching only donations with status "approved" for Admin and NGO
        db.collection("donations")
            .get()
            .addOnSuccessListener { documents ->
                val dataList: MutableList<VegItemViewModel> = mutableListOf()
                for (document in documents) {
                    val docId = document.id
                    val imagePath = document.getString("image") ?: ""
                    val date = document.getString("donateDate")?.let { formatDate(it) }
                    val vegName = document.getString("vegName") ?: "Default Item Name"
                    val status = document.getString("status") ?: "Default Item Name"
                    val recipient = document.getString("recipient") ?: "Default Item Name"
                    val fetchedQuantity = document.getString("quantity")
                    val fetchedQuantityType = document.getString("quantityType")
                    val formattedQuantity = getString(
                        R.string.formatted_quantity,
                        fetchedQuantity,
                        fetchedQuantityType
                    )
                    val itemName = vegName.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
                    dataList.add(VegItemViewModel(docId, imagePath, itemName,formattedQuantity,date, recipient, status))
                }
                adapter.updateData(dataList)
            }
            .addOnFailureListener { e ->
                handleFetchDataFailure(e)
            }
    }
    private fun handleFetchDataFailure(e: Exception) {
        Log.w("DonationHistoryActivity", "Error fetching data", e)
        Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show()
    }
    private fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMMM d, yyyy, h:mm a", Locale.getDefault())

        val date = inputFormat.parse(dateString)
        return outputFormat.format(date)
    }

}