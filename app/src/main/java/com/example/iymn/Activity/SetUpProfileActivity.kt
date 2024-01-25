package com.example.iymn.Activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.iymn.R
import com.example.iymn.databinding.ActivitySetUpProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.util.UUID

class SetUpProfileActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySetUpProfileBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var camLauncher: ActivityResultLauncher<Intent>
    private var selectedImage: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetUpProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        loadAndDisplayUserInfo()
        binding.btnSave.setOnClickListener {
            updateUserInfo()
        }
        binding.profileImage.setOnClickListener {
            showImageSourceDialog()
        }
        launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val imageUri = data.data
                    selectedImage = imageUri
                    binding.profileImage.setImageURI(imageUri)
                }
            }
            else{
                Toast.makeText(this, "result not ok", Toast.LENGTH_SHORT).show()
            }
        }
        camLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data = result.data
                val imageBitmap: Bitmap? = data?.extras?.getParcelable("data") as Bitmap?
                if (data != null) {
                    val tempUri = imageBitmap?.let { getImageUri(this, it) }
                    selectedImage = tempUri
                    binding.profileImage.setImageURI(tempUri)
                }
            }
            else{
                Toast.makeText(this, "result not ok", Toast.LENGTH_SHORT).show()
            }
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
    private fun updateUserInfo() {
        val user = auth.currentUser
        if (user != null) {
            val userDocument = db.collection("users").document(user.uid)
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val address = binding.etAddress.text.toString()
            val contact = binding.etContact.text.toString()

            if(name.isBlank() || email.isBlank() || address.isBlank() || contact.isBlank() || selectedImage == null){
                showEmptyFieldDialog("Kindly finish the form", "Empty Fields")
            }
            val updates = mutableMapOf(
                "name" to name,
                "email" to email,
                "address" to address,
                "contact" to contact,
                // Add other fields as needed
            )

            uploadToFirebase(selectedImage, userDocument, updates)

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
    private fun uploadToFirebase(
        imageUri: Uri?,
        userDoc: DocumentReference,
        updates: MutableMap<String, String>
    ) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imagesRef = storageRef.child("images/profileImg/${UUID.randomUUID()}.jpg") // Generate a unique filename

        val uploadTask = imageUri?.let { imagesRef.putFile(it) }

        if (uploadTask != null) {
            uploadTask.addOnSuccessListener { taskSnapshot ->
                // Image uploaded successfully, get the download URL
                imagesRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    updates["profileImageUrl"] = imageUrl

                    // Update the user document in Firestore
                    userDoc.update(updates as Map<String, Any>)
                        .addOnSuccessListener {
                            showSuccessFieldDialog()
                        }
                        .addOnFailureListener {
                            // Handle the failure
                            // You can display an error message or log the error
                        }

                }
            }.addOnFailureListener { e ->
                val errorMessage = "Error saving user data: ${e.message}"
                Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", errorMessage)
            }
        }
    }
    private fun loadAndDisplayUserInfo() {
        val user = auth.currentUser
        if (user != null) {
            val userDocument = db.collection("users").document(user.uid)

            userDocument.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Retrieve user data from Firestore
                        val name = documentSnapshot.getString("name")
                        val email = documentSnapshot.getString("email")
                        val address = documentSnapshot.getString("address")
                        val contact = documentSnapshot.getString("contact")
                        val image = documentSnapshot.getString("profileImageUrl")

                        if (name != null) {
                            binding.etName.setText(name)
                        }
                        if (email != null) {
                            binding.etEmail.setText(email)
                        }
                        if (address != null) {
                            binding.etAddress.setText(address)
                        }
                        if (contact != null) {
                            binding.etContact.setText(contact)
                        }
                        if (image != null) {

                            Glide.with(this)
                                .load(image)
                                .placeholder(R.drawable.ic_camera) // Placeholder image while loading
                                .error(R.drawable.ic_insert_img) // Image to show if loading fails
                                .into(binding.profileImage)
                        }
                    } else {
                        // Document doesn't exist
                        // Handle this case accordingly
                    }
                }
                .addOnFailureListener {
                    // Handle the failure
                    // You can display an error message or log the error
                }
        }
    }
    private fun showEmptyFieldDialog(message:String, status: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            setTitle(status)
            setMessage(message)
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun navigateToDashboardActivity() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showSuccessFieldDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            setTitle("Registration complete")
            setMessage("Press OK to Login")
            setPositiveButton("OK") { dialog, which ->
                startActivity(Intent(this@SetUpProfileActivity, DashboardActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}