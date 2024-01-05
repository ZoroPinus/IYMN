package com.example.iymn.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.iymn.Activity.DashboardActivity
import com.example.iymn.Activity.LoginActivity
import com.example.iymn.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StartFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_start, container, false)

        // Get the button
        val btnLogin = view.findViewById<Button>(R.id.btnToLogin)
        val btnRegister = view.findViewById<Button>(R.id.btnToRegister)

        auth = FirebaseAuth.getInstance()

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is signed in, proceed to DashboardActivity
            val intent = Intent(activity, DashboardActivity::class.java)
            startActivity(intent)
            activity?.finish() // Optional: Close the current activity
        }

        // Set a click listener for the button
        btnLogin.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            // Start the LoginActivity
            startActivity(intent)
        } // Set a click listener for the button
        btnRegister.setOnClickListener {
            // Create an instance of Fragment2
            val userOptFragment = UserOptFragment()

            // Replace Fragment1 with Fragment2
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, userOptFragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}