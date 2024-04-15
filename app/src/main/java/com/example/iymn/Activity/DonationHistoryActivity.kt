package com.example.iymn.Activity

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iymn.Adapters.VegItemAdapter
import com.example.iymn.Models.VegItemViewModel
import com.example.iymn.R
import com.example.iymn.databinding.ActivityDonationHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DonationHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDonationHistoryBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: VegItemAdapter
    val dataList: MutableList<VegItemViewModel> = ArrayList()
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonationHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        askPermissions();
        val headerIcon: ImageView = findViewById(R.id.customHeaderIcon)
        val headerText: TextView = findViewById(R.id.customHeaderText)

        headerIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        headerText.setText("Donation History")


        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        // getting the recyclerview by its id
        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)

        // this creates a vertical layout Manager
        recyclerview.layoutManager = GridLayoutManager(this, 2)

        adapter = VegItemAdapter(dataList)
        recyclerview.adapter = adapter

        // Fetch data from Firestore and update the adapter's data list
        fetchDataBasedOnUserType()
        // Set item click listener in the activity
        adapter.setOnItemClickListener(object : VegItemAdapter.OnItemClickListener {
            override fun onItemClick(documentId: String) {
                // Handle item click here, navigate to details activity passing document ID
                val intent =
                    Intent(this@DonationHistoryActivity, DonationDetailsActivity::class.java)
                intent.putExtra("donationRef", documentId)
                startActivity(intent)
            }
        })

        binding.btnDownloadCsv.setOnClickListener {
            fetchForDonor { dataList ->
                createPDF(dataList)
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

    private fun createPDF(dataList: List<VegItemViewModel>) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(1080, 1920, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Create a Paint object for styling text
        val paint = Paint()
        paint.textSize = 42f
        paint.color = Color.BLACK

        // Define table dimensions and cell padding
        val tableWidth = 1000
        val cellPadding = 10
        val columnWidths = intArrayOf(200, 200, 300, 300) // Adjust column widths as needed
        val numRows = dataList.size + 1 // Add 1 for header row

        // Draw table header
        val tableHeaders = arrayOf("Item Name", "Quantity", "Date", "Recipient")
        for (col in tableHeaders.indices) {
            val xPos = cellPadding + columnWidths.slice(0 until col).sum()
            canvas.drawText(tableHeaders[col], xPos.toFloat(), (cellPadding + 100).toFloat(), paint)
        }

        // Draw table rows
        for (row in dataList.indices) {
            val item = dataList[row]
            val rowData = arrayOf(item.title, item.item1, item.item2, item.item3)

            for (col in rowData.indices) {
                val xPos = cellPadding + columnWidths.slice(0 until col).sum()
                val yPos = (cellPadding + (row + 2) * 100).toFloat() // Start from row 2 (after header)
                canvas.drawText(rowData[col], xPos.toFloat(), yPos, paint)
            }
        }

        // Finish the page and save the document
        document.finishPage(page)

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val fileName = "donations_$currentDate.pdf"
        val filePath = File(downloadsDir, fileName)
        try {
            val fos = FileOutputStream(filePath)
            document.writeTo(fos)
            document.close()
            fos.close()
            Toast.makeText(this, "PDF creation successful", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error creating PDF", Toast.LENGTH_LONG).show()
        }
    }

    fun convertXmlToPdf() {
        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        val binding = ActivityDonationHistoryBinding.inflate(layoutInflater)
        val view = binding.root
        val displayMetrics = resources.displayMetrics

        // Measure the view
        view.measure(
            View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, View.MeasureSpec.EXACTLY)
        )

        // Layout the view
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        // Create a new PdfDocument instance
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(view.measuredWidth, view.measuredHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Draw the view on the canvas
        view.draw(canvas)

        // Manually draw RecyclerView items onto the canvas
        val recyclerView = binding.recyclerview
        val adapter = recyclerView.adapter
        val layoutManager = recyclerView.layoutManager
        layoutManager?.let { lm ->
            adapter?.let { adp ->
                for (i in 0 until adp.itemCount) {
                    val viewHolder = adp.createViewHolder(recyclerView, adp.getItemViewType(i))
                    adp.onBindViewHolder(viewHolder, i)
                    viewHolder.itemView.measure(
                        View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    )
                    viewHolder.itemView.layout(0, 0, viewHolder.itemView.measuredWidth, viewHolder.itemView.measuredHeight)
                    viewHolder.itemView.draw(canvas)
                }
            }
        }

        // Finish the page and save the document
        document.finishPage(page)

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fileName = "donations_$currentDate.pdf"
        val filePath = File(downloadsDir, fileName)
        try {
            val fos = FileOutputStream(filePath)
            document.writeTo(fos)
            document.close()
            fos.close()
            Toast.makeText(this, "PDF creation successful", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error creating PDF", Toast.LENGTH_LONG).show()
        }
    }



    companion object {
        const val REQUEST_CODE = 1232
    }

    private fun fetchDataBasedOnUserType() {
        if (currentUser == null) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        } else {
            val uid = currentUser!!.uid
            db.collection("users").document(uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    val accountType = documentSnapshot.getString("accountType")
                    when (accountType) {
                        "NGO", "Admin" -> fetchForAdminAndNgo()
                        else -> fetchForDonor { dataList ->
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("DonationHistoryActivity", "Error fetching user data", e)
                    Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchForDonor(callback: (List<VegItemViewModel>) -> Unit) {
        db.collection("donations")
            .whereEqualTo("donorUID", currentUser?.uid)
            .orderBy("donateDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val dataList: MutableList<VegItemViewModel> = mutableListOf()
                val dateFormat = SimpleDateFormat("MM-dd-yyyy ", Locale.getDefault())
                for (document in documents) {
                    val docId = document.id
                    val imagePath = document.getString("image") ?: ""
                    val timestamp = document.getTimestamp("donateDate")
                    val date = timestamp?.toDate()?.let { dateFormat.format(it) } ?: "Invalid Date"
                    val vegName = document.getString("vegName") ?: "Default Item Name"
                    val status = document.getString("status") ?: "Default Item Name"
                    val recipient = document.getString("recipient") ?: "Default Item Name"
                    val fetchedQuantity = document.getString("quantity")
                    val fetchedQuantityType = document.getString("quantityType")
                    val formattedQuantity = getString(
                        R.string.formatted_quantity,
                        fetchedQuantity,
                        fetchedQuantityType
                    )
                    val itemName = vegName.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
                    dataList.add(VegItemViewModel(docId, imagePath, itemName,formattedQuantity,date, recipient, status))
                }
                adapter.updateData(dataList)
                callback(dataList) // Invoke the callback with the fetched data
            }
            .addOnFailureListener { e ->
                handleFetchDataFailure(e)
            }
    }
    private fun fetchForAdminAndNgo() {
        // Example: Fetching only donations with status "approved" for Admin and NGO
        db.collection("donations")
            .get()
            .addOnSuccessListener { documents ->
                val dataList: MutableList<VegItemViewModel> = mutableListOf()
                val dateFormat = SimpleDateFormat("MM-dd-yyyy HH:mm:ss", Locale.getDefault())
                for (document in documents) {
                    val docId = document.id
                    val imagePath = document.getString("image") ?: ""
                    val timestamp = document.getTimestamp("donateDate")
                    val date = timestamp?.toDate()?.let { dateFormat.format(it) } ?: "Invalid Date"
                    val vegName = document.getString("vegName") ?: "Default Item Name"
                    val status = document.getString("status") ?: "Default Item Name"
                    val recipient = document.getString("recipient") ?: "Default Item Name"
                    val fetchedQuantity = document.getString("quantity")
                    val fetchedQuantityType = document.getString("quantityType")
                    val formattedQuantity = getString(
                        R.string.formatted_quantity,
                        fetchedQuantity,
                        fetchedQuantityType
                    )
                    val itemName = vegName.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
                    dataList.add(VegItemViewModel(docId, imagePath, itemName,formattedQuantity,date, recipient, status))
                }
                adapter.updateData(dataList)
            }
            .addOnFailureListener { e ->
                handleFetchDataFailure(e)
            }
    }
    private fun handleFetchDataFailure(e: Exception) {
        Log.w("DonationHistoryActivity", "Error fetching data", e)
        Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show()
    }
    private fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMMM d, yyyy, h:mm a", Locale.getDefault())

        val date = inputFormat.parse(dateString)
        return outputFormat.format(date)
    }

}