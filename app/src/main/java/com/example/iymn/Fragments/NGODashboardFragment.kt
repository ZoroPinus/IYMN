package com.example.iymn.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.iymn.Activity.DonationHistoryActivity
import com.example.iymn.Activity.FeedbackActivity
import com.example.iymn.Activity.FoodMapActivity
import com.example.iymn.Activity.ReportsActivity
import com.example.iymn.R
import com.example.iymn.databinding.FragmentNGODashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class NGODashboardFragment : Fragment() {
    private lateinit var binding: FragmentNGODashboardBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private var currentUser: FirebaseUser? = null
    private lateinit var displayName: String

    companion object {
        var ngoOrg: String = "Initial Input"
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNGODashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid // Get current user's UID
            fetchUserData(userId) // Fetch user data using the UID
        } else {
            Log.d("DonorDashboardActivity", "no current user error")
        }

        binding.profileIconImageView.setOnClickListener {
            replaceFragment(ProfileFragment())
        }
        binding.btnReportsNGO.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    ReportsActivity::class.java
                )
            )
        }
        binding.btnAcceptedDonations.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    DonationHistoryActivity::class.java
                )
            )
        }
        binding.btnFoodMapNGO.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    FoodMapActivity::class.java
                )
            )
        }
        binding.btnAddFeedbackDonor.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    FeedbackActivity::class.java
                )
            )
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
                    val email = data["email"] as String
                    val ngoOrgs= data["NgoOrg"] as String
                    ngoOrg = ngoOrgs
                    val profileImg = data["profileImageUrl"] as String
                    if(name == null){
                        displayName = email
                    }else{
                        displayName = name
                    }
                    Glide.with(requireContext())
                        .load(profileImg)
                        .placeholder(R.drawable.ic_profile) // Placeholder image while loading
                        .error(R.drawable.ic_insert_img) // Image to show if loading fails
                        .into(binding.profileIconImageView)
                    // Assuming you have TextViews to display this data
                    binding.tvWelcomeUser.text = displayName
                } else {
                    Log.d("AdminDashboardActivity", "No such document")
                }
            }
            .addOnFailureListener { e ->
                Log.w("AdminDashboardActivity", "Error fetching user data", e)
                Toast.makeText(requireContext(), "Error fetching user data", Toast.LENGTH_SHORT).show()
            }
    }
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = parentFragmentManager
        val transaction = fragmentManager.beginTransaction()

        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null) // Optional: Adds the transaction to the back stack
        transaction.commit()
    }
}