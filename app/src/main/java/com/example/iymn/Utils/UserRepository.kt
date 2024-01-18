package com.example.iymn.Utils

import com.example.iymn.Models.User
import com.example.iymn.Models.UserType
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    fun getUserData(userId: String, onUserLoaded: (User) -> Unit, onError: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val userName = documentSnapshot.getString("email") ?: ""
                    val userTypeString = documentSnapshot.getString("accountType") ?: ""

                    val userType = when (userTypeString) {
                        "Admin" -> UserType.Admin
                        "Donor" -> UserType.Donor
                        "NGO" -> UserType.Ngo
                        else -> throw IllegalArgumentException("Invalid user type")
                    }

                    val user = User(userId, userName, userType)
                    onUserLoaded.invoke(user)
                } else {
                    onError.invoke(Exception("User not found"))
                }
            }
            .addOnFailureListener { exception ->
                onError.invoke(exception)
            }
    }
}
