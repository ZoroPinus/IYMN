package com.example.iymn.Fragments

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import com.example.iymn.R
import com.example.iymn.databinding.FragmentDonationFormBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DonationFormFragment : Fragment() {
    private lateinit var binding: FragmentDonationFormBinding
    private lateinit var auth: FirebaseAuth
    private var selectedOptionNgo: String = ""
    private var selectedOptionQuantity: String = ""
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

        // Define your list of options
        val ngoOptions = arrayOf("Cordillera Youth Center", "Zero Waste Baguio")
        val quantityOptions = arrayOf("Kg", "Sack/s", "Piece/s", "Ton/s")

        // Create an ArrayAdapter using the string array and a default spinner layout
        val ngoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ngoOptions)
        val quantityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, quantityOptions)

        // Specify the layout to use when the list of choices appears
        ngoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        quantityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        binding.etRecipient.adapter = ngoAdapter
        binding.spinnerQuantityType.adapter = quantityAdapter


        headerIcon.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        headerText.setText("Donation Form")


        // Set a listener to handle item selections
        binding.etRecipient.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        binding.btnCropList.setOnClickListener {
            binding.cropListFragmentContainer.visibility = View.VISIBLE
            val fragment = CropFragment()
            childFragmentManager.beginTransaction()
                .replace(R.id.cropListFragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
        childFragmentManager.addOnBackStackChangedListener {
            if (childFragmentManager.backStackEntryCount == 0) {
                // If back stack is empty, hide the FrameLayout
                binding.cropListFragmentContainer.visibility = View.GONE
            }
        }

        childFragmentManager.setFragmentResultListener("cropSelection", this) { _, result ->
            val selectedCropName = result.getString("selectedCropName", "")
            binding.etVegName.setText(selectedCropName)
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
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val currentDateTime = dateFormat.format(Date())
            val status = "PENDING"
            submitDonation(vegNameString, selectedImage, descriptionString, addressString, weightString, selectedOptionNgo, selectedOptionQuantity, currentDateTime, status)
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
                               recipient: String, quantityType: String, donateDate: String, status: String
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
            "donorUID" to currentUser,
            "donateDate" to donateDate,
            "status" to status
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
            Toast.makeText(requireContext(), "Upload Failed", Toast.LENGTH_SHORT).show()
            Log.e("Firestore", errorMessage)
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

}