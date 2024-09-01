package com.example.iymn.Fragments

import android.app.Activity
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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.iymn.databinding.FragmentAddNGOPartnerBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID

class AddNGOPartnerFragment : Fragment() {
    private var _binding: FragmentAddNGOPartnerBinding? = null
    private val binding get() = _binding!!
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var camLauncher: ActivityResultLauncher<Intent>

    private lateinit var selectedImage: Uri
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddNGOPartnerBinding.inflate(inflater, container, false)
        val view = binding.root

        // Set click listener for image insertion
        binding.btnInsertImg.setOnClickListener {
            showImageSourceDialog()
        }

        // Set click listener for adding NGO
        binding.btnProceed.setOnClickListener {
            val ngoName = binding.etNGOName.text.toString()
            val address = binding.etAddress.text.toString()
            val contact = binding.etContact.text.toString()
            val areaOfResponsibility = binding.etAreaOfResponsibility.text.toString()

            // Validate inputs and proceed to submit data
            if (ngoName.isNotEmpty() && address.isNotEmpty() && contact.isNotEmpty()) {
                // Call a function to submit NGO data
                submitNGOData(ngoName, address, contact, areaOfResponsibility, selectedImage)
            } else {
                // Show an error or prompt to fill in all fields
                // You can implement your own logic here
            }
        }

        launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val imageUri = data.data
                    if (imageUri != null) {
                        selectedImage = imageUri
                    }
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
                    if (tempUri != null) {
                        selectedImage = tempUri
                    }
                    binding.btnInsertImg.setImageURI(tempUri)
                }
            }
            else{
                Toast.makeText(requireContext(), "result not ok", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
    private fun submitNGOData(ngoName: String, address: String, contact: String, areaOfResponsibility: String, imageUri: Uri?) {
        if (ngoName.isBlank() || address.isBlank() || contact.isBlank()|| areaOfResponsibility.isBlank()) {
            showEmptyFieldDialog("Kindly complete the form")
            return
        }

        if (imageUri == null) {
            showEmptyFieldDialog("Kindly include an image")
            return
        }

        val db = FirebaseFirestore.getInstance()
        val ngoPartnersCollection = db.collection("ngoPartners")

        val ngoData = hashMapOf(
            "ngoName" to ngoName,
            "address" to address,
            "contact" to contact,
            "areaOfResponsibility" to areaOfResponsibility,
            "image" to "" // Placeholder for the image URL
            // Add more fields as needed
        )

        ngoPartnersCollection.add(ngoData)
            .addOnSuccessListener { documentReference ->
                val ngoId = documentReference.id
                if (imageUri != null) {
                    uploadImageToFirebaseStorage(imageUri, ngoId)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding document", e)
            }
    }

    // Function to upload image to Firebase Storage
    private fun uploadImageToFirebaseStorage(imageUri: Uri, ngoId: String) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imagesRef = storageRef.child("ngoImages/${UUID.randomUUID()}.jpg") // Generate a unique filename

        val uploadTask = imagesRef.putFile(imageUri)

        uploadTask.addOnSuccessListener { _ ->
            imagesRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                storeImageUrlInFirestore(imageUrl, ngoId)
            }
        }.addOnFailureListener { e ->
            val errorMessage = "Error uploading image: ${e.message}"
            Log.e("Firestore", errorMessage)
        }
    }

    // Function to store image URL in Firestore
    private fun storeImageUrlInFirestore(imageUrl: String, ngoId: String) {
        val db = FirebaseFirestore.getInstance()
        val ngoRef = db.collection("ngoPartners").document(ngoId)

        ngoRef.update("image", imageUrl)
            .addOnSuccessListener {
                Toast.makeText(context, "Upload stored successfully", Toast.LENGTH_SHORT).show()
                // Handle success - For example, clear form fields
            }
            .addOnFailureListener { e ->
                val errorMessage = "Error storing image URL: ${e.message}"
                Log.e("Firestore", errorMessage)
                Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to take a picture using camera
    private fun takePicture(context: Context, camLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        camLauncher.launch(intent)
    }

    // Function to choose an image from gallery
    private fun chooseImage(context: Context, launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        launcher.launch(intent)
    }

    // Function to show image source dialog
    private fun showImageSourceDialog() {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Choose Image Source")
        dialog.setMessage("Take picture or choose from gallery?")
        dialog.setPositiveButton("Take Picture") { _, _ ->
            takePicture(requireContext(), camLauncher)
        }
        dialog.setNegativeButton("Choose from Gallery") { _, _ ->
            chooseImage(requireContext(), launcher)
        }
        dialog.show()
    }

    // Function to show empty field dialog
    private fun showEmptyFieldDialog(message: String) {
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