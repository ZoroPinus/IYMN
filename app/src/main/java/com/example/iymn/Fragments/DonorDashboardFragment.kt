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
import com.example.iymn.Activity.NGOPartnersActivity
import com.example.iymn.R
import com.example.iymn.databinding.FragmentDonorDashboardBinding
import com.example.iymn.databinding.FragmentNGODashboardBinding

class DonorDashboardFragment : Fragment() {
    private var _binding: FragmentDonorDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDonorDashboardBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.btnDonateDonor.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    DonationFormActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
        binding.btnDonationHistory.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    DonationHistoryActivity::class.java
                )
            )
        }
        binding.btnNgoPartners.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    NGOPartnersActivity::class.java
                )
            )
        }
        binding.btnFoodMapDonor.setOnClickListener {
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