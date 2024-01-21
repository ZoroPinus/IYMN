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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.iymn.Activity.DonationDetailsActivity
import com.example.iymn.Activity.DonationHistoryActivity
import com.example.iymn.Activity.FeedbackActivity
import com.example.iymn.Activity.FoodMapActivity
import com.example.iymn.Activity.NGOPartnersActivity
import com.example.iymn.Adapters.DonatedItemAdapter
import com.example.iymn.Adapters.VegItemAdapter
import com.example.iymn.Models.DonatedItem
import com.example.iymn.Models.VegItemViewModel
import com.example.iymn.R
import com.example.iymn.databinding.FragmentDonorDashboardBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class DonorDashboardFragment : Fragment() {
    private lateinit var binding: FragmentDonorDashboardBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: DonatedItemAdapter
    val dataList: MutableList<DonatedItem> = ArrayList()
    private var currentUser: FirebaseUser? = null
    private lateinit var displayName: String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDonorDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        // getting the recyclerview by its id
        val donatedRecyclerview = requireView().findViewById<RecyclerView>(R.id.donatedRecyclerview)

        // this creates a vertical layout Manager
        donatedRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        adapter = DonatedItemAdapter(dataList)
        donatedRecyclerview.adapter = adapter
        // Set item click listener in the activity
        adapter.setOnItemClickListener(object : DonatedItemAdapter.OnItemClickListener {
            override fun onItemClick(documentId: String) {
                // Handle item click here, navigate to details activity passing document ID
                val intent =
                    Intent(requireContext(), DonationDetailsActivity::class.java)
                intent.putExtra("donationRef", documentId)
                startActivity(intent)
            }
        })


        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid // Get current user's UID
            fetchUserData(userId) // Fetch user data using the UID
        } else {
            Log.d("DonorDashboardActivity", "no current user error")
        }
        binding.btnDonateDonor.setOnClickListener {
            Toast.makeText(requireContext(), "Food Map", Toast.LENGTH_SHORT).show()
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
                    fetchForDonor()
                } else {
                    Log.d("DonorDashboardActivity", "No such document")
                }
            }
            .addOnFailureListener { e ->
                Log.w("DonorDashboardActivity", "Error fetching user data", e)
                Toast.makeText(requireContext(), "Error fetching user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchForDonor() {
        db.collection("donations")
            .whereEqualTo("donorUID", currentUser?.uid)
            .limit(4)
            .get()
            .addOnSuccessListener { documents ->
                val dataList: MutableList<DonatedItem> = mutableListOf()
                for (document in documents) {
                    val docId = document.id
                    val date = document.getString("donateDate")?.let { formatDate(it) }
                    val vegName = document.getString("vegName") ?: "Default Item Name"
                    val status = document.getString("status") ?: "Default Item Name"
                    val recipient = document.getString("recipient") ?: "Default Item Name"
                    val itemName = vegName.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
                    dataList.add(DonatedItem(docId, itemName,recipient,date))
                }
                adapter.updateData(dataList)
            }
            .addOnFailureListener { e ->
                handleFetchDataFailure(e)
            }
    }
    private fun handleFetchDataFailure(e: Exception) {
        Log.w("DonationHistoryActivity", "Error fetching data", e)
        Toast.makeText(requireContext(), "Error fetching data", Toast.LENGTH_SHORT).show()
    }
    private fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMMM d, yyyy, h:mm a", Locale.getDefault())

        val date = inputFormat.parse(dateString)
        return outputFormat.format(date)
    }
}