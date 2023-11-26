package com.example.iymn

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class UserOptFragment : Fragment(R.layout.fragment_useropt) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the button by its ID
        val btnDonorReg: Button = view.findViewById(R.id.btnDonorReg)
        val btnNGOReg: Button = view.findViewById(R.id.btnNGOReg)
        val btnAdmin: Button = view.findViewById(R.id.btnAdmin)

        fun navigateToNextActivity(buttonName: String) {
            val intent = Intent(activity, RegistrationFormActivity::class.java)
            intent.putExtra("ACCOUNT_TYPE", buttonName)
            startActivity(intent)
        }

        btnDonorReg.setOnClickListener {
            navigateToNextActivity("Button 1")
        }

        btnNGOReg.setOnClickListener {
            navigateToNextActivity("Button 2")
        }

        btnAdmin.setOnClickListener {
            navigateToNextActivity("Button 3")
        }
    }
}