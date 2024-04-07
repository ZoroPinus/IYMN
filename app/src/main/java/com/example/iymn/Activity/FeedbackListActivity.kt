package com.example.iymn.Activity

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iymn.Adapters.FeedbacksListAdapter
import com.example.iymn.Adapters.VegItemAdapter
import com.example.iymn.Models.FeedbackListModel
import com.example.iymn.Models.VegItemViewModel
import com.example.iymn.R
import com.example.iymn.databinding.ActivityDonationHistoryBinding
import com.example.iymn.databinding.ActivityFeedbackListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class FeedbackListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeedbackListBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: FeedbacksListAdapter
    val dataList: MutableList<FeedbackListModel> = ArrayList()
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val headerIcon: ImageView = findViewById(R.id.customHeaderIcon)
        val headerText: TextView = findViewById(R.id.customHeaderText)

        headerIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        headerText.setText("Feedback List")


        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        // getting the recyclerview by its id
        val recyclerview = findViewById<RecyclerView>(R.id.feedbackListrecyclerview)

        adapter = FeedbacksListAdapter(dataList)
        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.adapter = adapter
        fetchFeedbacks()
//        adapter.setOnItemClickListener(object : FeedbacksListAdapter.OnItemClickListener {
//            override fun onItemClick(documentId: String) {
//                // Handle item click here, navigate to details activity passing document ID
//                val intent =
//                    Intent(this@FeedbackListActivity, DonationDetailsActivity::class.java)
//                intent.putExtra("donationRef", documentId)
//                startActivity(intent)
//            }
//        })
    }

    private fun fetchFeedbacks() {
        db.collection("feedbacks")
            .get()
            .addOnSuccessListener { documents ->
                val dataList: MutableList<FeedbackListModel> = mutableListOf()
                for (document in documents) {
                    val userId = document.getString("userId") ?: ""
                    val date = document.getString("date")?.let { formatDate(it) }
                    val newDate = date?.split(",")?.get(0)?.trim()
                    val textFeedBack = document.getString("textFeedback") ?: "Default Item Name"
                    val ratingDouble  = document.getDouble("rating") ?: 0.0
                    val rating = ratingDouble.toString()
                    // Fetch user's name and image using userId (assuming you have a 'users' collection)
                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { userDoc ->
                            val userName = userDoc.getString("name") ?: "Unknown"
                            val userImage = userDoc.getString("profileImageUrl") ?: "" // Assuming image is a URL string
                            val feedbackModel = FeedbackListModel(userImage, userName, rating, textFeedBack, newDate)
                            dataList.add(feedbackModel)
                            adapter.updateData(dataList)
                        }
                        .addOnFailureListener { e ->
                            // Handle failure to fetch user details
                            Log.e(TAG, "Error fetching user details for userId: $userId", e)
                        }
                }
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