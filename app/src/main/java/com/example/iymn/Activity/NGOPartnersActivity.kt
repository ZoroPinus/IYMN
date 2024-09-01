package com.example.iymn.Activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iymn.Adapters.NGOItemAdapter
import com.example.iymn.Fragments.AddNGOPartnerFragment
import com.example.iymn.Models.NGOItemViewModel
import com.example.iymn.R
import com.example.iymn.databinding.ActivityNgopartnersBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NGOPartnersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNgopartnersBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var NGOItemAdapter: NGOItemAdapter
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNgopartnersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHeader()
        setupRecyclerView()
        setupSearchBar()
        checkUserAccountType()
        fetchDataFromFirestore()

        binding.btnAddNgoPartner.setOnClickListener {
            showAddNGOPartnerFragment()
        }
    }

    private fun setupHeader() {
        val headerIcon: ImageView = findViewById(R.id.customHeaderIcon)
        val headerText: TextView = findViewById(R.id.customHeaderText)

        headerIcon.setOnClickListener {
            onBackPressed()
        }
        headerText.text = "NGO Partners"
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.NGOrecyclerview)
        NGOItemAdapter = NGOItemAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = NGOItemAdapter
    }

    private fun setupSearchBar() {
        val searchBar: EditText = findViewById(R.id.searchBar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                NGOItemAdapter.filter(query)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun showAddNGOPartnerFragment() {
        // Show the fragment container and hide the RecyclerView
        binding.NGOfragmentContainer.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        binding.searchBar.visibility = View.GONE
        // Create the fragment instance
        val fragment = AddNGOPartnerFragment()

        // Perform the fragment transaction
        supportFragmentManager.beginTransaction()
            .replace(R.id.NGOfragmentContainer, fragment)
            .addToBackStack(null) // Add to back stack to handle back navigation
            .commit()
    }

    override fun onBackPressed() {
        // Check if the AddNGOPartnerFragment is visible
        val fragment = supportFragmentManager.findFragmentById(R.id.NGOfragmentContainer)
        if (fragment is AddNGOPartnerFragment) {
            // Pop the fragment from the back stack and show the RecyclerView
            supportFragmentManager.popBackStack()
            binding.NGOfragmentContainer.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            binding.searchBar.visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }



    private fun fetchDataFromFirestore() {
        val collectionRef = db.collection("ngoPartners")

        collectionRef.get()
            .addOnSuccessListener { documents ->
                val itemList = ArrayList<NGOItemViewModel>()

                for (document in documents) {
                    val id = document.id
                    val ngoName = document.getString("ngoName") ?: ""
                    val image = document.getString("image") ?: ""
                    val contact = document.getString("contact") ?: ""
                    val address = document.getString("address") ?: ""
                    val areaOfResponsibility = document.getString("areaOfResponsibility") ?: ""
                    val ngo = NGOItemViewModel(id, image, ngoName, contact, address, areaOfResponsibility)
                    itemList.add(ngo)
                }

                if (itemList.isEmpty()) {
                    showDefaultUI()
                } else {
                    NGOItemAdapter.setOriginalList(itemList)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("NGOPartnersActivity", "Error getting documents: ", exception)
            }
    }

    private fun showDefaultUI() {
        binding.NGOfragmentContainer.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun checkUserAccountType() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = user.uid

            val userRef = db.collection("users").document(userId)
            userRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val accountType = documentSnapshot.getString("accountType")
                        binding.btnAddNgoPartner.visibility = if (accountType == "Admin") {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("NGOPartnersActivity", "Error fetching user document: ", exception)
                }
        }
    }

}
