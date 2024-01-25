package com.example.iymn.Activity

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.iymn.Models.NGOOption
import com.example.iymn.R
import com.example.iymn.databinding.ActivityRegistrationFormBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegistrationFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationFormBinding
    private lateinit var auth: FirebaseAuth
    private var selectedNgo: String = ""
    private lateinit var ngoOptionsList: List<NGOOption>
    lateinit var db: FirebaseFirestore
    lateinit var regType : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        regType = intent.getStringExtra("ACCOUNT_TYPE").toString()

        if (regType == "Donor") {
            binding.orgSelectContainer.visibility = View.GONE
        } else {
            // Show etSelectOrganzation for other account types
            binding.orgSelectContainer.visibility = View.VISIBLE
        }


        // Initialising auth object
        auth = FirebaseAuth.getInstance()

        db = FirebaseFirestore.getInstance()
        fetchNGOOptionsFromFirestore()
        binding.btnRegister.setOnClickListener {
            signUpUser()
        }

        binding.spinnerNGOOrg.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Handle the selected item here
                selectedNgo = ngoOptionsList[position].id
                binding.tvNGOOrg.text = ngoOptionsList[position].name
                // Do something with the selected item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle no selection if needed
            }
        }
    }

    private fun signUpUser() {
        val email = binding.etEmail.text.toString()
        val contact = binding.etContact.text.toString()
        val pass = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfPassword.text.toString()
        val accountType = intent.getStringExtra("ACCOUNT_TYPE").toString();
        // check pass
        if (email.isBlank() ) {
            showEmptyFieldDialog("Please input an email")
            return
        }
        if (contact.isBlank() ) {
            showEmptyFieldDialog("Please input your contact info")
            return
        }
        if (pass.isBlank() || confirmPassword.isBlank()) {
            showEmptyFieldDialog("Please input a password")
            return
        }
        if (pass != confirmPassword) {
            showEmptyFieldDialog("Password and Confirm Password do not match")
            return
        }

        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                val firebaseUser = auth.currentUser
                val userId = firebaseUser?.uid.toString()
                if(regType == "Donor"){
                    saveUserDataDonor(userId, email, contact, accountType)
                }else{
                    saveUserDataNgo(userId, email, contact, accountType, selectedNgo)
                }
                finish()
            } else {
                Toast.makeText(this, "Signed Up Failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserDataDonor(userId: String, email:String, contact: String, accountType:String){
        val user = hashMapOf(
            "email" to email,
            "contact" to contact,
            "accountType" to accountType,
        )

        val usersCollection = db.collection("users")
        usersCollection.document(userId)
            .set(user)
            .addOnSuccessListener {
                // Data written successfully
                Toast.makeText(this, "User data saved with custom ID to Firestore", Toast.LENGTH_SHORT).show()

                // Navigate to DashboardScreen upon successful registration
                val intent = Intent(this, SetUpProfileActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                // Handle errors
                val errorMessage = "Error saving user data: ${e.message}"
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                Log.e("Firestore", errorMessage)
            }
    }

    private fun saveUserDataNgo(userId: String, email:String, contact: String, accountType:String, ngoOrg:String){
        val user = hashMapOf(
            "email" to email,
            "contact" to contact,
            "accountType" to accountType,
            "NgoOrg" to ngoOrg,
        )

        val usersCollection = db.collection("users")
        usersCollection.document(userId)
            .set(user)
            .addOnSuccessListener {
                // Data written successfully
                Toast.makeText(this, "User data saved with custom ID to Firestore", Toast.LENGTH_SHORT).show()

                // Navigate to DashboardScreen upon successful registration
                val intent = Intent(this, SetUpProfileActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                // Handle errors
                val errorMessage = "Error saving user data: ${e.message}"
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                Log.e("Firestore", errorMessage)
            }
    }

    private fun showEmptyFieldDialog(message:String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            setTitle("Empty Fields")
            setMessage(message)
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
    private fun showSuccessFieldDialog(message:String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            setTitle("Registration complete")
            setMessage("Press OK to Login")
            setPositiveButton("OK") { dialog, which ->
                startActivity(Intent(this@RegistrationFormActivity, DashboardActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun fetchNGOOptionsFromFirestore() {
        db.collection("ngoPartners")
            .get()
            .addOnSuccessListener { result ->
                ngoOptionsList = result.documents.map { document ->
                    val id = document.id
                    val name = document.getString("ngoName") ?: ""
                    NGOOption(id, name)
                }

                // Populate spinner with NGO options
                populateSpinnerWithNGOOptions()
            }
            .addOnFailureListener { exception ->
                // Handle errors
                Log.w(TAG, "Error getting NGO options", exception)
            }
    }

    private fun populateSpinnerWithNGOOptions() {
        val ngoOptions = ngoOptionsList.map { it.name }.toTypedArray()
        val ngoListAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ngoOptions)
        ngoListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerNGOOrg.adapter = ngoListAdapter
    }
}