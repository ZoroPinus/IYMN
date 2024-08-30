package com.example.iymn.Fragments

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.iymn.Activity.DonationHistoryActivity
import com.example.iymn.Activity.FeedbackActivity
import com.example.iymn.Activity.FoodMapActivity
import com.example.iymn.Activity.ReportsActivity
import com.example.iymn.Activity.TrackDonationsActivity
import com.example.iymn.R
import com.example.iymn.databinding.FragmentNGODashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore

class NGODashboardFragment : Fragment() {
    private lateinit var binding: FragmentNGODashboardBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private var currentUser: FirebaseUser? = null
    private lateinit var displayName: String

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid // Get current user's UID
            fetchUserData(userId) // Fetch user data using the UID
            listenForDonations()
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
        binding.btnTrackDonations.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    TrackDonationsActivity::class.java
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
        binding.btnAddCrop.setOnClickListener {
            val fragmentManager = parentFragmentManager
            val transaction = fragmentManager.beginTransaction()

            // Add the CropFragment on top of the current fragment
            transaction.add(R.id.fragmentContainer, CropFragment())

            // Optionally add to the back stack to allow back navigation
            transaction.addToBackStack(null)

            // Commit the transaction
            transaction.commit()
        }

    }

    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission already granted, proceed with notification logic
            listenForDonations()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, proceed with notification logic
                listenForDonations()
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(requireContext(), "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun listenForDonations() {
        val donationsCollection = db.collection("donations")
        donationsCollection.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w("NGODashboardFragment", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshots != null && !snapshots.isEmpty) {
                for (documentChange in snapshots.documentChanges) {
                    when (documentChange.type) {
                        DocumentChange.Type.ADDED -> {
                            // New donation detected
                            val donationData = documentChange.document.data
                            val vegName = donationData["vegName"] as String
                            sendDonationNotification(vegName)
                        }
                        // Handle other types (MODIFIED, REMOVED) if needed
                        DocumentChange.Type.MODIFIED -> {
                            // Do nothing if the document is modified
                        }
                        DocumentChange.Type.REMOVED -> {
                            // Do nothing if the document is removed
                        }
                    }
                }
            }
        }
    }

    private fun sendDonationNotification(vegName: String) {
        // Create a notification channel if not already created
        createNotificationChannel()

        val notificationBuilder = NotificationCompat.Builder(requireContext(), "donation_channel_id")
            .setSmallIcon(R.drawable.app_logo3) // Replace with your app's icon
            .setContentTitle("New Donation Received")
            .setContentText("A new donation of $vegName has been received.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "donation_channel_id"
            val channelName = "Donation Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance)
            val notificationManager = requireContext().getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
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
                    displayName = name
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