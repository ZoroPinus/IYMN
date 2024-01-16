package com.example.iymn.Activity

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.iymn.Fragments.AdminDashboardFragment
import com.example.iymn.Fragments.DonorDashboardFragment
import com.example.iymn.Fragments.NGODashboardFragment
import com.example.iymn.R
import com.example.iymn.databinding.ActivityDashboardBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid // Get current user's UID
            fetchUserData(userId) // Fetch user data using the UID
        } else {
            finish()
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
                    val tvUsername: TextView = findViewById(R.id.tvWelcomeUser)
                    val tvAccountType: TextView = findViewById(R.id.tvAccType)
                    tvUsername.text = getString(R.string.welcome_user, name)
                    tvAccountType.text = getString(R.string.account_type, accountType)
                    displayFragmentForAccountType(accountType)
                } else {
                    Log.d("DashboardActivity", "No such document")
                }
            }
            .addOnFailureListener { e ->
                Log.w("DashboardActivity", "Error fetching user data", e)
                Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayFragmentForAccountType(accountType: String) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        when (accountType) {
            "Admin" -> {
                val adminFragment = AdminDashboardFragment()
                fragmentTransaction.replace(R.id.fragmentContainer, adminFragment)
            }
            "Donor" -> {
                val donorFragment = DonorDashboardFragment()
                fragmentTransaction.replace(R.id.fragmentContainer, donorFragment)
            }
            "NGO" -> {
                val ngoFragment = NGODashboardFragment()
                fragmentTransaction.replace(R.id.fragmentContainer, ngoFragment)
            }
            else -> {
            }
        }
        fragmentTransaction.commit()
    }
    private fun startNewActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }
}