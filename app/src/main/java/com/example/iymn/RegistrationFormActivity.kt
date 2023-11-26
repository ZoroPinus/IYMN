package com.example.iymn

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class RegistrationFormActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_form)

        // Retrieve the buttonName from the intent extras
        val buttonName = intent.getStringExtra("BUTTON_NAME")

        // Now you can use the buttonName as needed in this activity
        // For example, set it to a TextView
        val textView: TextView = findViewById(R.id.textView)
        textView.text = "You clicked: $buttonName"
    }
}