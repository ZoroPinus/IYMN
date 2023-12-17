package com.example.iymn.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.iymn.R
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class DonationDetailsActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_details)

        val item = intent.getStringExtra("donationRef").toString();
        val vegImg: ImageView = findViewById(R.id.ivVegImg)
        fetchDataFromFirestore(item, vegImg)

    }

    private fun fetchDataFromFirestore(donationId: String, imageView: ImageView) {
        db.collection("donations")
            .document(donationId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val data = documentSnapshot.data
                val vegName: TextView = findViewById(R.id.tvVegName)
                val description: TextView = findViewById(R.id.tvDescription)
                val quantity: TextView = findViewById(R.id.tvQuantity)
                val fetchedQuantity = data?.get("quantity") as String
                val fetchedQuantityType = data["quantityType"] as String

                // Load image using Picasso into ImageView
                val imagePath = data["image"] as String
                Picasso.get()
                    .load(imagePath)
                    .placeholder(R.drawable.ic_insert_img) // Placeholder image
                    .error(R.drawable.ic_folder) // Error image
                    .into(imageView) // Load into the specified ImageView

                vegName.text = data["vegName"] as String
                description.text = data["description"] as String
                quantity.text = getString(
                    R.string.formatted_quantity,
                    fetchedQuantity,
                    fetchedQuantityType
                )
            }
            .addOnFailureListener { e ->
                Log.w("DonationHistoryActivity", "Error fetching data", e)
                Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show()
            }
    }
}