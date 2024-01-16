package com.example.iymn.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.iymn.R
import com.example.iymn.databinding.ActivityRegistrationFormBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegistrationFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationFormBinding
    private lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialising auth object
        auth = FirebaseAuth.getInstance()

        db = Firebase.firestore

        binding.btnRegister.setOnClickListener {
            signUpUser()
        }

        fetchNGOPartnersFromFirestore()
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
                saveUserData(userId, email, contact, accountType)
                finish()
            } else {
                Toast.makeText(this, "Signed Up Failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserData(userId: String, email:String, contact: String, accountType:String){
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
                val intent = Intent(this, DashboardActivity::class.java)
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

    private fun fetchNGOPartnersFromFirestore() {
        val db = Firebase.firestore
        val partnersRef = db.collection("ngoPartners") // Assuming "ngoPartners" is the collection name

        partnersRef.get()
            .addOnSuccessListener { querySnapshot ->
                val partnerNames = ArrayList<String>()
                for (document in querySnapshot) {
                    val partnerName = document.getString("partnerName") // Replace with your field name
                    partnerName?.let {
                        partnerNames.add(it)
                    }
                }
                displayNGOPartnersInSpinner(partnerNames)
            }
            .addOnFailureListener { e ->
                // Handle failure
            }
    }

    private fun displayNGOPartnersInSpinner(partnerNames: ArrayList<String>) {
        val spinnerNGOOrg: Spinner = findViewById(R.id.spinnerNGOOrg) // Replace with your Spinner ID
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, partnerNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerNGOOrg.adapter = adapter
    }
}