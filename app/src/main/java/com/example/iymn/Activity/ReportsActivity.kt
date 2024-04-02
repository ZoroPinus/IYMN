package com.example.iymn.Activity

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.iymn.R
import com.example.iymn.databinding.ActivityReportsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale


class ReportsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val requestCode = 42 // Your unique request code
    private val writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE

    companion object {
        private const val WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()

        val headerIcon: ImageView = findViewById(R.id.customHeaderIcon)
        val headerText: TextView = findViewById(R.id.customHeaderText)

        headerIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        headerText.setText("Reports")



        // Initialize Firebase
        db = FirebaseFirestore.getInstance()

        displayTable()

        binding.btnDownloadCsv.setOnClickListener {
            // Replace "https://www.example.com" with your actual website URL
            val websiteUrl =
                "https://docs.google.com/spreadsheets/d/1yT9sH0BHo8ePD34YLuMDOMtD83VHv5r80mTD3RMmu34/edit?usp=sharing"

            // Create an Intent with ACTION_VIEW and the Google Sheets URL
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))

            // Set the package name to ensure the link opens in the Google Sheets app if available
            intent.setPackage("com.google.android.apps.docs")

            // Check if there is an app to handle the Intent
            if (intent.resolveActivity(packageManager) != null) {
                // Open the Google Sheets link in the browser or Google Sheets app
                startActivity(intent)
            } else {
                // If the Google Sheets app is not available, open in a browser
                intent.setPackage(null) // Clear the package to open in the default browser
                startActivity(intent)
            }
        }
    }
    private fun isDarkThemeEnabled(): Boolean {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    private fun displayTable() {
        db.collection("donations")
            .orderBy("donateDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { donationsSnapshot ->
                // Sample data for the table
                val headers = arrayOf("Donor", "Recipient", "Donation", "Date", "Status")

                // Create table header row
                val headerRow = TableRow(this)
                for (header in headers) {
                    val textView = TextView(this)
                    textView.text = header
                    textView.setPadding(30, 30, 30, 30)
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.toFloat()) // Set text size
                    if (isDarkThemeEnabled()) {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.white))
                    } else {
                        textView.setTextColor(ContextCompat.getColor(this, R.color.black))
                    }
                    textView.typeface =
                        Typeface.create("montserrat_bold", Typeface.BOLD) // Set font family
                    headerRow.addView(textView)
                }
                binding.tableLayout.addView(headerRow)

                // Process each donation document
                for (donationDocument in donationsSnapshot.documents) {
                    val dateFormat = SimpleDateFormat("MM-dd-yyyy ", Locale.getDefault())
                    val donorId = donationDocument.getString("donorUID")
                        ?: "Invalid Date"// Use document ID as UID
                    val recipient = donationDocument.getString("recipient")
                    val vegName = donationDocument.getString("vegName")
                    val timestamp = donationDocument.getTimestamp("donateDate")
                    val date = timestamp?.toDate()?.let { dateFormat.format(it) } ?: "Invalid Date"
                    val status = donationDocument.getString("status")
                    val name = donationDocument.getString("donorName")

                    // Fetch user data for the donorId
                    val dataRow = TableRow(this)
                    val rowData = arrayOf(name, recipient ?: "", vegName ?: "", date, status ?: "")
                    for (cellData in rowData) {
                        val textView = TextView(this)
                        textView.text = cellData
                        textView.setTextSize(
                            TypedValue.COMPLEX_UNIT_SP,
                            18.toFloat()
                        ) // Set text size
                        if (isDarkThemeEnabled()) {
                            textView.setTextColor(ContextCompat.getColor(this, R.color.white))
                        } else {
                            textView.setTextColor(ContextCompat.getColor(this, R.color.black))
                        }
                        textView.typeface =
                            Typeface.create("montserrat", Typeface.NORMAL) // Set font family
                        textView.setPadding(30, 30, 30, 30)
                        dataRow.addView(textView)
                    }
                    binding.tableLayout.addView(dataRow)

                }
            }
            .addOnFailureListener { exception ->
                // Handle errors
                Log.e(TAG, "Error getting donations: ", exception)
            }

    }


}