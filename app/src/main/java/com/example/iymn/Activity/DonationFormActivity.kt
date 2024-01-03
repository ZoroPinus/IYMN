package com.example.iymn.Activity

import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.iymn.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID

class DonationFormActivity : AppCompatActivity() {
    lateinit var etVegName: EditText
    lateinit var etQuantity: EditText
    lateinit var etRecipient: Spinner
    lateinit var spinnerQuantityType: Spinner
    lateinit var etAddress: EditText
    lateinit var etDescription: EditText
    private lateinit var btnInsertImg: ImageButton
    private lateinit var btnDonate: Button
    private lateinit var auth: FirebaseAuth
    private var selectedOptionNgo: String = ""
    private var selectedOptionQuantity: String = ""
    private var selectedImage: Uri? = null
    lateinit var db: FirebaseFirestore
    lateinit var storage: FirebaseStorage

    private lateinit var ivVegImage: ImageView
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var camLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_form)

        // View Bindings
        etRecipient = findViewById(R.id.etRecipient)
        spinnerQuantityType = findViewById(R.id.spinnerQuantityType)
        etQuantity = findViewById(R.id.etQuantity)
        etVegName = findViewById(R.id.etVegName)
        etAddress = findViewById(R.id.etAddress)
        etDescription = findViewById(R.id.etDescription)
        btnInsertImg = findViewById(R.id.btnInsertImg)
        btnDonate = findViewById(R.id.btnDonate)
//        ivVegImage = findViewById(R.id.ivVegImg)

        // Retrieve text from EditTexts when needed

        // Initialising auth object
        auth = FirebaseAuth.getInstance()

        db = Firebase.firestore

        // Define your list of options
        val ngoOptions = arrayOf("Cordillera Youth Center", "Zero Waste Baguio")
        val quantityOptions = arrayOf("Kg", "Sack/s", "Piece/s")


        // Create an ArrayAdapter using the string array and a default spinner layout
        val ngoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ngoOptions)
        val quantityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, quantityOptions)

        // Specify the layout to use when the list of choices appears
        ngoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        quantityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        etRecipient.adapter = ngoAdapter
        spinnerQuantityType.adapter = quantityAdapter

        // Set a listener to handle item selections
        etRecipient.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Handle the selected item here
                selectedOptionNgo = ngoOptions[position]
                // Do something with the selected item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle no selection if needed
            }
        }// Set a listener to handle item selections
        spinnerQuantityType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Handle the selected item here
                selectedOptionQuantity = quantityOptions[position]
                // Do something with the selected item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle no selection if needed
            }
        }


        btnInsertImg.setOnClickListener {
//            chooseImage()
//            takePicture()
            showImageSourceDialog()
        }



        launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val imageUri = data.data
                    selectedImage = imageUri
                    btnInsertImg.setImageURI(imageUri)
                }
            }
            else{
                Toast.makeText(this, "result not ok", Toast.LENGTH_SHORT).show()
            }
        }
        camLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val imageBitmap: Bitmap? = data?.extras?.getParcelable("data") as Bitmap?
                if (data != null) {
                    val tempUri = imageBitmap?.let { getImageUri(this@DonationFormActivity, it) }
                    selectedImage = tempUri
                    btnInsertImg.setImageURI(tempUri)
                }
            }
            else{
                Toast.makeText(this, "result not ok", Toast.LENGTH_SHORT).show()
            }
        }




        btnDonate.setOnClickListener {
            val vegNameString = etVegName.text.toString()
            val weightString = etQuantity.text.toString()
            val addressString = etAddress.text.toString()
            val descriptionString = etDescription.text.toString()
            submitDonation(vegNameString,selectedImage, descriptionString, addressString, weightString, selectedOptionNgo, selectedOptionQuantity)
        }
    }
    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = MediaStore.Images.Media.insertImage(
            inContext.contentResolver,
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }
    private fun submitDonation(vegName: String, imageUri: Uri?, description: String, address:String, quantity: String,
                               recipient: String, quantityType: String ) {

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
            "quantityType" to quantityType,
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
    private fun takePicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        camLauncher.launch(intent)
    }
    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        launcher.launch(intent)
    }

    private fun showImageSourceDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Choose Image Source")
        dialog.setMessage("Take picture or choose from gallery?")
        dialog.setPositiveButton("Take Picture") { _, _ ->
            takePicture()
        }
        dialog.setNegativeButton("Choose from Gallery") { _, _ ->
            chooseImage()
        }
        dialog.show()
    }


}