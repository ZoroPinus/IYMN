package com.example.iymn.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.iymn.R
import com.example.iymn.Fragments.StartFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create an instance of Fragment1
        val startFragment = StartFragment()

        // Add the fragment to the activity
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, startFragment)
            .commit()
    }
}