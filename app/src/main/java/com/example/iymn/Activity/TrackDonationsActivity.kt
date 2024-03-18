package com.example.iymn.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iymn.Adapters.TableItemAdapterParent
import com.example.iymn.Models.TableItem
import com.example.iymn.R
import com.example.iymn.databinding.ActivityTrackDonationsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class TrackDonationsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTrackDonationsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var statusValue : String
    private var activeButton: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackDonationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val headerIcon: ImageView = findViewById(R.id.customHeaderIcon)
        val headerText: TextView = findViewById(R.id.customHeaderText)

        headerIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        headerText.setText("Track Donations")

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()

        setActiveButton(binding.btnToPending)
        fetchDonationData("PENDING")

        binding.btnToPending.setOnClickListener {
            setActiveButton(binding.btnToPending)
            fetchDonationData("PENDING")
        }

        binding.btnToAccepted.setOnClickListener {
            setActiveButton(binding.btnToAccepted)
            fetchDonationData("ACCEPTED")
        }

        binding.btnToRejected.setOnClickListener {
            setActiveButton(binding.btnToRejected)
            fetchDonationData("REJECTED")
        }

    }

    private fun setActiveButton(button: TextView) {
        activeButton?.let {
            it.setTextColor(ContextCompat.getColor(this, R.color.black))
            it.setBackgroundResource(R.drawable.custom_btn_outlined)
            it.isSelected = false
        }
        activeButton = button
        button.setTextColor(ContextCompat.getColor(this, R.color.white))
        button.setBackgroundResource(R.drawable.custom_btn_outlined_selected)
        button.isSelected = true
    }
    private fun fetchDonationData(status: String) {
        // Assume "donations" is the name of your Firestore collection
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { userDocument ->
                    val ngoOrg = userDocument.getString("NgoOrg") ?: ""

                    // Fetch donations where the status matches and the organization matches the user's organization
                    db.collection("donations")
                        .whereEqualTo("status", status)
                        .whereEqualTo("recipient", ngoOrg)
                        .get()
                        .addOnSuccessListener { result ->
                            if (!result.isEmpty) {
                                val donations: MutableList<TableItem> = mutableListOf()
                                val dateFormat = SimpleDateFormat("MM-dd-yyyy ", Locale.getDefault())
                                for (document in result.documents) {
                                    val id = document.id
                                    val donorUID = document.getString("donorUID") ?: ""
                                    val cropName = document.getString("vegName") ?: ""
                                    val ngoPartner = document.getString("recipient") ?: ""
                                    val address = document.getString("address") ?: ""
                                    val quantity = document.getString("quantity") ?: ""
                                    val quantityType = document.getString("quantityType") ?: ""
                                    val timestamp = document.getTimestamp("donateDate")
                                    val date = timestamp?.toDate()?.let { dateFormat.format(it) } ?: "Invalid Date"
                                    val status = document.getString("status") ?: ""
                                    val finalQuantity =getString(
                                        R.string.formatted_quantity,
                                        quantity,
                                        quantityType
                                    )

                                    // Now, fetch the name of the donorUID from the "users" collection
                                    fetchDonorName(donorUID) { donorName ->
                                        val tableItem = TableItem(id, donorName, cropName, ngoPartner, address, finalQuantity, date, status)
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
                .addOnFailureListener { exception ->
                    Log.e("Firebase", "Error getting user document: ", exception)
                }
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