package com.example.iymn

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class UserOptFragment : Fragment(R.layout.fragment_useropt) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the button by its ID
        val btnDonorReg: Button = view.findViewById(R.id.btnDonorReg)
        val btnNGOReg: Button = view.findViewById(R.id.btnNGOReg)
        val btnAdmin: Button = view.findViewById(R.id.btnAdmin)

        // Set click listener for the button
        btnDonorReg.setOnClickListener {
            // Create an intent to navigate to the other activity
            val intent = Intent(activity, DonorRegActivity::class.java)

            // Start the activity
            startActivity(intent)
        }

        btnNGOReg.setOnClickListener {
            // Create an intent to navigate to the other activity
            val intent = Intent(activity, NGORegActivity::class.java)

            // Start the activity
            startActivity(intent)
        }

        btnAdmin.setOnClickListener {
            // Create an intent to navigate to the other activity
            val intent = Intent(activity, DonorRegActivity::class.java)

            // Start the activity
            startActivity(intent)
        }
    }
}