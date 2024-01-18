package com.example.iymn.Activity

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.iymn.Fragments.AdminDashboardFragment
import com.example.iymn.Fragments.DonationFormFragment
import com.example.iymn.Fragments.DonorDashboardFragment
import com.example.iymn.Fragments.NGODashboardFragment
import com.example.iymn.Fragments.ProfileFragment
import com.example.iymn.Models.UserType
import com.example.iymn.R
import com.example.iymn.Utils.UserRepository
import com.example.iymn.databinding.ActivityDashboardBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private lateinit var userRepository: UserRepository
    private lateinit var userId: String
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        userRepository = UserRepository()

        val currentUser = auth.currentUser


        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNav)
        bottomNavigationView.setOnItemSelectedListener  { menuItem  ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    userRepository.getUserData(userId,
                        onUserLoaded = { user ->
                            when (user.userType) {
                                is UserType.Admin -> {
                                    replaceFragment(AdminDashboardFragment())
                                }
                                is UserType.Donor -> {
                                    replaceFragment(DonorDashboardFragment())
                                }
                                is UserType.Ngo -> {
                                    replaceFragment(NGODashboardFragment())
                                }
                            }
                        },
                        onError = { exception ->
                            // Handle error loading user data
                            Log.e(TAG, "Error loading user data: $exception")
                        }
                    )
                    true
                }
                R.id.navigation_donate -> {
                    replaceFragment(DonationFormFragment())
                    true
                }
                R.id.navigation_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        if (currentUser != null) {
            userId = currentUser.uid // Get current user's UID
            userRepository.getUserData(userId,
                onUserLoaded = { user ->
                    when (user.userType) {
                        is UserType.Admin -> {
                            replaceFragment(AdminDashboardFragment())
                        }
                        is UserType.Donor -> {
                            replaceFragment(DonorDashboardFragment())
                        }
                        is UserType.Ngo -> {
                            replaceFragment(NGODashboardFragment())
                        }
                    }
                },
                onError = { exception ->
                    // Handle error loading user data
                    Log.e(TAG, "Error loading user data: $exception")
                }
            ) // Fetch user data using the UID
        } else {
            finish()
        }
    }

    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer,fragment).commit()
    }
}