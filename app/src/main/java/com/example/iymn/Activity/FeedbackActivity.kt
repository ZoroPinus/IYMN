package com.example.iymn.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.iymn.R
import com.example.iymn.databinding.ActivityFeedbackBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FeedbackActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeedbackBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val headerIcon: ImageView = findViewById(R.id.customHeaderIcon)
        val headerText: TextView = findViewById(R.id.customHeaderText)

        headerIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        headerText.setText("Feedback Form")

        binding.btnSubmitFeedback.setOnClickListener {
            submitFeedback()
        }

    }

    private fun submitFeedback() {
        val rating = binding.rBar.rating.toString()
        val textFeedback = binding.etFeedbackDetails.text.toString()

        if (rating.isBlank() || textFeedback.isBlank()) {
            Toast.makeText(
                this@FeedbackActivity,
                "Kindly complete the form",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Get user ID
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(
                this@FeedbackActivity,
                "User not authenticated",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        // Get current date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDateTime = dateFormat.format(Date())
        // Create a new feedback document
        val feedback = hashMapOf(
            "userId" to userId,
            "rating" to rating.toDouble(),
            "textFeedback" to textFeedback,
            "date" to currentDateTime
        )

        // Add the feedback document to the "feedback" collection
        db.collection("feedbacks")
            .add(feedback)
            .addOnSuccessListener {
                showSuccessDialog()
                resetForm()
            }
            .addOnFailureListener {
                showErrorDialog()
            }
    }
    private fun showSuccessDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Success")
            .setMessage("Feedback submitted successfully")
            .setPositiveButton("OK") { _, _ -> }
            .create()

        dialog.show()
    }

    private fun showErrorDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage("Failed to submit feedback. Please try again.")
            .setPositiveButton("OK") { _, _ -> }
            .create()

        dialog.show()
    }

    private fun resetForm() {
        // Assuming you have a RatingBar and an EditText in your layout
        binding.rBar.rating = 0.0f
        binding.etFeedbackDetails.text.clear()
    }
}
