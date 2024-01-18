package com.example.iymn.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.iymn.Activity.DonationFormActivity
import com.example.iymn.Activity.DonationHistoryActivity
import com.example.iymn.Activity.NGOPartnersActivity
import com.example.iymn.R
import com.example.iymn.databinding.FragmentDonorDashboardBinding
import com.example.iymn.databinding.FragmentNGODashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DonorDashboardFragment : Fragment() {
    private lateinit var binding: FragmentDonorDashboardBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDonorDashboardBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid // Get current user's UID
            fetchUserData(userId) // Fetch user data using the UID
        } else {
            Log.d("DonorDashboardActivity", "no current user error")
        }
        binding.btnDonateDonor.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    DonationFormActivity::class.java
                )
            )
        }
        binding.btnDonationHistory.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    DonationHistoryActivity::class.java
                )
            )
        }
        binding.btnNgoPartners.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    NGOPartnersActivity::class.java
                )
            )
        }
        binding.btnFoodMapDonor.setOnClickListener {
            Toast.makeText(requireContext(), "Food Map", Toast.LENGTH_SHORT).show()
        }
        binding.btnAddFeedbackDonor.setOnClickListener {
            Toast.makeText(requireContext(), "Add Feedback", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }
    private fun fetchUserData(userId: String) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val data = documentSnapshot.data
                if (data != null) {
                    val name = data["email"] as String
                    val accountType = data["accountType"] as String
                    // Assuming you have TextViews to display this data
                    binding.tvWelcomeUser.text = getString(R.string.welcome_user, name)
                    binding.tvAccType.text = getString(R.string.account_type, accountType)
                } else {
                    Log.d("DonorDashboardActivity", "No such document")
                }
            }
            .addOnFailureListener { e ->
                Log.w("DonorDashboardActivity", "Error fetching user data", e)
                Toast.makeText(requireContext(), "Error fetching user data", Toast.LENGTH_SHORT).show()
            }
    }
//    override fun onDestroyView() {
//        super.onDestroyView()
//        binding = null
//    }
}