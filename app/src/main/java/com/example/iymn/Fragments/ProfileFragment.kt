package com.example.iymn.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.iymn.Activity.AboutUsActivity
import com.example.iymn.Activity.MainActivity
import com.example.iymn.Activity.SetUpProfileActivity
import com.example.iymn.R
import com.example.iymn.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid // Get current user's UID
            fetchUserData(userId) // Fetch user data using the UID
        } else {
            Toast.makeText(requireContext(), "You are logged Out", Toast.LENGTH_SHORT).show()
        }


        binding.btnToEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), SetUpProfileActivity::class.java))
        }
        binding.btnToAboutUs.setOnClickListener {
            startActivity(Intent(requireContext(), AboutUsActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            logout()
        }


    }
    private fun fetchUserData(userId: String) {
        val ivProfileImg: ImageView = requireView().findViewById(R.id.ivProfileImg)

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                    val data = documentSnapshot.data
                if (data != null) {
                    val name = data["name"] as String
                    val accountType = data["accountType"] as String
                    val imagePath = data["profileImageUrl"] as String
                    if (name != null && accountType != null ) {
                        binding.tvProfileName.text = name
                        binding.tvSubName.text = accountType
                        Glide.with(requireContext())
                            .load(imagePath)
                            .placeholder(R.drawable.ic_profile) // Placeholder image while loading
                            .error(R.drawable.ic_insert_img) // Image to show if loading fails
                            .into(binding.ivProfileImg)
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
                Toast.makeText(requireContext(), "Error fetching user data", Toast.LENGTH_SHORT).show()
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
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        requireActivity().finishAffinity()
    }


}