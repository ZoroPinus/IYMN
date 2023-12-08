package com.example.iymn.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.iymn.R
import com.example.iymn.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {
      private lateinit var binding: ActivityDashboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            Toast.makeText(this, "Donation History", Toast.LENGTH_SHORT).show()
        }
        binding.btnNotifications.setOnClickListener {
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show()
        }
    }
}