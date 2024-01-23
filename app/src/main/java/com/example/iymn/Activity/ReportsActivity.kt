package com.example.iymn.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iymn.Adapters.TableItemAdapterParent
import com.example.iymn.Models.TableItem
import com.example.iymn.R
import com.example.iymn.databinding.ActivityReportsBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class ReportsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var statusValue : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val headerIcon: ImageView = findViewById(R.id.customHeaderIcon)
        val headerText: TextView = findViewById(R.id.customHeaderText)

        headerIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        headerText.setText("Reports")

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()


        fetchDonationData("PENDING")

        binding.btnToPending.setOnClickListener {
            fetchDonationData("PENDING")
        }

        binding.btnToAccepted.setOnClickListener {
            fetchDonationData("ACCEPTED")
        }

        binding.btnToRejected.setOnClickListener {
            fetchDonationData("REJECTED")
        }

    }
    private fun fetchDonationData(status: String) {
        // Assume "donations" is the name of your Firestore collection
        db.collection("donations")
            .whereEqualTo("status", status)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val donations: MutableList<TableItem> = mutableListOf()

                    for (document in result.documents) {
                        val id = document.id
                        val donorUID = document.getString("donorUID") ?: ""
                        val cropName = document.getString("vegName") ?: ""
                        val ngoPartner = document.getString("recipient") ?: ""
                        val address = document.getString("address") ?: ""
                        val quantity = document.getString("quantity") ?: ""
                        val quantityType = document.getString("quantityType") ?: ""
                        val date = document.getString("donateDate") ?: ""
                        val status = document.getString("status") ?: ""
                        val finalQuantity =getString(
                            R.string.formatted_quantity,
                            quantity,
                            quantityType
                        )
                        val formattedDate = formatDate(date)

                        // Now, fetch the name of the donorUID from the "users" collection
                        fetchDonorName(donorUID) { donorName ->
                            val tableItem = TableItem(id, donorName, cropName, ngoPartner, address, finalQuantity, formattedDate, status)
                            donations.add(tableItem)

                            // If all data is fetched, set up the RecyclerView adapter
                            if (donations.size == result.size()) {
                                val recyclerView = findViewById<RecyclerView>(R.id.tableRecyclerViewPending)
                                recyclerView.layoutManager = LinearLayoutManager(this)
                                recyclerView.adapter = TableItemAdapterParent(donations)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error getting documents: ", exception)
            }
    }

    private fun fetchDonorName(donorUID: String, callback: (String) -> Unit) {
        // Fetch the name of the donorUID from the "users" collection
        db.collection("users")
            .document(donorUID)
            .get()
            .addOnSuccessListener { userDocument ->
                val donorName = userDocument.getString("name") ?: ""
                val email = userDocument.getString("email") ?: ""

                if(donorName.isBlank()){
                    callback.invoke(email)
                }else{
                    callback.invoke(donorName)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error getting user document: ", exception)
                callback.invoke("") // Provide a default value or handle the failure accordingly
            }
    }

    private fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMMM d, yyyy, h:mm a", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        return outputFormat.format(date)
    }

}