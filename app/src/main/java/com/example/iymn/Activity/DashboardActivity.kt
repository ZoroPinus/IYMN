package com.example.iymn.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.iymn.R
import com.example.iymn.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {
      private lateinit var binding: ActivityDashboardBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid // Get current user's UID
            fetchUserData(userId) // Fetch user data using the UID
        } else {
            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }



        // Access your views through the binding object
        binding.btnDonate.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    DonationFormActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
        binding.btnNGOPartners.setOnClickListener {
            Toast.makeText(this, "NGO Partners", Toast.LENGTH_SHORT).show()
        }
        binding.btnInbox.setOnClickListener {
            Toast.makeText(this, "Inbox", Toast.LENGTH_SHORT).show()
        }
        binding.btnFoodMap.setOnClickListener {
            Toast.makeText(this, "Food Map", Toast.LENGTH_SHORT).show()
        }
        binding.btnAddFeedback.setOnClickListener {
            Toast.makeText(this, "Add Feedback", Toast.LENGTH_SHORT).show()
        }
        binding.btnTopDonors.setOnClickListener {
            Toast.makeText(this, "Top Donors", Toast.LENGTH_SHORT).show()
        }
        binding.btnAboutUs.setOnClickListener {
            Toast.makeText(this, "About Us", Toast.LENGTH_SHORT).show()
        }
        binding.btnDonationHistory.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    DonationHistoryActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
        binding.btnNotifications.setOnClickListener {
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show()
        }

        binding.btnProfile.setOnClickListener {
            logout()
        }
    }

    private fun fetchUserData(userId: String) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val data = documentSnapshot.data
                if (data != null) {
                    val name = data["name"] as String
                    val accountType = data["accountType"] as String
                    // Assuming you have TextViews to display this data
                    val tvUsername: TextView = findViewById(R.id.tvWelcomeUser)
                    val tvAccountType: TextView = findViewById(R.id.tvAccType)
                    tvUsername.text = getString(R.string.welcome_user, name)
                    tvAccountType.text = getString(R.string.account_type, accountType)

                } else {
                    Log.d("DashboardActivity", "No such document")
                }
            }
            .addOnFailureListener { e ->
                Log.w("DashboardActivity", "Error fetching user data", e)
                Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
            }
    }
    private fun logout() {
        auth.signOut() // Sign out the current user
        // Redirect to the login or sign-in activity
        startActivity(Intent(this, LoginActivity::class.java))
        finish() // Close the DashboardActivity
    }
}