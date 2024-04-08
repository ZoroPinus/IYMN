package com.example.iymn.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.iymn.R
import com.example.iymn.databinding.ActivityAboutUsBinding
import com.example.iymn.databinding.ActivityDonationDetailsBinding

class AboutUsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAboutUsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val headerIcon: ImageView = findViewById(R.id.customHeaderIcon)
        val headerText: TextView = findViewById(R.id.customHeaderText)

        headerIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        headerText.setText("About Us")

        Glide.with(this)
            .load(R.drawable.erol) // Replace with the actual image resource ID
            .into(binding.erol)

        Glide.with(this)
            .load(R.drawable.meloy) // Replace with the actual image resource ID
            .into(binding.meloy)

        Glide.with(this)
            .load(R.drawable.eds) // Replace with the actual image resource ID
            .into(binding.eds)
    }
}