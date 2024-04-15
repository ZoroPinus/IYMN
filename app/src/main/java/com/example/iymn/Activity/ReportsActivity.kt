package com.example.iymn.Activity

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.iymn.R
import com.example.iymn.databinding.ActivityDonationHistoryBinding
import com.example.iymn.databinding.ActivityReportsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ReportsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val requestCode = 42 // Your unique request code
    private val writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE

    companion object {
        private const val WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 1001
        const val REQUEST_CODE = 1232
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
//        binding.btnDownloadCsv.setOnClickListener {
//            convertXmlToPdf()
//        }
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

    private fun askPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            DonationHistoryActivity.Companion.REQUEST_CODE
        )
    }

    private fun createPDF() {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(1080, 1920, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        paint.setColor(Color.RED)
        paint.textSize = 42f
        val text = "Hello, World"
        val x = 500f
        val y = 900f
        canvas.drawText(text, x, y, paint)
        document.finishPage(page)
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fileName = "example.pdf"
        val file = File(downloadsDir, fileName)
        try {
            val fos = FileOutputStream(file)
            document.writeTo(fos)
            document.close()
            fos.close()
            Toast.makeText(this, "Written Successfully!!!", Toast.LENGTH_SHORT).show()
        } catch (e: FileNotFoundException) {
            Log.d("mylog", "Error while writing $e")
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun convertXmlToPdf() {
        // Inflate the layout using view binding
        val binding = ActivityReportsBinding.inflate(layoutInflater)
        val view = binding.root

        // Measure the view to get its width and height
        view.measure(
            View.MeasureSpec.makeMeasureSpec(resources.displayMetrics.widthPixels, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(resources.displayMetrics.heightPixels, View.MeasureSpec.EXACTLY)
        )

        // Set the layout size based on measured dimensions
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        // Create a new PdfDocument instance
        val document = PdfDocument()

        // Create a PageInfo object specifying the page attributes
        val pageInfo = PdfDocument.PageInfo.Builder(view.measuredWidth, view.measuredHeight, 1).create()

        // Start a new page
        val page = document.startPage(pageInfo)

        // Get the Canvas object to draw on the page
        val canvas = page.canvas

        // Create a Paint object for styling the view
        val paint = Paint()
        paint.color = Color.WHITE

        // Draw the view on the canvas
        view.draw(canvas)

        // Finish the page
        document.finishPage(page)

        // Specify the path and filename of the output PDF file
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fileName = "exampleXML.pdf"
        val filePath = File(downloadsDir, fileName)
        try {
            // Save the document to a file
            val fos = FileOutputStream(filePath)
            document.writeTo(fos)
            document.close()
            fos.close()
            // PDF conversion successful
            Toast.makeText(this, "XML to PDF Conversion Successful", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            // Error occurred while converting to PDF
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