package com.example.iymn.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.iymn.R
import com.example.iymn.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Date

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {
            login()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check condition
        if (requestCode == 100) {
            // When request code is equal to 100 initialize task
            val signInAccountTask: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)

            // check condition
            if (signInAccountTask.isSuccessful) {
                // When google sign in successful initialize string
                val s = "Google sign in successful"
                // Display Toast
                displayToast(s)
                // Initialize sign in account
                try {
                    // Initialize sign in account
                    val googleSignInAccount = signInAccountTask.getResult(ApiException::class.java)
                    // Check condition
                    if (googleSignInAccount != null) {
                        // When sign in account is not equal to null initialize auth credential
                        val authCredential: AuthCredential = GoogleAuthProvider.getCredential(
                            googleSignInAccount.idToken, null
                        )
                        // Check credential
                        auth.signInWithCredential(authCredential)
                            .addOnCompleteListener(this) { task ->
                                // Check condition
                                if (task.isSuccessful) {
                                    // When task is successful redirect to profile activity
                                    startActivity(
                                        Intent(
                                            this,
                                            RecentDonationsActivity::class.java
                                        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                    // Display Toast
                                    displayToast("Firebase authentication successful")
                                } else {
                                    // When task is unsuccessful display Toast
                                    displayToast(
                                        "Authentication Failed :" + task.exception?.message
                                    )
                                }
                            }
                    }
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun login() {
        val email = binding.etEmail.text.toString()
        val pass = binding.etPassword.text.toString()

        if (email.isBlank() || pass.isBlank()) {
            showEmptyFieldDialog("Kindly finish the form", "Empty Fields")
            return
        }

        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val firebaseUser = auth.currentUser
                if (firebaseUser != null) {
                    // Fetch the accountType from the user's data (e.g., Firestore)
                    val userId = firebaseUser.uid
                    val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

                    userRef.get().addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val accountType = document.getString("accountType")

                            if (accountType == "Admin") {
                                // Proceed without checking email verification for admin
                                showEmptyFieldDialog("Successfully Logged In", "Welcome back, Admin!")
                            } else {
                                // Check the user's creation timestamp
                                firebaseUser.metadata?.creationTimestamp?.let { creationTime ->
                                    // Convert the creation timestamp to a Date object
                                    val creationDate = Date(creationTime)

                                    // Define the target date (August 30, 2024)
                                    val targetDate = Calendar.getInstance().apply {
                                        set(2024, Calendar.AUGUST, 31)
                                    }.time

                                    if (creationDate.before(targetDate)) {
                                        // If the email is not verified, resend the verification email
                                        if (!firebaseUser.isEmailVerified) {
                                            firebaseUser.sendEmailVerification().addOnCompleteListener { emailTask ->
                                                if (emailTask.isSuccessful) {
                                                    Toast.makeText(this, "Old user detected. Verification email resent.", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(this, "Failed to resend verification email.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            auth.signOut()
                                        } else {
                                            showEmptyFieldDialog("Successfully Logged In", "Welcome back!")
                                        }
                                    } else {
                                        if (!firebaseUser.isEmailVerified) {
                                            Toast.makeText(this, "Please check your email for the verification link", Toast.LENGTH_LONG).show()
                                            auth.signOut()
                                        }else{
                                            showEmptyFieldDialog("Successfully Logged In", "Welcome back!")
                                        }
                                    }
                                } ?: run {
                                    Toast.makeText(this, "Failed to retrieve creation timestamp.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to retrieve user data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Log In failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayToast(s: String) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_SHORT).show()
    }

    private fun showEmptyFieldDialog(message:String, status: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            setTitle(status)
            setMessage(message)
            setPositiveButton("OK") { dialog, which ->
                startActivity(Intent(this@LoginActivity, DashboardActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }




}