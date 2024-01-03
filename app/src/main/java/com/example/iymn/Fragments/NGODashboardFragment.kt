package com.example.iymn.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.iymn.Activity.DonationFormActivity
import com.example.iymn.Activity.DonationHistoryActivity
import com.example.iymn.R
import com.example.iymn.databinding.FragmentNGODashboardBinding

class NGODashboardFragment : Fragment() {
    private var _binding: FragmentNGODashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNGODashboardBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.btnReportsNGO.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    DonationHistoryActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
        binding.btnAcceptedDonations.setOnClickListener {
            Toast.makeText(requireContext(), "NGO Partners", Toast.LENGTH_SHORT).show()
        }
        binding.btnFoodMapNGO.setOnClickListener {
            Toast.makeText(requireContext(), "Food Map", Toast.LENGTH_SHORT).show()
        }
        binding.btnAddFeedbackDonor.setOnClickListener {
            Toast.makeText(requireContext(), "Add Feedback", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}