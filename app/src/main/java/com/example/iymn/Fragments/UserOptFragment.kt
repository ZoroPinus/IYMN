package com.example.iymn.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button

import com.example.iymn.Activity.RegistrationFormActivity
import com.example.iymn.R

class UserOptFragment : Fragment(R.layout.fragment_useropt) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnDonorReg: Button = view.findViewById(R.id.btnDonorReg)
        val btnNGOReg: Button = view.findViewById(R.id.btnNGOReg)

        fun navigateToNextActivity(buttonName: String) {
            val intent = Intent(activity, RegistrationFormActivity::class.java)
            intent.putExtra("ACCOUNT_TYPE", buttonName)
            startActivity(intent)
        }

        btnDonorReg.setOnClickListener {
            navigateToNextActivity("Donor")
        }

        btnNGOReg.setOnClickListener {
            navigateToNextActivity("NGO")
        }

    }
}