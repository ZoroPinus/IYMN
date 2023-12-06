package com.example.iymn.Activity

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.iymn.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegistrationFormActivity : AppCompatActivity() {
    lateinit var etName: EditText
    lateinit var etEmail: EditText
    lateinit var etContact: EditText
    lateinit var etConfPassword: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    data class User(val name: String, val email: String, val contact: Number, val accountType: String)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_form)

        // View Bindings
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etContact = findViewById(R.id.etContact)
        etConfPassword = findViewById(R.id.etConfPassword)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)

        // Initialising auth object
        auth = FirebaseAuth.getInstance()


        btnRegister.setOnClickListener {
            signUpUser()
        }
    }

    private fun signUpUser() {
        val name = etName.text.toString()
        val email = etEmail.text.toString()
        val contact = etContact.text.toString()
        val pass = etPassword.text.toString()
        val confirmPassword = etConfPassword.text.toString()
        val accountType = intent.getStringExtra("ACCOUNT_TYPE").toString();
        // check pass
        if (email.isBlank() || pass.isBlank() || confirmPassword.isBlank()) {
            Toast.makeText(this, "Email and Password can't be blank", Toast.LENGTH_SHORT).show()
            return
        }

        if (pass != confirmPassword) {
            Toast.makeText(this, "Password and Confirm Password do not match", Toast.LENGTH_SHORT)
                .show()
            return
        }
        // If all credential are correct
        // We call createUserWithEmailAndPassword
        // using auth object and pass the
        // email and pass in it.


        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                val firebaseUser = auth.currentUser
                val userId = firebaseUser?.uid.toString()
                Toast.makeText(this, "Successfully Singed Up", Toast.LENGTH_SHORT).show()
                saveUserData(userId, name, email, contact, accountType)
                finish()
            } else {
                Toast.makeText(this, "Singed Up Failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserData(userId: String, name: String, email:String, contact: String, accountType:String){
        val user = hashMapOf(
            "name" to name,
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
            }
            .addOnFailureListener { e ->
                // Handle errors
                Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }



}