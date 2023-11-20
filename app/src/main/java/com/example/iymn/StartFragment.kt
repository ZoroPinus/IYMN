package com.example.iymn

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class StartFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_start, container, false)

        // Get the button
        val button = view.findViewById<Button>(R.id.button)

        // Set a click listener for the button
        button.setOnClickListener {
            // Create an instance of Fragment2
            val fragment2 = UserOptFragment()

            // Replace Fragment1 with Fragment2
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment2)
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}