package com.example.iymn.Activity

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.iymn.R
import com.example.iymn.databinding.ActivityRegisteredAccountsBinding
import com.example.iymn.databinding.ActivityReportsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisteredAccountsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisteredAccountsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisteredAccountsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()

        val headerIcon: ImageView = findViewById(R.id.customHeaderIcon)
        val headerText: TextView = findViewById(R.id.customHeaderText)

        headerIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        headerText.setText("Registered Accounts")

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()

        displayTable("Donor")

        binding.btnShowDonors.setOnClickListener {
            displayTable("Donor")
        }

        binding.btnShowNgo.setOnClickListener {
            displayTable("NGO")
        }

    }

    private fun displayTable(accType:String){
        binding.regAccountsTableLayout.removeAllViews()
        db.collection("users")
            .whereEqualTo("accountType", accType)
            .get()
            .addOnSuccessListener { donationsSnapshot ->
                // Sample data for the table
                val headers = arrayOf("Name", "Account Type", "Address", "Contact", "Email", "Organization")

                // Create table header row
                val headerRow = TableRow(this)
                for (header in headers) {
                    val textView = TextView(this)
                    textView.text = header
                    textView.setPadding(5, 5, 5, 5)
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.toFloat()) // Set text size
                    textView.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black
                        )
                    ) // Set text color
                    textView.typeface =
                        Typeface.create("montserrat_bold", Typeface.BOLD) // Set font family
                    headerRow.addView(textView)
                }
                binding.regAccountsTableLayout.addView(headerRow)

                // Process each document in the result
                for (document in donationsSnapshot) {
                    val rowData = arrayOf(
                        document.getString("name") ?: "", // Replace "donor" with the field name in your Firestore document
                        document.getString("accountType") ?: "", // Replace "recipient" with the field name in your Firestore document
                        document.getString("address") ?: "", // Replace "donation" with the field name in your Firestore document
                        document.getString("contact") ?: "", // Replace "date" with the field name in your Firestore document
                        document.getString("email") ?: "", // Replace "status" with the field name in your Firestore document
                        document.getString("NgoOrg") ?: "N/A" // Replace "status" with the field name in your Firestore document
                    )

                    // Create table data row for each document
                    val dataRow = TableRow(this)
                    for (cellData in rowData) {
                        val textView = TextView(this)
                        textView.text = cellData
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.toFloat()) // Set text size
                        textView.setTextColor(ContextCompat.getColor(this, R.color.black)) // Set text color
                        textView.typeface = Typeface.create("montserrat", Typeface.NORMAL) // Set font family
                        textView.setPadding(5, 5, 5, 5)
                        dataRow.addView(textView)
                    }
                    binding.regAccountsTableLayout.addView(dataRow)
                }
            }
            .addOnFailureListener { exception ->
                // Handle errors
                Log.e(TAG, "Error getting documents: ", exception)
            }
    }


}