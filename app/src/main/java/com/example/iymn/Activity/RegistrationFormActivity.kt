package com.example.iymn.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.iymn.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegistrationFormActivity : AppCompatActivity() {
    lateinit var etEmail: EditText
    lateinit var etContact: EditText
    lateinit var etConfPassword: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
//    data class User(val name: String, val email: String, val contact: Number, val accountType: String)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_form)

        // View Bindings
        etEmail = findViewById(R.id.etEmail)
        etContact = findViewById(R.id.etContact)
        etConfPassword = findViewById(R.id.etConfPassword)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)

        // Initialising auth object
        auth = FirebaseAuth.getInstance()

        db = Firebase.firestore


        btnRegister.setOnClickListener {
            signUpUser()
        }
    }

    private fun signUpUser() {
        val email = etEmail.text.toString()
        val contact = etContact.text.toString()
        val pass = etPassword.text.toString()
        val confirmPassword = etConfPassword.text.toString()
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
}