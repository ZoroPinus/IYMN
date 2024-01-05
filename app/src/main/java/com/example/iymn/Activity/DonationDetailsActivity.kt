package com.example.iymn.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.iymn.R
import com.example.iymn.databinding.ActivityDashboardBinding
import com.example.iymn.databinding.ActivityDonationDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class DonationDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDonationDetailsBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private var documentUid: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonationDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val item = intent.getStringExtra("donationRef").toString();
        val vegImg: ImageView = findViewById(R.id.ivVegImg)
        fetchDataFromFirestore(item, vegImg)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        fetchDataBasedOnUserType()

        binding.btnAccept.setOnClickListener{
            documentUid?.let { it1 -> updateFieldInFirestore(it1, "ACCEPTED") }
        }
        binding.btnReject.setOnClickListener{
            documentUid?.let { it1 -> updateFieldInFirestore(it1, "REJECTED") }
        }

    }



    private fun fetchDataFromFirestore(donationId: String, imageView: ImageView) {
        db.collection("donations")
            .document(donationId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val data = documentSnapshot.data
                documentUid = documentSnapshot.id
                val vegName: TextView = findViewById(R.id.tvVegname)
                val donor: TextView = findViewById(R.id.tvDonor)
                val quantity: TextView = findViewById(R.id.tvQuantity)
                val timeDonated: TextView = findViewById(R.id.tvTimeDonated)
                val contact: TextView = findViewById(R.id.tvContact)
                val nearestNgo: TextView = findViewById(R.id.tvNGO)
                val status: TextView = findViewById(R.id.tvStatus)
                val fetchedQuantity = data?.get("quantity") as String
                val fetchedQuantityType = data["quantityType"] as String
                val imagePath = data["image"] as String
                val donorUID = data["donorUID"] as String
                // Fetch user's email based on donorUID
                db.collection("users")
                    .document(donorUID)
                    .get()
                    .addOnSuccessListener { userSnapshot ->
                        val userEmail = userSnapshot.getString("email")
                        val userContact = userSnapshot.getString("contact")
                        Picasso.get()
                            .load(imagePath)
                            .placeholder(R.drawable.ic_insert_img) // Placeholder image
                            .error(R.drawable.ic_folder) // Error image
                            .into(imageView) // Load into the specified ImageView
                        val vegNameCap = data["vegName"] as String
                        vegName.text = vegNameCap.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(
                                Locale.getDefault()
                            ) else it.toString()
                        }
                        donor.text = userEmail
                        quantity.text = getString(
                            R.string.formatted_quantity,
                            fetchedQuantity,
                            fetchedQuantityType
                        )
                        timeDonated.text =formatDate(data["donateDate"] as String)
                        contact.text = userContact
                        nearestNgo.text = data["recipient"] as String
                        status.text = data["status"] as String
                    }
                    .addOnFailureListener { e ->
                        Log.w("DonationHistoryActivity", "Error fetching user data", e)
                        Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.w("DonationHistoryActivity", "Error fetching data", e)
                Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchDataBasedOnUserType() {
        if (currentUser == null) {
            Toast.makeText(this, "Eror", Toast.LENGTH_SHORT).show()
        } else {
            val uid = currentUser!!.uid
            db.collection("users").document(uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    val accountType = documentSnapshot.getString("accountType")
                    when (accountType) {
                        "NGO", "Admin" -> binding.btnAccept.visibility = View.VISIBLE
                        else -> binding.btnAccept.visibility = View.GONE
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("DonationHistoryActivity", "Error fetching user data", e)
                    Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMMM d, yyyy, h:mm a", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        return outputFormat.format(date)
    }
    private fun updateFieldInFirestore(documentId: String, status: String) {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("donations")

        // Reference the specific document by its ID
        val documentRef = collectionRef.document(documentId)

        // Update the specific field
        documentRef
            .update("status", status)
            .addOnSuccessListener {
                val successMessage = when (status) {
                    "ACCEPTED" -> "You have successfully accepted the donation."
                    "REJECTED" -> "You have successfully rejected the donation."
                    // Add more conditions as needed
                    else -> "Operation successful with status: $status"
                }
                showEmptyFieldDialog("Success","You have successfully $successMessage")
            }
            .addOnFailureListener { e ->
                // Update failed
                // Handle the error
                Log.w("DonationHistoryActivity", "Error fetching user data", e)
            }
    }

    private fun showEmptyFieldDialog(message:String, status: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            setTitle(status)
            setMessage(message)
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}