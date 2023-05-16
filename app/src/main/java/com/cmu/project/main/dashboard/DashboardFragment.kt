package com.cmu.project.main.dashboard

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cmu.project.R
import com.cmu.project.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var binding: FragmentDashboardBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentDashboardBinding.bind(view)
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        binding.ivMaps.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_mapsFragment)
        }
        binding.ivBookSearch.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_bookSearchFragment)
        }
    }

}