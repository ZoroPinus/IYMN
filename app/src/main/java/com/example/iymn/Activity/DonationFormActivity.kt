package com.example.iymn.Activity

import android.content.ClipDescription
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder.DeathRecipient
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.iymn.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class DonationFormActivity : AppCompatActivity() {
    lateinit var etVegName: EditText
    lateinit var etWeight: EditText
    lateinit var etRecipient: Spinner
    lateinit var etAddress: EditText
    lateinit var etDescription: EditText
    private lateinit var btnInsertImg: Button
    private lateinit var btnDonate: Button
    private lateinit var auth: FirebaseAuth
    private var selectedOption: String = ""
    private var selectedImage: Uri? = null
    lateinit var db: FirebaseFirestore
    lateinit var storage: FirebaseStorage

    private lateinit var ivVegImage: ImageView
    private lateinit var launcher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_form)

        // View Bindings
        etRecipient = findViewById(R.id.etRecipient)
        etVegName = findViewById(R.id.etVegName)
        etWeight = findViewById(R.id.etWeight)
        etAddress = findViewById(R.id.etAddress)
        etDescription = findViewById(R.id.etDescription)
        btnInsertImg = findViewById(R.id.btnInsertImg)
        btnDonate = findViewById(R.id.btnDonate)
        ivVegImage = findViewById(R.id.ivVegImg)

        // Retrieve text from EditTexts when needed

        // Initialising auth object
        auth = FirebaseAuth.getInstance()

        db = Firebase.firestore

        // Define your list of options
        val options = arrayOf("Cordillera Youth Center", "Zero Waste Baguio")

        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        etRecipient.adapter = adapter

        // Set a listener to handle item selections
        etRecipient.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Handle the selected item here
                selectedOption = options[position]
                // Do something with the selected item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle no selection if needed
            }
        }


        btnInsertImg.setOnClickListener {
            chooseImage();
        }

        launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val imageUri = data.data
                    selectedImage = imageUri
                    ivVegImage.setImageURI(imageUri)
                }
            }
        }

        btnDonate.setOnClickListener {
            val vegNameString = etVegName.text.toString()
            val weightString = etWeight.text.toString()
            val addressString = etAddress.text.toString()
            val descriptionString = etDescription.text.toString()

            submitDonation(vegNameString,selectedImage, descriptionString, addressString, weightString, selectedOption)

        }

    }

    private fun submitDonation(vegName: String, imageUri: Uri?, description: String, address:String, quantity: String,
                               recipient: String ) {

        if (vegName.isBlank() || description.isBlank() || quantity.isBlank() || address.isBlank() || recipient.isBlank()  ) {
            Toast.makeText(this, "Kindly Complete the form", Toast.LENGTH_SHORT).show()

            return
        }

        if (imageUri == null) {
            Toast.makeText(this, "Kindly include an image", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Get the currently logged-in user's UID
        val currentUser = auth.currentUser?.uid

        val donationsCollection = FirebaseFirestore.getInstance().collection("donations")

        val donationData = hashMapOf(
            "vegName" to vegName,
            "description" to description,
            "quantity" to quantity,
            "address" to address,
            "image" to "",
            "recipient" to recipient,
            "donorUID" to currentUser // Store the donor's UID
            // Add more fields as needed
        )

        // Add the donation data to Firestore
        donationsCollection.add(donationData)
            .addOnSuccessListener { documentReference ->
                // Donation saved successfully
                val donationId = documentReference.id
                // Handle success or perform additional operations
                // For example, upload image to Firebase Storage and update donation document with image URL
                if (imageUri != null) {
                    uploadImageToFirebaseStorage(imageUri, donationId)
                }
            }
            .addOnFailureListener { e ->
                // Handle failure
                Log.e("Firestore", "Error adding document", e)
            }


    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri, donationId: String) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imagesRef = storageRef.child("images/${UUID.randomUUID()}.jpg") // Generate a unique filename

        val uploadTask = imagesRef.putFile(imageUri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Image uploaded successfully, get the download URL
            imagesRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                // Store the imageUrl in Firestore or use it as needed
                storeImageUrlInFirestore(imageUrl, donationId)
            }
        }.addOnFailureListener { e ->
            val errorMessage = "Error saving user data: ${e.message}"
            Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show()
            Log.e("Firestore", errorMessage)
        }
    }

    private fun storeImageUrlInFirestore(imageUrl: String, donationId: String) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("donations").document(donationId) // Replace with your document ID

        // Store the image URL in Firestore
        docRef.update("image", imageUrl)
            .addOnSuccessListener {
                Toast.makeText(this, "Upload stored successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                val errorMessage = "Error saving user data: ${e.message}"
                Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", errorMessage)
            }
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        launcher.launch(intent)
    }

//    fun takePicture() {
//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        launcher.launch(intent)
//    }
}