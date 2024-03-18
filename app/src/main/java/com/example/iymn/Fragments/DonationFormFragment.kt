package com.example.iymn.Fragments

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.iymn.Models.CropListOption
import com.example.iymn.Models.LocationData
import com.example.iymn.Models.NGOOption
import com.example.iymn.R
import com.example.iymn.databinding.FragmentDonationFormBinding
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.security.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DonationFormFragment : Fragment() {
    private lateinit var binding: FragmentDonationFormBinding
    private lateinit var auth: FirebaseAuth
    private var selectedOptionCropList: String = ""
    private var selectedOptionNgo: String = ""
    private var selectedOptionQuantity: String = ""
    private var latLng: GeoPoint? = null
    private var name: String? = ""
    private var placeName: String = ""
    private lateinit var latlngString: String
    private lateinit var ngoOptionsList: List<NGOOption>
    private lateinit var cropOptionsList: List<CropListOption>
    private var selectedImage: Uri? = null
    lateinit var db: FirebaseFirestore
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var camLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDonationFormBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        db = Firebase.firestore

        val headerIcon: ImageView = requireView().findViewById(R.id.customHeaderIcon)
        val headerText: TextView = requireView().findViewById(R.id.customHeaderText)

        fetchCropListFromFirestore()
        fetchNGOOptionsFromFirestore()

        val quantityOptions = arrayOf("Kg", "Sack/s", "Piece/s", "Ton/s")

        val quantityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, quantityOptions)

        quantityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerQuantityType.adapter = quantityAdapter

        headerIcon.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        headerText.setText("Donation Form")

        // Set a listener to handle item selections
        binding.etRecipient.prompt="Select an NGO partner"
        binding.etRecipient.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Handle the selected item here
                selectedOptionNgo = ngoOptionsList[position].name
                // Do something with the selected item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle no selection if needed
            }
        }
        binding.spinnerCropList.prompt = "Select a crop"

        binding.spinnerCropList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Handle the selected item here
                selectedOptionCropList = cropOptionsList[position].id
                binding.etVegName.setText(cropOptionsList[position].name)
                // Do something with the selected item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding.etVegName.text = null
            }
        }

        binding.spinnerQuantityType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

        binding.btnInsertImg.setOnClickListener {
            showImageSourceDialog()
        }


        binding.btnChooseFromMap.setOnClickListener {
            // Replace 'YourNewFragment()' with the fragment you want to open
            val newFragment = ChooseLocationFragment()
            // Begin fragment transaction
            val fragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()

            // Replace the current fragment with the new fragment
            fragmentTransaction.replace(R.id.fragmentContainer, newFragment)

            // Optional: Add to back stack for handling back navigation
            fragmentTransaction.addToBackStack(null)

            // Commit the transaction
            fragmentTransaction.commit()
        }

        parentFragmentManager.setFragmentResultListener("choosenLocation", this) { _, result ->
            val placeName = result.getString("placeName")
            val latlngString = result.getString("latlng")
            latLng = latlngString?.let { convertStringToGeoPoint(it) }

            if (!placeName.isNullOrBlank()) {
                binding.etAddress.setText(placeName)
            }
        }

        launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val imageUri = data.data
                    selectedImage = imageUri
                    binding.btnInsertImg.setImageURI(imageUri)
                }
            }
            else{
                Toast.makeText(requireContext(), "result not ok", Toast.LENGTH_SHORT).show()
            }
        }
        camLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data = result.data
                val imageBitmap: Bitmap? = data?.extras?.getParcelable("data") as Bitmap?
                if (data != null) {
                    val tempUri = imageBitmap?.let { getImageUri(requireContext(), it) }
                    selectedImage = tempUri
                    binding.btnInsertImg.setImageURI(tempUri)
                }
            }
            else{
                Toast.makeText(requireContext(), "result not ok", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDonate.setOnClickListener {
            val vegNameString = binding.etVegName.text.toString()
            val weightString = binding.etQuantity.text.toString()
            val addressString = binding.etAddress.text.toString()
            val descriptionString = binding.etDescription.text.toString()
            val currentDateTime = com.google.firebase.Timestamp.now()
            val status = "PENDING"
            latLng?.let { it1 ->
                submitDonation(vegNameString, selectedImage, descriptionString, addressString, weightString, selectedOptionNgo, selectedOptionQuantity, currentDateTime, status,
                    it1
                )
            }
        }

    }

    private fun convertStringToGeoPoint(latLngString: String): GeoPoint? {
        try {
            val cleanString = latLngString
                .replace("lat/lng:", "")
                .replace("(", "")
                .replace(")", "")
                .trim()

            val parts = cleanString.split(",")

            if (parts.size == 2) {
                val latitude = parts[0].toDoubleOrNull()
                val longitude = parts[1].toDoubleOrNull()

                if (latitude != null && longitude != null) {
                    return GeoPoint(latitude, longitude)
                }
            }
        } catch (e: NumberFormatException) {
            // Handle the case when the conversion fails
            e.printStackTrace()
        }

        // Return null if the conversion is not successful
        return null
    }


    private fun fetchCropListFromFirestore() {
        db.collection("crops")
            .get()
            .addOnSuccessListener { result ->
                cropOptionsList = result.documents.map { document ->
                    val id = document.id
                    val name = document.getString("name") ?: ""
                    CropListOption(id, name)
                }

                // Populate spinner with NGO options
                populateSpinnerWithCropList()
            }
            .addOnFailureListener { exception ->
                // Handle errors
                Log.w(ContentValues.TAG, "Error getting NGO options", exception)
            }
    }

    private fun populateSpinnerWithCropList() {
        val cropOptions = cropOptionsList.map { it.name }.toTypedArray()
        val cropOptionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cropOptions)
        cropOptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCropList.adapter = cropOptionAdapter
    }
    private fun fetchNGOOptionsFromFirestore() {
        db.collection("ngoPartners")
            .get()
            .addOnSuccessListener { result ->
                ngoOptionsList = result.documents.map { document ->
                    val id = document.id
                    val name = document.getString("ngoName") ?: ""
                    NGOOption(id, name)
                }

                // Populate spinner with NGO options
                populateSpinnerWithNGOOptions()
            }
            .addOnFailureListener { exception ->
                // Handle errors
                Log.w(ContentValues.TAG, "Error getting NGO options", exception)
            }
    }
    private fun populateSpinnerWithNGOOptions() {
        val ngoOptions = ngoOptionsList.map { it.name }.toTypedArray()
        val ngoListAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ngoOptions)
        ngoListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.etRecipient.adapter = ngoListAdapter
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
                               recipient: String, quantityType: String, donateDate: com.google.firebase.Timestamp, status: String, latLng: GeoPoint
    ) {

        if (vegName.isBlank() || description.isBlank() || quantity.isBlank() || address.isBlank() || recipient.isBlank()  ) {
            showEmptyFieldDialog("Kindly Complete the form")
            return
        }

        if (imageUri == null) {
            showEmptyFieldDialog("Kindly include an image")
            return
        }

        // Get the currently logged-in user's UID
        val currentUserUID = auth.currentUser?.uid
        if (currentUserUID != null) {
            // Fetch user data from Firestore using the UID
            db.collection("users").document(currentUserUID)
                .get()
                .addOnSuccessListener { userDocument ->
                    name = userDocument.getString("name").toString()
                    if (name != null) {
                        // Use the userName in your code
                        Log.d(TAG, "Current user's name: $name")
                    } else {
                        Log.e(TAG, "User document does not contain a 'name' field.")
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle errors
                    Log.e(TAG, "Error getting current user data: ", exception)
                }
        } else {
            Log.e(TAG, "Current user is null.")
        }

        val donationsCollection = FirebaseFirestore.getInstance().collection("donations")

        val donationData = hashMapOf(
            "vegName" to vegName,
            "description" to description,
            "quantity" to quantity,
            "quantityType" to quantityType,
            "address" to address,
            "image" to "",
            "recipient" to recipient,
            "donorUID" to currentUserUID,
            "donorName" to name,
            "donateDate" to donateDate,
            "status" to status,
            "latlng" to latLng,
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
                Log.e("Firestore", latLng.toString())
            }
            .addOnFailureListener { e ->
                // Handle failure
                Log.e("Firestore", "Error adding document", e)
            }
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri, donationId: String) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imagesRef = storageRef.child("images/donations/${UUID.randomUUID()}.jpg") // Generate a unique filename

        val resizedBitmap = resizeImage(imageUri)
        val compressedByteArray = resizedBitmap?.let { compressImage(it) }
        val uploadTask = compressedByteArray?.let { imagesRef.putBytes(it) }

        if (uploadTask != null) {
            uploadTask.addOnSuccessListener { taskSnapshot ->
                // Image uploaded successfully, get the download URL
                imagesRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    // Store the imageUrl in Firestore or use it as needed
                    storeImageUrlInFirestore(imageUrl, donationId)
                }
            }.addOnFailureListener { e ->
                val errorMessage = "Error saving user data: ${e.message}"
                Toast.makeText(requireContext(), "Upload Failed", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", errorMessage)
            }
        }
    }

    private fun storeImageUrlInFirestore(imageUrl: String, donationId: String) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("donations").document(donationId) // Replace with your document ID

        // Store the image URL in Firestore
        docRef.update("image", imageUrl)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Upload stored successfully", Toast.LENGTH_SHORT).show()
                val successDialog = AlertDialog.Builder(requireContext())
                    .setTitle("Success")
                    .setMessage("Donation submitted successfully")
                    .setPositiveButton("OK") { dialog, _ ->
                        // Clear form fields here
                        binding.etVegName.setText("")
                        binding.etAddress.setText("")
                        binding.etDescription.setText("")
                        binding.etQuantity.setText("")
                        binding.btnInsertImg.setImageResource(R.drawable.ic_insert_img)
                        // Clear other fields similarly
                        dialog.dismiss() // Dismiss dialog
                    }
                    .create()
                successDialog.show()
            }
            .addOnFailureListener { e ->
                val errorMessage = "Error saving user data: ${e.message}"
                Toast.makeText(requireContext(), "Upload Failed", Toast.LENGTH_SHORT).show()
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
        val dialog = AlertDialog.Builder(requireContext())
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
    private fun showEmptyFieldDialog(message:String) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.apply {
            setTitle("Empty Fields")
            setMessage(message)
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun resizeImage(uri: Uri?): Bitmap? {
        val inputStream = requireActivity().contentResolver.openInputStream(uri!!)
        val options = BitmapFactory.Options()
        options.inSampleSize = 2 // Adjust the value as needed
        return BitmapFactory.decodeStream(inputStream, null, options)
    }

    private fun compressImage(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // Adjust quality as needed
        return outputStream.toByteArray()
    }

}