package com.example.iymn.Activity

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.iymn.R
import com.example.iymn.databinding.ActivityProfileBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid // Get current user's UID
            fetchUserData(userId) // Fetch user data using the UID
        } else {
            Toast.makeText(this, "You are logged Out", Toast.LENGTH_SHORT).show()
        }
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNav)
        bottomNavigationView.setOnItemSelectedListener  { menuItem  ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    startNewActivity(DashboardActivity::class.java)
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,R.anim.slide_in_right,R.anim.slide_out_left)
                    true
                }
                R.id.navigation_donate -> {
                    startNewActivity(DonationFormActivity::class.java)
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,R.anim.slide_in_right,R.anim.slide_out_left)
                    true
                }
                R.id.navigation_profile -> {
                    startNewActivity(ProfileActivity::class.java)
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,R.anim.slide_in_right,R.anim.slide_out_left)
                    true
                }
                else -> false
            }
        }

        binding.btnToEditProfile.setOnClickListener {
            startActivity(Intent(this,SetUpProfileActivity::class.java))
        }
        binding.btnToAboutUs.setOnClickListener {
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
        }
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }
    private fun fetchUserData(userId: String) {
        val ivProfileImg: ImageView = findViewById(R.id.ivProfileImg)

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val data = documentSnapshot.data
                    val name = data?.get("email") as String
                    val accountType = data?.get("accountType") as? String
                    val imagePath = data?.get("image") as? String
                    Log.d("ProfileActivity", "$name")
                    if (name != null && accountType != null ) {
                        binding.tvProfileName.text = name
                        binding.tvSubName.text = accountType
                        Picasso.get()
                            .load(imagePath)
                            .placeholder(R.drawable.ic_insert_img) // Placeholder image
                            .error(R.drawable.ic_folder) // Error image
                            .into(ivProfileImg) // Load into the specified ImageView
                        displayNameForAccountType(accountType)
                    } else {
                        Log.d("ProfileActivity", "Data fields are null")
                    }
                }else{
                    Log.d("ProfileActivity", "Document does not exist")
                }
            }
            .addOnFailureListener { e ->
                Log.w("ProfileActivity", "Error fetching user data", e)
                Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayNameForAccountType(accountType: String) {
        when (accountType) {
            "Admin" -> {
                binding.tvSubName.text = "ADMINISTRATOR"
            }
            "Donor" -> {
                binding.tvSubName.text = "DONOR"
            }
            "NGO" -> {
                binding.tvSubName.text = "NON GOVERNMENTAL ORGANIZATION"
            }
            else -> {
                // Handle default case or show a default fragment
            }
        }
    }
    private fun logout() {
        auth.signOut() // Sign out the current user
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finishAffinity()
    }
    private fun startNewActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }
}