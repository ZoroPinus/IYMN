package com.example.iymn.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.iymn.Activity.DonationHistoryActivity
import com.example.iymn.R
import com.example.iymn.databinding.FragmentAdminDashboardBinding
import com.example.iymn.databinding.FragmentNGODashboardBinding
class AdminDashboardFragment : Fragment() {
    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.btnDonationHistory.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    DonationHistoryActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
        binding.btnNGOPartners.setOnClickListener {
            Toast.makeText(requireContext(), "NGO Partners", Toast.LENGTH_SHORT).show()
        }
        binding.btnInbox.setOnClickListener {
            Toast.makeText(requireContext(), "Inbox", Toast.LENGTH_SHORT).show()
        }
        binding.btnAboutUs.setOnClickListener {
            Toast.makeText(requireContext(), "Food Map", Toast.LENGTH_SHORT).show()
        }
        binding.btnFoodMap.setOnClickListener {
            Toast.makeText(requireContext(), "Add Feedback", Toast.LENGTH_SHORT).show()
        }
        binding.btnRegisteredAccounts.setOnClickListener {
            Toast.makeText(requireContext(), "Top Donors", Toast.LENGTH_SHORT).show()
        }
        binding.btnNotifications.setOnClickListener {
            Toast.makeText(requireContext(), "About Us", Toast.LENGTH_SHORT).show()
        }
        binding.btnFeedbacks.setOnClickListener {
            Toast.makeText(requireContext(), "Notifications", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}