package com.example.iymn.Activity

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.iymn.R
import com.example.iymn.databinding.ActivityReportsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.FileWriter
import java.io.IOException
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
            exportTableToCSV(binding.tableLayout)
        }
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
                    textView.setTextColor(ContextCompat.getColor(this, R.color.black)) // Set text color
                    textView.typeface = Typeface.create("montserrat_bold", Typeface.BOLD) // Set font family
                    headerRow.addView(textView)
                }
                binding.tableLayout.addView(headerRow)

                // Process each donation document
                for (donationDocument in donationsSnapshot.documents) {
                    val dateFormat = SimpleDateFormat("MM-dd-yyyy ", Locale.getDefault())
                    val donorId = donationDocument.getString("donorUID") ?: "Invalid Date"// Use document ID as UID
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
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.toFloat()) // Set text size
                            textView.setTextColor(ContextCompat.getColor(this, R.color.black)) // Set text color
                            textView.typeface = Typeface.create("montserrat", Typeface.NORMAL) // Set font family
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

    private fun exportTableToCSV(tableLayout: TableLayout) {
        val fileName = "iymn_donation_reports.csv"
        val path = Environment.getExternalStorageDirectory().toString() + "/" + fileName

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request the permission
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_PERMISSION_CODE)
        } else {
            // Permission has already been granted
            writeCSVToFile(tableLayout, path)
        }
    }

    private fun writeCSVToFile(tableLayout: TableLayout, path: String) {
        try {
            val file = File(path)
            file.createNewFile()

            val fileWriter = FileWriter(file)
            val sb = StringBuilder()

            // Write table headers
            for (i in 0 until tableLayout.childCount) {
                val row = tableLayout.getChildAt(i)
                if (row is TableRow) {
                    for (j in 0 until row.childCount) {
                        val textView = row.getChildAt(j) as TextView
                        sb.append(textView.text.toString())
                        if (j < row.childCount - 1) {
                            sb.append(",")
                        }
                    }
                    sb.appendLine()
                }
            }

            // Write table data
            for (i in 0 until tableLayout.childCount) {
                val row = tableLayout.getChildAt(i)
                if (row is TableRow) {
                    for (j in 0 until row.childCount) {
                        val textView = row.getChildAt(j) as TextView
                        sb.append(textView.text.toString())
                        if (j < row.childCount - 1) {
                            sb.append(",")
                        }
                    }
                    sb.appendLine()
                }
            }

            fileWriter.write(sb.toString())
            fileWriter.flush()
            fileWriter.close()

            Log.d("Export", "CSV file saved to $path")

        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Export", "Error exporting table to CSV: ${e.message}")
        }
    }
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            WRITE_EXTERNAL_STORAGE_PERMISSION_CODE -> {
//                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                    // Permission granted, proceed with exporting
//                    exportTableToCSV(binding.tableLayout)
//                } else {
//                    Log.e("Permission", "Write external storage permission denied")
//                    // You may show a message to the user indicating why the permission is needed
//                    // For example, you can display a Snackbar or a Toast
//                    Toast.makeText(this, "Permission denied, cannot export table to CSV.", Toast.LENGTH_SHORT).show()
//                }
//                return
//            }
//            else -> {
//                // Handle other permission requests if any
//            }
//        }
//    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            WRITE_EXTERNAL_STORAGE_PERMISSION_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted, proceed with exporting
                    exportTableToCSV(binding.tableLayout)
                } else {
                    // Permission denied, handle accordingly
                    Log.e("Permission", "Write external storage permission denied")
                    // Check if user has selected "Don't ask again"
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        // User selected "Don't ask again", explain why permission is needed and redirect to app settings
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Permission Required")
                            .setMessage("This permission is required to export table data to CSV. Please enable it in app settings.")
                            .setPositiveButton("Settings") { _, _ ->
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri: Uri = Uri.fromParts("package", packageName, null)
                                intent.data = uri
                                startActivity(intent)
                            }
                            .setNegativeButton("Cancel") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    } else {
                        // User denied permission, inform them and handle accordingly
                        Toast.makeText(this, "Permission denied, cannot export table to CSV.", Toast.LENGTH_SHORT).show()
                    }
                }
                return
            }
            else -> {
                // Handle other permission requests if any
            }
        }
    }





}